---
title: Security Model - API Pesca Mérida
date: 2026-04-01
project_type: Java (Spring Boot 3.5.11)
architecture_pattern: Layered with Spring Security 6
detail_level: Comprehensive
includes_implementation_patterns: true
includes_code_examples: true
---

# Security Model - API Pesca Yucatán

## 1. Architecture Detection and Analysis

### Technology Stack
- **Framework**: Spring Boot 3.5.11
- **Security**: Spring Security 6.x
- **Password Encoding**: BCrypt (strength 12)
- **Authentication**: HTTP Basic Auth
- **Session**: Stateless (SessionCreationPolicy.STATELESS)
- **CORS**: Configurable via CorsConfig.java

### Architecture Pattern
El proyecto implementa una arquitectura de seguridad en capas con los siguientes componentes:

```
SecurityConfig (Filter Chain) → CustomUserDetailsService → UserRepository → Database
                ↓
         CorsConfig (CORS Policy)
```

---

## 2. Security Architecture Overview

### 2.1 High-Level Security Flow

```mermaid
flowchart TD
    subgraph Client
        REQ[Request]
    end
    
    subgraph Security_Layer
        CORS[CorsConfig<br/>Origin Validation]
        CSRF[CSRF Disabled<br/>Stateless API]
        AUTH[HTTP Basic Auth<br/>Header Validation]
        FILT[SecurityFilterChain<br/>Route Matching]
    end
    
    subgraph Authentication
        PROVIDER[DaoAuthenticationProvider]
        ENCODER[BCrypt Password Encoder]
        DETAILS[CustomUserDetailsService]
        REPO[UserRepository]
    end
    
    subgraph Authorization
        RBAC[Role-Based Access Control<br/>hasRole/permitAll]
    end
    
    subgraph Backend
        CONT[Controllers]
        SVC[Services]
        DB[(Database)]
    end
    
    REQ --> CORS
    CORS --> CSRF
    CSRF --> AUTH
    AUTH --> FILT
    FILT --> PROVIDER
    PROVIDER --> ENCODER
    ENCODER --> DETAILS
    DETAILS --> REPO
    REPO --> DB
    FILT --> RBAC
    RBAC --> CONT
    CONT --> SVC
    SVC --> DB
```

### 2.2 Request Flow - Authentication & Authorization

```mermaid
sequenceDiagram
    participant Client
    participant Filter as SecurityFilterChain
    participant Provider as DaoAuthenticationProvider
    participant Encoder as BCryptPasswordEncoder
    participant UserService as CustomUserDetailsService
    participant DB as Database
    participant Controller
    
    Client->>Filter: GET /api/v1/ingestion/status<br/>Authorization: Basic dXNlcjpwYXNz
    
    Note over Filter: CORS validado en entrada
    
    Filter->>Provider: Buscar usuario "user"
    Provider->>UserService: loadUserByUsername("user")
    UserService->>DB: SELECT * FROM users WHERE username = ?
    DB-->>UserService: User entity (password hash)
    
    Note over Encoder: Comparar password plano vs hash BCrypt
    Encoder-->>Provider: Password match: true/false
    
    alt Autenticación exitosa
        Provider-->>Filter: Authentication success
        Filter->>Filter: Verificar authorization (hasRole)
        
        alt Authorized
            Filter->>Controller: Permit request
            Controller-->>Client: 200 OK + Data
        else Not authorized
            Filter-->>Client: 403 Forbidden
        end
    else Autenticación fallida
        Provider-->>Filter: Authentication failure
        Filter-->>Client: 401 Unauthorized
    end
```

---

## 3. Core Security Components

### 3.1 SecurityConfig.java - Filter Chain Configuration

**Ubicación**: `src/main/java/com/pescayucatan/api_pesca_merida/config/SecurityConfig.java`

**Responsabilidades**:
1. Configurar SecurityFilterChain (Spring Security 6)
2. Definir reglas de autorización por ruta
3. Configurar HTTP Basic Auth
4. Establecer política de sesión stateless
5. Configurar Password Encoder

**Código Clave**:

