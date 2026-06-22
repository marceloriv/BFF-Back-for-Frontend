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
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService servicio;

    private static final List<String> PUBLIC_ROUTES = Arrays.asList(
        "/auth/",
        "/api/v1/eventos",
        "/api/v1/Eventos",
        "/api/v1/causas/activas",
        "/api/v1/organizaciones"
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
        log.debug("Authorization header: {}", header != null ? header.substring(0, Math.min(20, header.length())) + "..." : "null");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.debug("Token extracted, validating...");

            if (servicio.esValido(token)) {
                String correo = servicio.extraerCorreo(token);
                String rol = servicio.extraerRol(token);
                log.debug("Token valid. User: {}, Role: {}", correo, rol);

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
                            token,
                            authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Authentication set for user: {} with roles: {}", correo, authorities);
                }
            } else {
                log.warn("Invalid JWT token received");
            }
        } else {
            log.debug("No Authorization header or does not start with Bearer");
        }

        filterChain.doFilter(request, response);
    }
}
