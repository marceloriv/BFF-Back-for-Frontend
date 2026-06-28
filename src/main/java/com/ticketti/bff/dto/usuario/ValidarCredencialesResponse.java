package com.ticketti.bff.dto.usuario;

public record ValidarCredencialesResponse(
        boolean valido,
        Long usuarioId,
        String correo,
        String nombre,
        String rol,
        String mensaje
) {
}
