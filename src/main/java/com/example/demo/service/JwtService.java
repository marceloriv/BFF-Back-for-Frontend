package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service

public class JwtService {

    // la contraseña se pasa como variable de entorno, no se guarda en el código
    // fuente

    private final String SECRET = "clave-secreta-super-larga-de-minimo-32-caracteres";
    // permite decir cuanta duración va a tener el token, en este caso 15 min
    private final long EXPIRATION = 1000 * 60 * 15; // 15 en milisegundos

    private SecretKey getKey() {

        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String username) {
        // cque va a generar y cuando va a generarlo
        return Jwts.builder().subject(username).issuedAt(new Date()) // acá se coloca lo que se quiere encriptar
                .expiration(new Date(EXPIRATION + System.currentTimeMillis())) // se le dice que el token va a expirar                                                // en 15 min
                .signWith(getKey())
                .compact();

    }

    public String extraerUsername(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload().getSubject(); // se le dice que se va a extraer el username del
                                                                     // token

    }

    public boolean esValido(String token) {

        try {
            extraerUsername(token);
            return true; // si el token es válido, se devuelve true
        } catch (Exception e) {
            return false; // si el token no es válido, se devuelve false
        }

    }

}
