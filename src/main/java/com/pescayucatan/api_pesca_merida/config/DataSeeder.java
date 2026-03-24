package com.pescayucatan.api_pesca_merida.config;

import com.pescayucatan.api_pesca_merida.model.Role;
import com.pescayucatan.api_pesca_merida.model.User;
import com.pescayucatan.api_pesca_merida.repository.RoleRepository;
import com.pescayucatan.api_pesca_merida.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Constantes para evitar Hardcoding
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

    @Override
    public void run(String... args) {
        try {
            seedAdminUser();
        } catch (Exception e) {
            log.error("Critical error during data seeding: {}", e.getMessage());
        }
    }

    private void seedAdminUser() {
        if (userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            return; // Ya existe, no hacemos nada.
        }

        String adminPassword = System.getenv("ADMIN_PASSWORD");
        if (adminPassword == null || adminPassword.isBlank()) {
            log.error("FATAL: ADMIN_PASSWORD environment variable is missing!");
            return;
        }

        // Buscamos el rol (que ya debió ser creado por Flyway/SQL)
        Role adminRole = roleRepository.findByName(ADMIN_ROLE_NAME)
                .orElseGet(() -> {
                    log.warn("{} not found in DB, creating as fallback.", ADMIN_ROLE_NAME);
                    return roleRepository.save(Role.builder().name(ADMIN_ROLE_NAME).build());                });

        User adminUser = User.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(adminPassword))
                .roles(Set.of(adminRole))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(adminUser);
        log.info("Admin user '{}' successfully seeded.", ADMIN_USERNAME);
    }
}