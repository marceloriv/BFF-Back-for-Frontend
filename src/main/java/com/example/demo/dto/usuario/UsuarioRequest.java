package com.example.demo.dto.usuario;

public record UsuarioRequest(
	String nombre,
	String correo,
	String contrasena,
	String direccion,
	String telefono,
	String rol) {

}
