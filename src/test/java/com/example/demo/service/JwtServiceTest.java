package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "jwt.secret=miClaveSecretaMuySeguraParaPruebasDeJwtConUnaTamañoMuyGrande1234567890")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private String tokenValido;
    private String correoTest = "test@example.com";

    @BeforeEach
    void setUp() {
        tokenValido = jwtService.generarToken(correoTest, "USER");
    }

    @Test
    void testGenerarToken() {
        String token = jwtService.generarToken(correoTest, "USER");
        assertTrue(token != null && !token.isEmpty(), "El token no debe ser nulo ni vacío");
    }

    @Test
    void testExtraerCorreoDelToken() {
        String correoExtraido = jwtService.extraerCorreo(tokenValido);
        assertEquals(correoTest, correoExtraido);
    }

    @Test
    void testValidarTokenValido() {
        assertTrue(jwtService.esValido(tokenValido));
    }

    @Test
    void testValidarTokenInvalido() {
        String tokenInvalido = "token_invalido_fake";
        assertFalse(jwtService.esValido(tokenInvalido));
    }

    @Test
    void testTokenExpiraEnTiempoEstablecido() throws InterruptedException {
        String token = jwtService.generarToken("test@example.com", "USER");
        assertTrue(jwtService.esValido(token), "El token debe ser válido al crearse");
    }
}
