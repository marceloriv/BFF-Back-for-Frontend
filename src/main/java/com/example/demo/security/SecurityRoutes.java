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

    // Spring Security evalúa las reglas en orden, de arriba hacia abajo,
    // deteniéndose en la primera que coincide con la petición.
    // Las rutas de donaciones, causas, organizaciones y notificaciones
    // NO se incluyen aquí porque ya tienen reglas específicas definidas
    // en SecurityConfig con hasRole/hasAnyRole por metodo HTTP.
    // Si se agregaran aquí con solo .authenticated(), cualquier usuario
    // logueado podría acceder, ignorando la restricción de rol.
    //rutas protegidas por rol
    static final String[] PROTECTED_ROUTES = {
            "/api/v1/usuarios/**"
            // Nota: /api/v1/eventos/** ya es público (listado/detalle).
            // Las operaciones de escritura (POST, PUT, DELETE) están protegidas
            // por reglas hasRole() en SecurityConfig.
    };
}
