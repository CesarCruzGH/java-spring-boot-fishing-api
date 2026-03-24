package com.pescayucatan.api_pesca_merida.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ¿Qué orígenes pueden hacer requests?
        // PROD: Solo frontend específico
        // DEV: localhost para testing
        config.setAllowedOrigins(Arrays.asList(
                "https://pesca-merida.com",      // Producción
                "http://localhost:3000",          // React dev server
                "http://localhost:5173"           // Vite dev server
        ));

        // ¿Qué métodos HTTP?
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // ¿Qué headers puede enviar el cliente?
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",  // Para Basic Auth header
                "Content-Type",
                "Accept"
        ));

        // ¿Permitir credentials (cookies, auth headers)?
        config.setAllowCredentials(true);

        // Cache preflight requests por 1 hora
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
