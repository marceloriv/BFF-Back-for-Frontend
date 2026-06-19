package com.example.demo.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component

public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService servicio;

    // Rutas públicas que no requieren validación de JWT
    private static final List<String> PUBLIC_ROUTES = Arrays.asList(
        "/api/v1/Carrito/",
        "/api/v1/carrito/",
        "/api/v1/eventos",
        "/api/v1/Eventos",
        "/api/v1/causas/activas",
        "/api/v1/organizaciones/activas",
        "/api/v1/usuarios",
        "/auth/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_ROUTES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("[JWT Filter] Authorization header: " + (header != null ? header.substring(0, Math.min(20, header.length())) + "..." : "null"));

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);
            System.out.println("[JWT Filter] Token extracted, validating...");
            if (servicio.esValido(token)) {
                System.out.println("[JWT Filter] Token is valid");
                // Si el token es válido, se puede configurar la autenticación en el contexto de
                // seguridad
                // Aquí podrías extraer el username y roles del token y establecer la
                // autenticación

                String correo = servicio.extraerCorreo(token);
                System.out.println("[JWT Filter] Correo extraído: " + correo);

                String rol = servicio.extraerRol(token);
                System.out.println("[JWT Filter] Rol extraído: " + rol);
                // se extrae el rol que viene dentro del jwt, se guarda en una variable y se agega a una lista para que spring Security pueda filtrar si es que el usuario tiene permiso para acceder a una ruta protegida
                //ROLE_ = para que springSecurity reconozca el rol por conversión automatica
                if (rol != null && !rol.isBlank()) {
                    String cleanRol = rol.trim().toUpperCase();
                    List<SimpleGrantedAuthority> authorities;
                    if ("ADMINPLATAFORMA".equals(cleanRol)) {
                        authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_ADMINPLATAFORMA"),
                            new SimpleGrantedAuthority("ROLE_ORGANIZADOR"),
                            new SimpleGrantedAuthority("ROLE_CLIENTE")
                        );
                    } else if ("ORGANIZADOR".equals(cleanRol)) {
                        authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_ORGANIZADOR"),
                            new SimpleGrantedAuthority("ROLE_CLIENTE")
                        );
                    } else {
                        authorities = List.of(new SimpleGrantedAuthority("ROLE_" + cleanRol));
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            correo,
                            token,   // guardamos el token raw para que ApiGatewayClient lo propague
                            authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("[JWT Filter] Authentication set for user: " + correo + " with roles: " + authorities);
                }
            } else {
                System.out.println("[JWT Filter] Token is invalid");
            }

        } else {
            System.out.println("[JWT Filter] No Authorization header or does not start with Bearer");
        }

        filterChain.doFilter(request, response);
    }

}
