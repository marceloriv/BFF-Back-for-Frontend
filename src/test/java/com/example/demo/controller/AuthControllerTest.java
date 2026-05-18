package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testLoginExitoso() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse expectedResponse = new LoginResponse("token_jwt_valido");

        when(authService.login(request)).thenReturn(expectedResponse);

        LoginResponse actualResponse = authController.login(request);

        assertEquals(expectedResponse.token(), actualResponse.token());
    }

    @Test
    void testLoginConCredencialesInvalidas() {
        LoginRequest request = new LoginRequest("test@example.com", "passwordIncorrecto");

        when(authService.login(request)).thenThrow(new IllegalArgumentException("Credenciales inválidas"));

        assertThrows(IllegalArgumentException.class, () -> authController.login(request));
    }

    @Test
    void testLoginConUsuarioNoEncontrado() {
        LoginRequest request = new LoginRequest("inexistente@example.com", "password123");

        when(authService.login(request)).thenThrow(new IllegalArgumentException("Credenciales inválidas"));

        assertThrows(IllegalArgumentException.class, () -> authController.login(request));
    }

    @Test
    void testLoginTokenNoNulo() {
        LoginRequest request = new LoginRequest("admin@example.com", "admin123");
        LoginResponse expectedResponse = new LoginResponse("token_jwt_valido_no_nulo");

        when(authService.login(request)).thenReturn(expectedResponse);

        LoginResponse actualResponse = authController.login(request);

        assertNotNull(actualResponse.token(), "El token no debe ser nulo");
    }
}
