package com.ticketti.bff.dto.usuario;

public record ValidarCredencialesRequest(
        String correo,
        String contrasena

) {
}
