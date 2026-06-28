package com.ticketti.bff.dto.auth;

//es record porque es una clase que solo contiene atributos para solo trasportar datos, no logica de negocio

public record  LoginRequest (String correo, String contrasena) {

}
