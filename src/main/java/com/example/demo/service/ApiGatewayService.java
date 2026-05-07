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
    /**
     *HttpServletRequest es la clase de Jakarta Servlet que representa la solicitud HTTP
      que recibe tu servidor BFF
     . Contiene toda la info de la petición del cliente:
     */
    public String reenviarGet(String microservicio, HttpServletRequest request) {
        String ruta = construirRuta(microservicio, request);
        return apiGatewayClient.get(ruta);
    }

    public String reenviarPost(String microservicio, String body, HttpServletRequest request) {
        String ruta = construirRuta(microservicio, request);
        return apiGatewayClient.post(ruta, body);
    }

    public String reenviarPut(String microservicio, String body, HttpServletRequest request) {
        String ruta = construirRuta(microservicio, request);
        return apiGatewayClient.put(ruta, body);
    }

    public String reenviarDelete(String microservicio, HttpServletRequest request) {
        String ruta = construirRuta(microservicio, request);
        return apiGatewayClient.delete(ruta);
    }

    private String construirRuta(String microservicio, HttpServletRequest request) {
        String uri = request.getRequestURI();

        /*
         * BFF recibe:
         * /api/usuarios/api/v1/usuarios
         * microservicio = usuarios
         * ruta final hacia API Gateway:
         * /usuarios/api/v1/usuarios
         */

        // Elimina "/api/{microservicio}" de la ruta: /api/usuarios/api/v1/usuarios → /api/v1/usuarios
        String rutaDespuesDelMicroservicio = uri.replaceFirst("/api/" + microservicio, "");

        String query = request.getQueryString();

        if (query != null && !query.isBlank()) {
            return "/" + microservicio + rutaDespuesDelMicroservicio + "?" + query;
        }

        return "/" + microservicio + rutaDespuesDelMicroservicio;
    }
}


