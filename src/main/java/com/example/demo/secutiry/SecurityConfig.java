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

                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/*/rol").hasRole("ADMINPLATAFORMA")



                        .requestMatchers(SecurityRoutes.PROTECTED_ROUTES).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}

