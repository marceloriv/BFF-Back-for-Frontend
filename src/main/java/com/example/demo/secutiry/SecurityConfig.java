package com.example.demo.secutiry;

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
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/*/rol").hasRole("ADMINPLATAFORMA")
                        //eventos
                        .requestMatchers(HttpMethod.POST, "/api/v1/eventos").hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eventos").hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/**").hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")

                        // ══════════════════════════════════════════════════════
                        // ORGANIZACIONES (ms-donaciones)
                        .requestMatchers(HttpMethod.POST, "/api/v1/organizaciones").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Subir documento de convenio (ORGANIZADOR al crear la org)
                        .requestMatchers(HttpMethod.POST, "/api/v1/organizaciones/*/documento").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Activar organización pendiente: SOLO el admin puede aprobar
                        .requestMatchers(HttpMethod.PUT, "/api/v1/organizaciones/*/activar").hasRole("ADMINPLATAFORMA")

                        // Editar datos de una organización: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.PUT, "/api/v1/organizaciones/**").hasRole("ADMINPLATAFORMA")

                        // Desactivar (borrado lógico): solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/organizaciones/**").hasRole("ADMINPLATAFORMA")

                        // Consultar organizaciones: ADMINPLATAFORMA ve todas Organizador las activas
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizaciones/**").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // ══════════════════════════════════════════════════════
                        // CAUSAS SOCIALES (ms-donaciones)

                        // Crear causa: ORGANIZADOR (pendiente) o ADMINPLATAFORMA (activa)
                        .requestMatchers(HttpMethod.POST, "/api/v1/causas").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Desactivar causa: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/causas/**").hasRole("ADMINPLATAFORMA")

                        // Ver causas por organización: ADMINPLATAFORMA y ORGANIZADOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/organizacion/**").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // Buscar causa por ID: ADMINPLATAFORMA y ORGANIZADOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/causas/**").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")

                        // ══════════════════════════════════════════════════════
                        // DONACIONES (ms-donaciones) MSDonaciones NO recibe POST desde el BFF.
                        // Las donaciones se crean internamente cuando RabbitMQ entrega el evento "pago.confirmado" desde MSCarrito.
                        // Reportes de donaciones: ADMINPLATAFORMA ve todo,
                        // ORGANIZADOR ve solo las de las causas sociales asociadas a sus eventos
                        // (el filtrado por org se hace dentro del microservicio)
                        .requestMatchers(HttpMethod.GET, "/api/v1/donaciones/**").hasAnyRole("ADMINPLATAFORMA", "ORGANIZADOR")


                        // ══════════════════════════════════════════════════════
                        // 5. MENSAJERÍA (ms-mensajeria)

                        // Envíos manuales (reenvío de ticket, devolución, recordatorio, recomendación): solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.POST, "/api/v1/notificaciones/**").hasRole("ADMINPLATAFORMA")

                        // Cancelar notificación pendiente: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/notificaciones/cancelar/**").hasRole("ADMINPLATAFORMA")

                        // Historial de notificaciones de un usuario:
                        // COMPRADOR ve el suyo, ADMINPLATAFORMA ve cualquiera
                        .requestMatchers(HttpMethod.GET, "/api/v1/notificaciones/historial/**").hasAnyRole("ADMINPLATAFORMA", "COMPRADOR")

                        // Obtener notificación por ID: solo ADMINPLATAFORMA
                        .requestMatchers(HttpMethod.GET, "/api/v1/notificaciones/obtener/**").hasRole("ADMINPLATAFORMA")


                        .requestMatchers(SecurityRoutes.PROTECTED_ROUTES).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}