```java
// SecurityConfig.java:28-87
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF deshabilitado (API stateless)
                .csrf(csrf -> csrf.disable())
                
                // CORS desde CorsConfig
                .cors(Customizer.withDefaults())
                
                // RULES DE AUTORIZACIÓN
                .authorizeHttpRequests(auth -> auth
                    // ADMIN-only endpoints
                    .requestMatchers("/api/v1/ingestion/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/peces").hasRole("ADMIN")
                    .requestMatchers("/actuator/metrics", "/actuator/scheduledtasks").hasRole("ADMIN")
                    .requestMatchers("/h2-console/**").hasRole("ADMIN")
                    
                    // Public endpoints
                    .requestMatchers("/peces/**", "/api/v1/ingestion/health").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    
                    // Resto requiere autenticación
                    .anyRequest().authenticated()
                )
                
                // HTTP Basic Auth
                .httpBasic(Customizer.withDefaults())
                
                // Sesión stateless (sin cookies)
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
```

### 3.2 CorsConfig.java - Cross-Origin Resource Sharing

**Ubicación**: `src/main/java/com/pescayucatan/api_pesca_merida/config/CorsConfig.java`

**Responsabilidades**:
1. Definir orígenes permitidos
2. Configurar métodos HTTP permitidos
3. Definir headers autorizados
4. Habilitar credenciales

**Código**:

```java
// CorsConfig.java:14-50
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Orígenes permitidos
        config.setAllowedOrigins(Arrays.asList(
            "https://pesca-merida.com",    // Producción
            "http://localhost:3000",       // React dev
            "http://localhost:5173"        // Vite dev
        ));
        
        // Métodos HTTP
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        // Headers autorizados
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",  // Para Basic Auth
            "Content-Type",
            "Accept"
        ));
        
        // Habilitar credenciales
        config.setAllowCredentials(true);
        
        // Cache preflight: 1 hora
        config.setMaxAge(3600L);
        
        return config;
    }
}
```

---

## 4. Role-Based Access Control (RBAC)

### 4.1 Matriz de Permisos

| Endpoint | Método | Rol Requerido | Descripción |
|----------|--------|---------------|--------------|
| `/peces` | GET | **PÚBLICO** | Listar todas las especies |
| `/peces/{id}` | GET | **PÚBLICO** | Obtener especie por ID |
| `/peces/temporada` | GET | **PÚBLICO** | Buscar por zona/temporada |
| `/peces` | POST | **ADMIN** | Crear nueva especie |
| `/api/v1/ingestion/trigger` | POST | **ADMIN** | Ejecutar ingesta manual |
| `/api/v1/ingestion/status` | GET | **ADMIN** | Ver historial ingestas |
| `/api/v1/ingestion/latest` | GET | **ADMIN** | Ver última ingesta |
| `/api/v1/ingestion/stats` | GET | **ADMIN** | Ver estadísticas |
| `/api/v1/ingestion/health` | GET | **PÚBLICO** | Health check ingestas |
| `/actuator/health` | GET | **PÚBLICO** | Spring Actuator health |
| `/actuator/info` | GET | **PÚBLICO** | Spring Actuator info |
| `/actuator/metrics` | GET | **ADMIN** | Métricas JVM |
| `/actuator/scheduledtasks` | GET | **ADMIN** | Tareas programadas |
| `/h2-console/**` | * | **ADMIN** | Consola H2 (dev) |
| `/swagger-ui/**` | GET | **PÚBLICO** | Documentación API |

### 4.2 Diagrama de Acceso

```mermaid
flowchart TB
    subgraph Public
        GET_PECES[GET /peces]
        GET_PEZ_ID[GET /peces/{id}]
        GET_TEMPORADA[GET /peces/temporada]
        GET_HEALTH[GET /ingestion/health]
        GET_ACTUATOR[GET /actuator/health]
        GET_SWAGGER[GET /swagger-ui]
    end
    
    subgraph Admin
        POST_PECES[POST /peces]
        POST_TRIGGER[POST /ingestion/trigger]
        GET_STATUS[GET /ingestion/status]
        GET_STATS[GET /ingestion/stats]
        GET_METRICS[GET /actuator/metrics]
    end
    
    Public -->|permitAll| API
    Admin -->|hasRole(ADMIN)| API
    
    API[Spring Security<br/>Filter Chain]
    DB[(Database)]
    API --> DB
```

---

## 5. Password Handling

