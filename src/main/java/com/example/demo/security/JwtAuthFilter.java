package com.example.demo.security;

import java.io.IOException;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);
            if (servicio.esValido(token)) {
                // Si el token es válido, se puede configurar la autenticación en el contexto de
                // seguridad
                // Aquí podrías extraer el username y roles del token y establecer la
                // autenticación

                String correo = servicio.extraerCorreo(token);

                String rol = servicio.extraerRol(token);
                // se extrae el rol que viene dentro del jwt, se guarda en una variable y se agega a una lista para que spring Security pueda filtrar si es que el usuario tiene permiso para acceder a una ruta protegida
                //ROLE_ = para que springSecurity reconozca el rol por conversión automatica
                if (rol != null && !rol.isBlank()) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            correo,
                            token,   // guardamos el token raw para que ApiGatewayClient lo propague
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol.trim().toUpperCase())));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

        }

        filterChain.doFilter(request, response);
    }

}
