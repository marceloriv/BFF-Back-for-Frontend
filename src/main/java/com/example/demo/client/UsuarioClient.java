package com.example.demo.client;

import org.springframework.stereotype.Component;

import com.example.demo.dto.usuario.ValidarCredencialesRequest;
import com.example.demo.dto.usuario.ValidarCredencialesResponse;

@Component // para que Spring lo detecte como un componente y lo inyecte donde se necesite
public class UsuarioClient {

    private final ApiGatewayClient apiGatewayClient;

    public UsuarioClient(ApiGatewayClient apiGatewayClient) {
        this.apiGatewayClient = apiGatewayClient;
    }

    // método para validar las credenciales del usuario a través de la API, no del microservicio directo
    public ValidarCredencialesResponse validarCredenciales(ValidarCredencialesRequest request) {
        String ruta = "/api/usuarios/api/v1/usuarios/validar-credenciales";
        return apiGatewayClient.post(ruta, request, ValidarCredencialesResponse.class);
    }
}



