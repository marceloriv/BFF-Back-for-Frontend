package com.example.demo.service;

import com.example.demo.client.ApiGatewayClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

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