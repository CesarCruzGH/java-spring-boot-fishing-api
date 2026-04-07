package com.pescayucatan.api_pesca_merida.config;

import com.pescayucatan.api_pesca_merida.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // ════════════════════════════════════════════════════════════
    // FILTER CHAIN (CORE DE SPRING SECURITY)
    // ════════════════════════════════════════════════════════════

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // ────────────────────────────────────────────────────
                // CSRF Protection
                // ────────────────────────────────────────────────────
                // ¿Por qué disabled?
                // - API REST stateless (no cookies)
                // - Basic Auth en cada request (no CSRF vector)
                // - Frontend consumirá vía CORS (no form-based)
                .csrf(csrf -> csrf.disable())

                // ────────────────────────────────────────────────────
                // CORS Configuration
                // ────────────────────────────────────────────────────
                // Inyecta config desde CorsConfig.java
                .cors(Customizer.withDefaults())

                // ────────────────────────────────────────────────────
                // AUTHORIZATION RULES
                // ────────────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // Administrativos → ROLE_ADMIN
                        .requestMatchers("/api/v1/ingestion/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/peces")
                        .hasRole("ADMIN")
                        .requestMatchers("/actuator/metrics", "/actuator/scheduledtasks")
                        .hasRole("ADMIN")
                        .requestMatchers("/h2-console/**").hasRole("ADMIN")
                        // Públicos → permitAll
                        .requestMatchers("/peces/**", "/api/v1/ingestion/health")
                        .permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info")
                        .permitAll()
                        .requestMatchers("/api/v1/regulaciones", "/api/v1/regulaciones/pez/**")
                        .permitAll()
                        // Swagger UI (si se implementa)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()

                        // Resto → autenticado (pero sin rol específico)
                        .anyRequest()
                        .authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // 3. ¡ESTA ES LA CLAVE!
                )
                // ────────────────────────────────────────────────────
                // HTTP BASIC AUTHENTICATION
                // ────────────────────────────────────────────────────
                .httpBasic(Customizer.withDefaults())

                // ────────────────────────────────────────────────────
                // SESSION MANAGEMENT
                // ────────────────────────────────────────────────────
                // Stateless → no crea HttpSession
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ════════════════════════════════════════════════════════════
    // PASSWORD ENCODER
    // ════════════════════════════════════════════════════════════

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt con strength 12 (default 10)
        // Cada hash toma ~300ms → protege contra brute force
        return new BCryptPasswordEncoder(12);
    }
}