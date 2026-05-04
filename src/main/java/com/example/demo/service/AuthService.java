package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.client.UsuarioClient;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.dto.usuario.ValidarCredencialesRequest;
import com.example.demo.dto.usuario.ValidarCredencialesResponse;

@Service
public class AuthService {

    private final UsuarioClient usuarioClient;
    private final JwtService jwtService;

    public AuthService(UsuarioClient usuarioClient, JwtService jwtService) {
        this.usuarioClient = usuarioClient;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        ValidarCredencialesRequest validarRequest = new ValidarCredencialesRequest(
                request.correo(),
                request.contrasena()
        );

        ValidarCredencialesResponse usuarioValidado =
                usuarioClient.validarCredenciales(validarRequest);

        if (usuarioValidado == null || !usuarioValidado.valido()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtService.generarToken(usuarioValidado.correo());
        return new LoginResponse(token);
    }
}
