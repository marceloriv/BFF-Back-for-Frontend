package com.ticketti.bff.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtAuthFilter filtro;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(SecurityRoutes.PUBLIC_ROUTES).permitAll()

                        // ══════════════════════════════════════════════════════
                        // USUARIOS (ms-usuarios)

                        // Crear usuario: POST público (registro)
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/").permitAll()

                        // Login público
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/login/").permitAll()

                        // Validar credenciales público
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/validar-credenciales").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/validar-credenciales/").permitAll()

                        // Listar todos los usuarios: solo ADMIN o ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios")
                        .hasAnyRole("ADMIN", "ADMINPLATAFORMA")

                        // Solo el admin de plataforma puede cambiar roles
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/usuarios/*/rol")
                        .hasRole("ADMINPLATAFORMA")

                        // Eliminar usuario:

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/*")
                        .authenticated()

                        // Obtener usuario por ID: requiere estar autenticado
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/*")
                        .authenticated()

                        // Actualizar usuario por ID: requiere estar autenticado
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/*")
                        .authenticated()

                        // ══════════════════════════════════════════════════════
                        // EVENTOS (ms-eventos)

                        // requiere autenticación para ver mis eventos (mis eventos creados o en los que
                        // estoy registrado)
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos/mis").authenticated()

                        // Consultar eventos: GET público,
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos/**").permitAll()

                        // POST, PUT y DELETE requieren autenticación y roles específicos
                        .requestMatchers(HttpMethod.POST, "/api/v1/eventos/**")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eventos/**")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/actualizarStock/**")
                        .hasAnyRole("CLIENTE", "ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/restaurarStock/**")
                        .hasAnyRole("CLIENTE", "ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/*/estado").hasRole("ORGANIZADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/**")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")

                        // ══════════════════════════════════════════════════════
                        // ORGANIZACIONES (ms-donaciones)

                        // (PUBLICO ) Consultar organizaciones
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizaciones").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizaciones/").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/organizaciones")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Subir documento de convenio (ORGANIZADOR al crear la org)
                        .requestMatchers(HttpMethod.POST, "/api/v1/organizaciones/*/documento")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Activar organización pendiente: SOLO el admin puede aprobar
                        .requestMatchers(HttpMethod.PUT, "/api/v1/organizaciones/*/activar").hasRole("ADMINPLATAFORMA")

                        // Editar datos de una organización: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.PUT, "/api/v1/organizaciones/**").hasRole("ADMINPLATAFORMA")

                        // Desactivar (borrado lógico): solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/organizaciones/**").hasRole("ADMINPLATAFORMA")
                        // Consultar organizaciones: ADMINPLATAFORMA ve todas, ORGANIZADOR ve solo las
                        // activas
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizaciones/**")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // ══════════════════════════════════════════════════════
                        // CAUSAS SOCIALES (ms-donaciones)

                        // (PUBLICO ) Consultar causas sociales activas:
                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/activas").permitAll()

                        // (PUBLICO ) Detalle de una causa por ID: necesario para mostrar
                        // la causa vinculada en DetalleEvento y auto-seleccionarla en el
                        // carrito. La causa individual (nombre, org, objetivo) no es info
                        // sensible — es tan pública como el listado de activas.
                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/*").permitAll()

                        // Crear causa: ORGANIZADOR (pendiente) o ADMINPLATAFORMA (activa)
                        .requestMatchers(HttpMethod.POST, "/api/v1/causas").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Desactivar causa: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/causas/**").hasRole("ADMINPLATAFORMA")

                        // Ver causas por organización: ADMINPLATAFORMA y ORGANIZADOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/organizacion/**")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Listar todas (admin): ADMINPLATAFORMA y ORGANIZADOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/**")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // ══════════════════════════════════════════════════════
                        // DONACIONES (ms-donaciones) MSDonaciones NO recibe POST desde el BFF.
                        // Las donaciones se crean internamente cuando RabbitMQ entrega el evento
                        // "pago.confirmado" desde MSCarrito.

                        // Mis donaciones: accesible para cualquier usuario autenticado (CLIENTE
                        // incluido)
                        .requestMatchers(HttpMethod.GET, "/api/v1/donaciones/me").authenticated()
                        // Reportes de donaciones: ADMINPLATAFORMA ve todo,
                        // ORGANIZADOR ve solo las de las causas sociales asociadas a sus eventos
                        // (el filtrado por org se hace dentro del microservicio)
                        .requestMatchers(HttpMethod.GET, "/api/v1/donaciones/**")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // ══════════════════════════════════════════════════════
                        // NOTIFICACIONES (ms-mensajeria)

                        // Historial de notificaciones de un usuario:
                        // Requiere autenticación por seguridad (evita fugas IDOR / BOLA)
                        .requestMatchers(HttpMethod.GET, "/api/v1/notificaciones/historial/**").authenticated()

                        // Obtener notificación por ID: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.GET, "/api/v1/notificaciones/obtener/**").hasRole("ADMINPLATAFORMA")

                        // Formulario de contacto público
                        .requestMatchers(HttpMethod.POST, "/api/v1/notificaciones/contacto").permitAll()

                        // Envíos manuales (reenvío de ticket, devolución, recordatorio, recomendación):
                        // solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.POST, "/api/v1/notificaciones/**").hasRole("ADMINPLATAFORMA")

                        // Cancelar notificación pendiente: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/notificaciones/cancelar/**")
                        .hasRole("ADMINPLATAFORMA")

                        // ══════════════════════════════════════════════════════
                        // CARRITO (ms-carrito)

                        // Rutas que requieren autenticación
                        .requestMatchers(HttpMethod.POST, "/api/v1/Carrito/webhooks/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/Carrito/checkout/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/Carrito/renovar/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/Carrito/pago-manual/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/Carrito/devoluciones/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/Carrito/listar").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/Carrito/estadisticas").authenticated()

                        // Rutas públicas: guest puede operar con cartId
                        .requestMatchers(HttpMethod.POST, "/api/v1/Carrito/crear").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/Carrito/obtener/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/Carrito/resumen/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/Carrito/*/entradas").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/Carrito/*/entradas/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/Carrito/actualizar/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/Carrito/vaciar/**").permitAll()

                        // Respaldo: cualquier otra ruta de carrito queda autenticada
                        .requestMatchers("/api/v1/Carrito/**").authenticated()
                        .requestMatchers("/api/v1/carrito/**").authenticated()

                        // ══════════════════════════════════════════════════════

                        .requestMatchers(SecurityRoutes.PROTECTED_ROUTES).authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://front-ticketti-fullstacks3-2026.s3-website-us-east-1.amazonaws.com"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        config.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept", "X-Usuario-Id", "X-Rol-Usuario-Id"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
