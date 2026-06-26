package com.example.demo.security;

final class SecurityRoutes {

    // clase de rutas para organizar las rutas públicas y protegidas
    private SecurityRoutes() {
    }

    // Rutas realmente públicas. que un usuario puede ver sin iniciar sesión
    // Esta clase no distingue método HTTP, solo la ruta.
    static final String[] PUBLIC_ROUTES = {
            // Usuarios 
            "/auth/login",

            // Causas sociales públicas
            "/api/v1/causas/activas",

    };

    // Rutas generales protegidas.
    // Sirven como respaldo para que todo microservicio quede privado por defecto.
    // Las reglas más específicas por rol van en SecurityConfig ANTES de usar este arreglo.
    static final String[] PROTECTED_ROUTES = {
            // Usuarios
            "/api/v1/usuarios",
            "/api/v1/usuarios/**",

            // Eventos
            "/api/v1/eventos",
            "/api/v1/eventos/**",
            "/api/v1/Eventos/**",

            // Organizaciones
            "/api/v1/organizaciones",
            "/api/v1/organizaciones/",
            "/api/v1/organizaciones/**",

            // Causas sociales
            "/api/v1/causas",
            "/api/v1/causas/**",

            // Donaciones
            "/api/v1/donaciones/**",

            // Notificaciones / mensajería
            "/api/v1/notificaciones/**",

            // Carrito
            "/api/v1/carrito/**",
            "/api/v1/Carrito/**"
    };
}
