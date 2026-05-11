package com.example.demo.dto.usuario;

public record ValidarCredencialesRequest(
        String correo,
        String contrasena
) {
}