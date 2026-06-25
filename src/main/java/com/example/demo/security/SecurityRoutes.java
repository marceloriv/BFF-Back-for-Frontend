package com.example.demo.security;

final class SecurityRoutes {

    // clase de rutas para organizar las rutas públicas y protegidas
    private SecurityRoutes() {
    }

    static final String[] PUBLIC_ROUTES = {
            "/auth/login",
            "/api/v1/carrito/**",
            "/api/v1/Carrito/**",
            "/api/v1/causas/activas",
            "/api/v1/organizaciones/activas",
            "/api/v1/eventos",           // Listar eventos - público
            "/api/v1/eventos/**",          // Detalle de evento - público
            "/api/v1/Eventos/**"
    };

    static final String[] PROTECTED_ROUTES = {
            "/api/v1/usuarios/**"
  
    };
}
