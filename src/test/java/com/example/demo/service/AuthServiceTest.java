package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.client.UsuarioClient;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.dto.usuario.ValidarCredencialesResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioClient, jwtService);
    }

    @Test
    void testLoginExitoso() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        ValidarCredencialesResponse validacion = new ValidarCredencialesResponse(
                true, 1L, "user@example.com", "Juan", "USER", "Credenciales válidas"
        );

        when(usuarioClient.validarCredenciales(
                new com.example.demo.dto.usuario.ValidarCredencialesRequest("user@example.com", "password123")))
                .thenReturn(validacion);
        when(jwtService.generarToken("user@example.com", "USER", 1L)).thenReturn("token_valido");

        LoginResponse response = authService.login(request);

        assertEquals("token_valido", response.token());
    }

    @Test
    void testLoginConCredencialesInvalidas() {
        LoginRequest request = new LoginRequest("user@example.com", "passwordIncorrecto");
        ValidarCredencialesResponse validacion = new ValidarCredencialesResponse(
                false, null, null, null, null, "Credenciales inválidas"
        );

        when(usuarioClient.validarCredenciales(
                new com.example.demo.dto.usuario.ValidarCredencialesRequest("user@example.com", "passwordIncorrecto")))
                .thenReturn(validacion);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void testLoginConUsuarioNoEncontrado() {
        LoginRequest request = new LoginRequest("inexistente@example.com", "password123");

        when(usuarioClient.validarCredenciales(
                new com.example.demo.dto.usuario.ValidarCredencialesRequest("inexistente@example.com", "password123")))
                .thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}
