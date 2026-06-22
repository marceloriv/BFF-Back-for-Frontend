package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtAuthFilter filtro;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityRoutes.PUBLIC_ROUTES).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios").hasAnyRole("ADMIN", "ADMINPLATAFORMA")
                        // Solo el admin de plataforma puede cambiar roles
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/usuarios/*/rol").hasRole("ADMINPLATAFORMA")

                        // ══════════════════════════════════════════════════════
                        // EVENTOS (ms-eventos)
                        .requestMatchers(HttpMethod.POST, "/api/v1/eventos").hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eventos").hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/actualizarStock/**").hasAnyRole("CLIENTE", "ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/restaurarStock/**").hasAnyRole("CLIENTE", "ORGANIZADOR", "ADMINPLATAFORMA")

                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/*/estado").hasRole("ORGANIZADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/**").hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eventos/**").hasRole("ADMINPLATAFORMA")

                        // Stock eventos
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/actualizarStock/*/*").hasRole("ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/restaurarStock/*/*").hasRole("ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos/stock/*").hasRole("ADMINPLATAFORMA")

                        // ══════════════════════════════════════════════════════
                        // ORGANIZACIONES (ms-donaciones)
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

                        // Consultar organizaciones: ADMINPLATAFORMA ve todas Organizador las activas
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizaciones").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizaciones/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizaciones/**")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // ══════════════════════════════════════════════════════
                        // CAUSAS SOCIALES (ms-donaciones)

                        // Crear causa: ORGANIZADOR (pendiente) o ADMINPLATAFORMA (activa)
                        .requestMatchers(HttpMethod.POST, "/api/v1/causas").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Desactivar causa: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/causas/**").hasRole("ADMINPLATAFORMA")

                        // Ver causas por organización: ADMINPLATAFORMA y ORGANIZADOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/organizacion/**")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/activas").permitAll()
                        // Buscar causa por ID: ADMINPLATAFORMA y ORGANIZADOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/**")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // ══════════════════════════════════════════════════════
                        // DONACIONES (ms-donaciones) MSDonaciones NO recibe POST desde el BFF.
                        // Las donaciones se crean internamente cuando RabbitMQ entrega el evento
                        // "pago.confirmado" desde MSCarrito.
                        // Reportes de donaciones: ADMINPLATAFORMA ve todo,
                        // ORGANIZADOR ve solo las de las causas sociales asociadas a sus eventos
                        // (el filtrado por org se hace dentro del microservicio)
                        .requestMatchers(HttpMethod.GET, "/api/v1/donaciones/**")
                        .hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Historial de notificaciones de un usuario:
                        // Requiere autenticación por seguridad (evita fugas IDOR / BOLA)
                        .requestMatchers(HttpMethod.GET, "/api/v1/notificaciones/historial/**").authenticated()

                        // Obtener notificación por ID: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.GET, "/api/v1/notificaciones/obtener/**").hasRole("ADMINPLATAFORMA")

                        // Envíos manuales (reenvío de ticket, devolución, recordatorio, recomendación):
                        // solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.POST, "/api/v1/notificaciones/**").hasRole("ADMINPLATAFORMA")

                        // Cancelar notificación pendiente: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/notificaciones/cancelar/**")
                        .hasRole("ADMINPLATAFORMA")

                        // ══════════════════════════════════════════════════════
                        // CARRITO (ms-carrito)
                        // Todo requiere autenticación (incluido crear, para capturar usuarioId del JWT)
                        .requestMatchers("/api/v1/Carrito/**").authenticated()
                        .requestMatchers("/api/v1/carrito/**").authenticated()

                        // ══════════════════════════════════════════════════════
                        // USUARIOS (ms-usuarios) — rutas que requieren auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/**").authenticated()

                        .requestMatchers(SecurityRoutes.PROTECTED_ROUTES).authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
