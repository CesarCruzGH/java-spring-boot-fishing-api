package com.pescayucatan.api_pesca_merida.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("$cors.allowed.origins")
    private List<String> allowedOrigins;
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ¿Qué orígenes pueden hacer requests?
        // PROD: Solo frontend específico
        // DEV: localhost para testing
        config.setAllowedOrigins(allowedOrigins);

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
