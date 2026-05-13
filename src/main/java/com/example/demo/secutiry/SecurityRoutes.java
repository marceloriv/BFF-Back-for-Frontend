package com.example.demo.secutiry;

final class SecurityRoutes {

    // clase de rutas para organizar las rutas públicas y protegidas
    private SecurityRoutes() {
    }

    static final String[] PUBLIC_ROUTES = {
            "/auth/login",
            "/api/v1/usuarios/**"
    };

    static final String[] PROTECTED_ROUTES = {
                "/api/v1/eventos/**",
                "/api/v1/pedidos/**"
            

    };
}