### 5.1 BCrypt Configuration

**Ubicación**: `SecurityConfig.java:101-106`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    // BCrypt con strength 12 (default es 10)
    // Cada hash toma ~300ms → protege contra brute force
    return new BCryptPasswordEncoder(12);
}
```

**Características**:
- **Algoritmo**: BCrypt con salt automático
- **Strength**: 12 rondas (512 iteraciones)
- **Output**: 60 caracteres hash
- **Protección**: Resistente a rainbow tables y brute force

### 5.2 Password Seeder

**Ubicación**: `DataSeeder.java:38-67`

```java
private void seedAdminUser() {
    // Leer password desde variable de entorno
    String adminPassword = System.getenv("ADMIN_PASSWORD");
    if (adminPassword == null || adminPassword.isBlank()) {
        log.error("FATAL: ADMIN_PASSWORD environment variable is missing!");
        return;
    }
    
    // Codificar password con BCrypt
    User adminUser = User.builder()
        .username("admin")
        .password(passwordEncoder.encode(adminPassword))  // BCrypt hash
        .roles(Set.of(adminRole))
        .enabled(true)
        .build();
    
    userRepository.save(adminUser);
}
```

**Security Best Practice**: El password del admin NO está hardcodeado en código. Se lee desde variable de entorno `ADMIN_PASSWORD`.

---

## 6. Session Management

### 6.1 Stateless Configuration

**Ubicación**: `SecurityConfig.java:83-85`

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

**Características**:
- **Sin cookies de sesión**: Cada request debe incluir credenciales
- **Cada request es independiente**: No hay estado entre requests
- **Compatible con REST**: Ideal para APIs stateless
- **Escalabilidad**: Sin sesiones en servidor, facilita load balancing

### 6.2 Autenticación por Request

```mermaid
sequenceDiagram
    participant Client
    participant Server
    
    Client->>Server: GET /peces<br/>Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=
    
    Note over Server: Cada request requiere<br/>Authorization header
    
    Server-->>Client: 200 OK + JSON data
    
    Client->>Server: GET /api/v1/ingestion/status<br/>Authorization: Basic ...
    
    Note over Server: Mismo header requerido<br/>para cada request
    
    Server-->>Client: 200 OK + data
```

---

## 7. Authentication Flow

### 7.1 CustomUserDetailsService

**Ubicación**: `src/main/java/com/pescayucatan/api_pesca_merida/service/CustomUserDetailsService.java`

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User no encontrado: " + username));

        // Mapear User entity a Spring UserDetails
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),
            user.isAccountNonExpired(),
            user.isCredentialsNonExpired(),
            user.isAccountNonLocked(),
            user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet())
        );
    }
}
```

### 7.2 User Entity - UserDetails Implementation

**Ubicación**: `src/main/java/com/pescayucatan/api_pesca_merida/model/User.java`

```java
@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;  // Almacena BCrypt hash
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(name = "account_non_expired")
    private boolean accountNonExpired = true;
    
    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;
    
    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", ...)
    private Set<Role> roles;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet());
    }
}
```

---

## 8. Implementation Patterns

### 8.1 Creating New Admin Users

```java
// Para crear usuarios programmatically
@Service
public class UserManagementService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUser(String username, String plainPassword, Set<Role> roles) {
        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(plainPassword))  // Siempre codificar
            .roles(roles)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();
        
        userRepository.save(user);
    }
}
```

### 8.2 Adding New Protected Endpoints

```java
// Ejemplo: Nuevo endpoint solo para ADMIN
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    // Esta ruta requiere ROLE_ADMIN (configurado en SecurityConfig)
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        return Map.of(
            "users", userRepository.count(),
            "system", "healthy"
        );
    }
}
```

### 8.3 Programmatic Security Checks

```java
// Checks manuales dentro de servicios
@Service
public class SomeService {

    @Autowired
    private AuthenticationManager authenticationManager;

    public void performAction(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        
        if (auth.isAuthenticated()) {
            // Proceder con acción
        }
    }
}
```

---

## 9. Security Headers

### 9.1 Frame Options Configuration

**Ubicación**: `SecurityConfig.java:72-74`

```java
.headers(headers -> headers
    .frameOptions(frame -> frame.sameOrigin())  // Solo iframes del mismo origen
)
```

