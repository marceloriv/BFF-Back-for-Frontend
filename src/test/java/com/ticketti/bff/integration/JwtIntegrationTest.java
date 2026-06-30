package com.ticketti.bff.integration;

import com.ticketti.bff.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de integración para validar la generación y contenido del JWT.
// Se prueba que JwtService genere un token firmado y que tenga los datos esperados.
@SpringBootTest(properties = {
        // Se evita depender del config server durante las pruebas.
        "spring.config.import=optional:",
        "spring.cloud.config.enabled=false",

        // URL de prueba para levantar el contexto del BFF.
        "api.gateway.url=http://localhost:8222",

        // Secreto solo para pruebas JWT.
        "jwt.secret=ticketti-jwt-secret-key-2024-secure-random-256-bit-test",

        // Variables de RabbitMQ de prueba.
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672",
        "spring.rabbitmq.username=guest",
        "spring.rabbitmq.password=guest"
})
@ActiveProfiles("test")
public class JwtIntegrationTest {

    private static final String SECRET_TEST = "ticketti-jwt-secret-key-2024-secure-random-256-bit-test";

    @Autowired
    private JwtService jwtService;

    @Test
    void generarToken_deberiaCrearTokenValido() {
        // Se genera un token como lo haría el BFF después de un login correcto.
        String token = jwtService.generarToken(
                "jwt@ticketti.cl",
                "CLIENTE",
                1L
        );

        // Se valida que el token exista y tenga formato JWT básico.
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generarToken_deberiaContenerCorreoRolYUsuarioId() {
        // Se genera un token con datos conocidos para poder validar sus claims.
        String token = jwtService.generarToken(
                "jwt@ticketti.cl",
                "CLIENTE",
                1L
        );

        // Se usa el mismo secreto de prueba para leer y validar la firma del token.
        SecretKey key = Keys.hmacShaKeyFor(SECRET_TEST.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Se valida el subject del JWT, que debería ser el correo del usuario.
        assertEquals("jwt@ticketti.cl", claims.getSubject());

        // Se valida el rol guardado dentro del token.
        assertEquals("CLIENTE", claims.get("rol", String.class));

        // Se valida el id del usuario guardado dentro del token.
        assertEquals(1L, claims.get("usuarioId", Number.class).longValue());

        // Se valida que el token tenga fecha de emisión y expiración.
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void tokenGenerado_noDeberiaEstarExpiradoAlCrearse() {
        // Se genera un token nuevo.
        String token = jwtService.generarToken(
                "jwt@ticketti.cl",
                "CLIENTE",
                1L
        );

        SecretKey key = Keys.hmacShaKeyFor(SECRET_TEST.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Se valida que la expiración sea posterior a la fecha de emisión.
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }
}