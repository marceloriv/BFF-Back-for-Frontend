package com.ticketti.bff.client;

import org.springframework.stereotype.Component;

import com.ticketti.bff.dto.usuario.ValidarCredencialesRequest;
import com.ticketti.bff.dto.usuario.ValidarCredencialesResponse;

//cliente para el cmicroservicio de usuarios, delega a ApiGatewayClient para hacer las llamadas HTTP
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
