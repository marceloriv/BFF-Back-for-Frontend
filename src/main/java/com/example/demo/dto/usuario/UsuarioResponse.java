package com.example.demo.dto.usuario;

public record UsuarioResponse(
	Long id,
	String nombre,
	String correo,
	String direccion,
	String telefono,
	String rol) {

}
