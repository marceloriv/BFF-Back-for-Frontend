package com.example.demo.dto.usuario;

public record ValidarCredencialesResponse(
        boolean valido,
        Long usuarioId,
        String correo,
        String nombre,
        String rol,
        String mensaje
) {
}

