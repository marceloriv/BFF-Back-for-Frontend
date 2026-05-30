package com.example.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/*
 * Archivo que hace las llamadas a la API Gateway para que los clientes
 * no tengan que preocuparse por la API de cada microservicio.
 */
@Component
public class ApiGatewayClient {

    // Inyección de RestTemplate para hacer llamadas HTTP con la URL base de la API Gateway
    private final RestTemplate restTemplate;
    private final String apiGatewayUrl;

    public ApiGatewayClient(
            RestTemplate restTemplate,
            @Value("${api.gateway.url}") String apiGatewayUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiGatewayUrl = apiGatewayUrl;
    }

    /*
     * Sobrecargas convenientes que devuelven String.
     * Internamente delegan a las versiones genéricas usando String.class.
     */
    public String get(String ruta) {
        return get(ruta, String.class);
    }

    public String post(String ruta, String body) {
        return post(ruta, body, String.class);
    }

    public String put(String ruta, String body) {
        return put(ruta, body, String.class);
    }

    /*
     * Método genérico GET.
     * Se usa cuando se espera mapear la respuesta a un DTO.
     */
    public <T> T get(String ruta, Class<T> responseType) {
        try {
            return restTemplate.getForObject(construirUrl(ruta), responseType);

        } catch (RestClientResponseException e) {
            // Si el microservicio respondió con 400, 403, 404, 409, etc.,
            // se relanza tal cual para que el GlobalExceptionHandler respete el status y body original.
            throw e;

        } catch (RestClientException e) {
            // Error real de comunicación: servicio caído, timeout, conexión rechazada, etc.
            throw new RestClientException(
                    String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()),
                    e
            );
        }
    }

    /*
     * Método genérico POST.
     */
    public <T> T post(String ruta, Object body, Class<T> responseType) {
        return ejecutarConBody(HttpMethod.POST, ruta, body, responseType);
    }

    /*
     * Método genérico PUT.
     */
    public <T> T put(String ruta, Object body, Class<T> responseType) {
        return ejecutarConBody(HttpMethod.PUT, ruta, body, responseType);
    }

    /*
     * Método DELETE.
     */
    public String delete(String ruta) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    construirUrl(ruta),
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    String.class
            );

            return response.getBody();

        } catch (RestClientResponseException e) {
            // Respeta errores devueltos por el microservicio.
            throw e;

        } catch (RestClientException e) {
            // Error real de comunicación con API Gateway o microservicio.
            throw new RestClientException(
                    String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()),
                    e
            );
        }
    }

    /*
     * Método reutilizable para POST y PUT.
     */
    private <T> T ejecutarConBody(HttpMethod method, String ruta, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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
            // Si el microservicio respondió con error de negocio,
            // como 400 Bad Request o 409 Conflict, se relanza igual.
            throw e;

        } catch (RestClientException e) {
            // Si no hubo respuesta válida del servicio externo,
            // se considera error de comunicación.
            throw new RestClientException(
                    String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()),
                    e
            );
        }
    }

    /*
     * Construye la URL final usando la URL base de la API Gateway.
     */
    private String construirUrl(String ruta) {
        return apiGatewayUrl + ruta;
    }
}