package com.ticketti.bff.service;

import com.ticketti.bff.client.ApiGatewayClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class ApiGatewayService {

    private final ApiGatewayClient apiGatewayClient;

    public ApiGatewayService(ApiGatewayClient apiGatewayClient) {
        this.apiGatewayClient = apiGatewayClient;
    }

    public String reenviarGet(String microservicio, HttpServletRequest request) {
        String ruta = construirRuta(request);
        return apiGatewayClient.get(ruta);
    }

    public String reenviarPost(String microservicio, String body, HttpServletRequest request) {
        String ruta = construirRuta(request);
        return apiGatewayClient.post(ruta, body);
    }

    /**
     * Reenvía una petición multipart/form-data (subida de archivos) sin
     * pasar por el camino de @RequestBody String: leer un body binario
     * como String lo corrompe y rompe el boundary del multipart antes de
     * reenviarlo. Aquí se leen las partes originales (archivos y campos de
     * texto) y se reconstruyen como un nuevo multipart hacia el Gateway.
     */
    public String reenviarPostMultipart(String microservicio, HttpServletRequest request) {
        String ruta = construirRuta(request);
        MultiValueMap<String, Object> body = extraerPartesMultipart(request);
        return apiGatewayClient.postMultipart(ruta, body);
    }

    private MultiValueMap<String, Object> extraerPartesMultipart(HttpServletRequest request) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            for (Part part : request.getParts()) {
                String filename = part.getSubmittedFileName();
                if (filename != null && !filename.isBlank()) {
                    body.add(part.getName(), partAResource(part, filename));
                } else {
                    body.add(part.getName(), leerContenido(part));
                }
            }
        } catch (IOException | ServletException e) {
            throw new IllegalStateException("No se pudo leer la petición multipart", e);
        }
        return body;
    }

    private ByteArrayResource partAResource(Part part, String filename) throws IOException {
        try (InputStream in = part.getInputStream()) {
            byte[] contenido = StreamUtils.copyToByteArray(in);
            return new ByteArrayResource(contenido) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
        }
    }

    private String leerContenido(Part part) throws IOException {
        try (InputStream in = part.getInputStream()) {
            return new String(StreamUtils.copyToByteArray(in), StandardCharsets.UTF_8);
        }
    }

    public String reenviarPut(String microservicio, String body, HttpServletRequest request) {
        String ruta = construirRuta(request);
        return apiGatewayClient.put(ruta, body);
    }

    public String reenviarDelete(String microservicio, HttpServletRequest request) {
        String ruta = construirRuta(request);
        return apiGatewayClient.delete(ruta);
    }

    private String construirRuta(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        if (query != null && !query.isBlank()) {
            return uri + "?" + query;
        }

        return uri;
    }
}
