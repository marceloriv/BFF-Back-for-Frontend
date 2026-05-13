package com.example.demo.client;

import org.springframework.stereotype.Component;

import com.example.demo.dto.usuario.ValidarCredencialesRequest;
import com.example.demo.dto.usuario.ValidarCredencialesResponse;

@Component
public class UsuarioClient {

    private final ApiGatewayClient apiGatewayClient;

    public UsuarioClient(ApiGatewayClient apiGatewayClient) {
        this.apiGatewayClient = apiGatewayClient;
    }

    // Valida credenciales a través de la API Gateway
    public ValidarCredencialesResponse validarCredenciales(ValidarCredencialesRequest request) {
        String ruta = "/api/v1/usuarios/validar-credenciales";
        return apiGatewayClient.post(ruta, request, ValidarCredencialesResponse.class);
    }
}