package com.example.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.usuario.ValidarCredencialesRequest;
import com.example.demo.dto.usuario.ValidarCredencialesResponse;

@Component // para que Spring lo detecte como un componente y lo inyecte donde se necesite
public class UsuarioClient {

    private final RestTemplate restTemplate;
    private final String usuariosUrl;


    // inyectamos el RestTemplate para hacer las llamadas HTTP y la URL del servicio de usuarios a través del constructor

    public UsuarioClient(
            RestTemplate restTemplate,
            @Value("${servicios.usuarios.url}") String usuariosUrl // inyectamos la URL del servicio de usuarios desde properties
    ) {
        this.restTemplate = restTemplate;
        this.usuariosUrl = usuariosUrl;
    }
    // método para validar las credenciales del usuario llamando al servicio de usuarios
    public ValidarCredencialesResponse validarCredenciales(ValidarCredencialesRequest request) {
        String url = usuariosUrl + "/api/v1/usuarios/validar-credenciales";

        return restTemplate.postForObject(url, request, ValidarCredencialesResponse.class);
    }
}



