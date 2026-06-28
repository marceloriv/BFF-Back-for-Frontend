package com.ticketti.bff.service;

import org.springframework.stereotype.Service;

import com.ticketti.bff.client.UsuarioClient;
import com.ticketti.bff.dto.auth.LoginRequest;
import com.ticketti.bff.dto.auth.LoginResponse;
import com.ticketti.bff.dto.usuario.ValidarCredencialesRequest;
import com.ticketti.bff.dto.usuario.ValidarCredencialesResponse;

@Service
public class AuthService {

    private final UsuarioClient usuarioClient;
    private final JwtService jwtService;

    public AuthService(UsuarioClient usuarioClient, JwtService jwtService) {
        this.usuarioClient = usuarioClient;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.correo() == null || request.contrasena() == null ||
            request.correo().trim().isEmpty() || request.contrasena().trim().isEmpty()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        // Validación de formato de email básico para evitar peticiones mal formadas a la red interna
        if (!request.correo().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        ValidarCredencialesRequest validarRequest = new ValidarCredencialesRequest(
                request.correo().trim(),
                request.contrasena());

        ValidarCredencialesResponse usuarioValidado = usuarioClient.validarCredenciales(validarRequest);

        if (usuarioValidado == null || !usuarioValidado.valido()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtService.generarToken(usuarioValidado.correo(), usuarioValidado.rol(), usuarioValidado.usuarioId());
        return new LoginResponse(token);
    }
}