**Protección**: Previene clickjacking permitiendo solo iframes del mismo dominio.

### 9.2 Headers Recomendados (No implementados actualmente)

| Header | Propósito | Recomendación |
|--------|-----------|---------------|
| `X-Content-Type-Options` | Previene MIME sniffing | `nosniff` |
| `Strict-Transport-Security` | Fuerza HTTPS | En producción |
| `X-Frame-Options` | Previene clickjacking | `DENY` o `SAMEORIGIN` |
| `Content-Security-Policy` | Previene XSS | CSP custom |

---

## 10. Testing Security

### 10.1 Testear endpoints públicos

```bash
# Sin autenticación - debe funcionar
curl http://localhost:8080/peces

# Output: JSON array de peces
```

### 10.2 Testear endpoints admin

```bash
# Sin autenticación - debe dar 401
curl http://localhost:8080/api/v1/ingestion/status
# {"error":"Unauthorized","status":401,"message":"..."}

# Con Basic Auth wrong credentials - 401
curl -u admin:wrongpass http://localhost:8080/api/v1/ingestion/status

# Con Basic Auth correct - debe dar 200
curl -u admin:$ADMIN_PASSWORD http://localhost:8080/api/v1/ingestion/status
```

### 10.3 Testear CORS

```bash
# Preflight request
curl -X OPTIONS http://localhost:8080/peces \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization"
```

---

## 11. Environment Configuration

### 11.1 Variables de Entorno Requeridas

```bash
# Required
export ADMIN_PASSWORD="TuPasswordSeguro123!"

# Optional - override defaults
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod
```

### 11.2 Application Properties

```properties
# Security (default - no cambiar en prod)
spring.security.user.name=admin
# Password se provee via ADMIN_PASSWORD env var

# CORS origins
cors.allowed.origins=https://pesca-merida.com,http://localhost:3000
```

---

## 12. Common Security Pitfalls

### ✅ Lo que se hace bien
1. **BCrypt con strength 12** - Más seguro que default (10)
2. **Password desde env var** - No hardcodeado en código
3. **Stateless sessions** - Sin cookies de sesión
4. **CSRF deshabilitado** - Correcto para REST API
5. **CORS restrictivo** - Orígenes específicos, no `*`

### ⚠️ Mejoras potenciales
1. **Sin @PreAuthorize en controllers** - Seguridad solo en SecurityConfig
2. **No hay JWT** - Solo Basic Auth (limitado para SPAs)
3. **H2 Console habilitado** - Deshabilitar en producción
4. **Sin rate limiting** - Vulnerable a brute force
5. **Sin HTTPS en desarrollo** - Implementar en producción

---

## 13. Adding New Security Rules

### Agregar nuevo endpoint público

```java
// En SecurityConfig.java:59
.requestMatchers("/nuevo-endpoint-publico").permitAll()
```

### Agregar nuevo endpoint admin

```java
// En SecurityConfig.java:51
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
```

### Cambiar origen CORS

```java
// En CorsConfig.java:21
config.setAllowedOrigins(Arrays.asList(
    "https://nuevo-dominio.com",
    "http://localhost:3000"
));
```

---

## 14. Quick Reference - Developer Guide

### Autenticación
```bash
# Formato: Base64(username:password)
Authorization: Basic YWRtaW46cGFzc3dvcmQxMjM=

# Ejemplo con curl
curl -u admin:$ADMIN_PASSWORD http://localhost:8080/api/v1/ingestion/status
```

### Roles en Base de Datos
```sql
-- Tabla de roles
SELECT * FROM roles;

-- Asignar rol a usuario
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- Ver roles de usuario
SELECT u.username, r.name 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id;
```

### Endpoints de Prueba
| Endpoint | Auth Required | Descripción |
|----------|---------------|-------------|
| `GET /peces` | No | Listar peces |
| `GET /api/v1/ingestion/health` | No | Health check |
| `POST /api/v1/ingestion/trigger` | Yes (ADMIN) | Trigger ingesta |
| `GET /actuator/health` | No | Actuator health |

---

*Security Model documentado: 2026-04-01*
*Base de análisis: SecurityConfig.java, CorsConfig.java, CustomUserDetailsService.java, User.java, Role.java, DataSeeder.java*