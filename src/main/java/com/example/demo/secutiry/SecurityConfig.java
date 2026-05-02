package com.example.demo.secutiry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration

public class SecurityConfig {
    @Autowired
    private JwtAuthFilter filtro;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http ) throws Exception {
        //configuraciones de seguridad, como deshabilitar csrf, configurar rutas publicas y privadas de forma personalizada
        return http.csrf( csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();


    }

}