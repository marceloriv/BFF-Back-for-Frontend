package com.example.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Enumeration;
import com.example.demo.service.JwtService;

/*
 * Archivo que hace las llamadas a la API Gateway para que los clientes
 * no tengan que preocuparse por la API de cada microservicio.
 *
 * Propaga automáticamente el token JWT del usuario autenticado en el header
 * Authorization, de modo que los microservicios puedan validar la identidad
 * sin necesidad de pasar el token manualmente en cada llamada.
 */
@Component
public class ApiGatewayClient {

    private final RestTemplate restTemplate;
    private final String apiGatewayUrl;
    private final JwtService jwtService;

    public ApiGatewayClient(
            RestTemplate restTemplate,
            @Value("${api.gateway.url}") String apiGatewayUrl,
            JwtService jwtService
    ) {
        this.restTemplate = restTemplate;
        this.apiGatewayUrl = apiGatewayUrl;
        this.jwtService = jwtService;
    }

    // ── Sobrecargas convenientes que devuelven String ──────────────────────

    public String get(String ruta) {
        return get(ruta, String.class);
    }

    public String post(String ruta, String body) {
        return post(ruta, body, String.class);
    }

    public String put(String ruta, String body) {
        return put(ruta, body, String.class);
    }

    // ── Métodos genéricos ──────────────────────────────────────────────────

    /** GET genérico. Propaga el JWT del usuario autenticado. */
    public <T> T get(String ruta, Class<T> responseType) {
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    construirUrl(ruta),
                    HttpMethod.GET,
                    entity,
                    responseType
            );
            return response.getBody();

        } catch (RestClientResponseException e) {
            throw e;
        } catch (RestClientException e) {
            throw new RestClientException(
                    String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()), e);
        }
    }

    /** POST genérico. Propaga el JWT del usuario autenticado. */
    public <T> T post(String ruta, Object body, Class<T> responseType) {
        return ejecutarConBody(HttpMethod.POST, ruta, body, responseType);
    }

    /** PUT genérico. Propaga el JWT del usuario autenticado. */
    public <T> T put(String ruta, Object body, Class<T> responseType) {
        return ejecutarConBody(HttpMethod.PUT, ruta, body, responseType);
    }

    /** DELETE. Propaga el JWT del usuario autenticado. */
    public String delete(String ruta) {
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    construirUrl(ruta),
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );
            return response.getBody();

        } catch (RestClientResponseException e) {
            throw e;
        } catch (RestClientException e) {
            throw new RestClientException(
                    String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()), e);
        }
    }

    // ── Internos ───────────────────────────────────────────────────────────

    private <T> T ejecutarConBody(HttpMethod method, String ruta, Object body, Class<T> responseType) {
        HttpHeaders headers = buildHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    construirUrl(ruta),
                    method,
                    entity,
                    responseType
            );
            return response.getBody();

        } catch (RestClientResponseException e) {
            throw e;
        } catch (RestClientException e) {
            throw new RestClientException(
                    String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()), e);
        }
    }

    /**
     * Construye los headers base incluyendo Content-Type y,
     * si existe una autenticación activa en el SecurityContext,
     * el header Authorization con el JWT original del usuario.
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Propagar el JWT al microservicio para que pueda validar la identidad
        String token = obtenerTokenActual();
        if (token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            // Inyectar headers de identidad extraídos del JWT validado por el BFF
            // Estos headers son el contrato de identidad entre el BFF y los microservicios
            try {
                Long usuarioId = jwtService.extraerUsuarioId(token);
                String rol = jwtService.extraerRol(token);
                if (usuarioId != null) {
                    headers.set("X-Usuario-Id", String.valueOf(usuarioId));
                }
                if (rol != null) {
                    // X-Rol-Usuario-Id lleva el nombre del rol (String), no un Long
                    headers.set("X-Rol-Usuario-Id", rol);
                }
            } catch (Exception e) {
                // Si falla la extracción, continuamos sin estos headers
            }
        }

        // Propagar adicionalmente las cabeceras personalizadas que vengan del frontend
        // (X-Carrito-Id, X-Idempotency-Key, etc.) sin sobreescribir los que ya pusimos
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest currentRequest = attributes.getRequest();
            Enumeration<String> headerNames = currentRequest.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    if (headerName.toLowerCase().startsWith("x-")
                            && headers.get(headerName) == null) { // no sobreescribir los del JWT
                        headers.set(headerName, currentRequest.getHeader(headerName));
                    }
                }
            }
        }

        return headers;
    }

    /**
     * Recupera el JWT del SecurityContext.
     * El JwtAuthFilter guarda el token raw como credencial del Authentication.
     * Si no hay autenticación activa, devuelve null (rutas públicas).
     */
    private String obtenerTokenActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object credentials = auth.getCredentials();
        if (credentials instanceof String token && !token.isBlank()) {
            return token;
        }
        return null;
    }

    private String construirUrl(String ruta) {
        return apiGatewayUrl + ruta;
    }
}