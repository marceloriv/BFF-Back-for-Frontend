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
                        // Listar eventos debe ser público para que el home pueda mostrarlos
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos/listarEventos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios").hasAnyRole("ADMIN", "ADMINPLATAFORMA")
                        // Solo el admin de plataforma puede cambiar roles
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/*/rol").hasRole("ADMINPLATAFORMA")
                        // eventos
                        .requestMatchers(HttpMethod.POST, "/api/v1/eventos")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eventos")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eventos/**")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        // donaciones
                        .requestMatchers(HttpMethod.POST, "/api/v1/donaciones")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/donaciones")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/donaciones/**")
                        .hasAnyRole("ORGANIZADOR", "ADMINPLATAFORMA")
                        // mensajería
                        .requestMatchers(HttpMethod.POST, "/api/v1/mensajeria").hasRole("ADMIN")

                        .requestMatchers(SecurityRoutes.PROTECTED_ROUTES).authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
