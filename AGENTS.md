# AGENTS.md - API Pesca Yucatán

Guidelines for agentic coding systems working in this repository.

## Project Overview

**Main Application**: Spring Boot 3.5 REST API for managing fishing seasons (vedas) and marine species in Yucatán, Mexico.
**Frontend**: (Planned) React + Vite + TypeScript SPA

---

## 1. Build, Test & Run Commands

### Main Spring Boot Application (Java 21)

```bash
# Build (compiles, runs tests, creates JAR)
./mvnw clean package

# Run application
./mvnw spring-boot:run

# Run without packaging
./mvnw spring-boot:run -Dspring-boot.run.fork=false

# Run a single test class
./mvnw test -Dtest=CsvParserServiceTest

# Run a single test method
./mvnw test -Dtest=CsvParserServiceTest#testParseEspecies_HappyPath

# Run tests matching a pattern
./mvnw test -Dtest="*Controller*Test"

# Skip tests (for faster builds)
./mvnw clean package -DskipTests

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# View test reports: target/surefire-reports/*.txt
```

---

## 2. Code Style Guidelines

### 2.1 Java - Spring Boot

#### Package Structure
```
com.pescayucatan.api_pesca_merida
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access (Spring Data JPA)
├── model/          # JPA entities
├── enums/          # Enumerations
├── exception/      # Custom exceptions
├── config/         # Configuration classes
└── infrastructure/ # External integrations (CSV)
```

#### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `PezService`, `IngestionController` |
| Methods | camelCase | `listarTodosLosPeces()`, `buscarPorZona()` |
| Variables | camelCase | `pezRepository`, `nombreComun` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS` |
| Packages | lowercase | `com.pescayucatan.api_pesca_merida.model` |
| Database columns | snake_case | `nombre_comun`, `talla_minima` |
| Enums | PascalCase | `TEMPORAL_FIJA`, `PERMANENTE` |

#### Import Order
1. `java.*` and `javax.*`
2. `org.*` (Spring, third-party)
3. `com.pescayucatan.*` (project imports)

#### Annotations
- Use `@Service` for business logic, `@RestController` for REST endpoints
- Use `@RequiredArgsConstructor` (Lombok) for constructor injection
- Avoid `@Autowired` on fields; prefer constructor injection
- Use `@Slf4j` (Lombok) for logging
- Use `@Transactional` for methods modifying data

#### Error Handling
- Use custom exceptions extending `RuntimeException`
- Handle exceptions at controller level with `@ExceptionHandler`
- Use `GlobalExceptionHandler` for centralized handling
- Log errors with appropriate level (`log.error()` for exceptions, `log.warn()` for recoverable)
- Never expose stack traces to clients

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PezNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlePezNotFound(PezNotFoundException ex) {
        return ex.getMessage();
    }
}
```

#### Records (Java 16+)
- Use records for simple DTOs/CSV rows - immutable by default

```java
public record PeriodoVedaCsvRow(
    Long id,
    Long regulacionId,
    String tipoVeda,
    Integer mesInicio,
    Integer diaInicio,
    Integer mesFin,
    Integer diaFin,
    String fuenteDof
) {
    public static PeriodoVedaCsvRow fromCsvLine(String[] cols) {
        return new PeriodoVedaCsvRow(
            parseLongOrNull(cols[0]),
            parseLongOrNull(cols[1]),
            cols[2].trim(),
            parseIntOrNull(cols[3]),
            parseIntOrNull(cols[4]),
            parseIntOrNull(cols[5]),
            parseIntOrNull(cols[6]),
            cols[7].trim()
        );
    }
}
```

#### Testing Conventions
- Use JUnit 5 (Jupiter)
- Use `@Nested` classes to group related tests
- Use `@DisplayName` for human-readable test names in Spanish
- Use Arrange-Act-Assert (AAA) pattern
- Test file location: `src/test/java/...` mirrors `src/main/java/...`

### 2.2 General Guidelines

#### API Design
- Use plural nouns: `/api/v1/peces`
- Proper HTTP methods: GET (retrieve), POST (create), PUT/PATCH (update), DELETE (remove)
- Return appropriate status codes: 200, 201, 404, 500

#### Database Conventions
- Table names: singular, lowercase (`pez`, `regulacion`)
- Column names: snake_case (`nombre_comun`, `talla_minima`)

#### Logging
- Use `@Slf4j` for structured logging
- Include relevant context (IDs, file names, row numbers)
- Use emoji sparingly (only in data pipeline logs for visibility)

---

## 3. Configuration Files

### application.properties
```properties
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:pescayucatandb
cors.allowed.origins="http://localhost:3000","http://localhost:5173"
```

### Flyway Migrations
- Location: `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1__create_vedas_schema.sql`)
- Migration files:
  - `V1__create_vedas_schema.sql` - Core tables: `pez`, `zona`, `regulacion`, `periodo_veda`, `arte_pesca`, `ingestions_logs`
  - `V2__create_auth_schema.sql` - Auth tables: `users`, `roles`, `user_roles`

---

## 4. Environment Setup

### Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 18+ (frontend - planned)

### Quick Start
```bash
./mvnw clean package
./mvnw test
./mvnw spring-boot:run
# API: http://localhost:8080
# H2 Console: http://localhost:8080/h2-console
```

---

## 5. Database Schema

### Entity Relationships
```
zona (1) ---> (N) regulacion
pez (1) ---> (N) regulacion
regulacion (1) ---> (N) periodo_veda
regulacion (1) ---> (N) arte_pesca
users (N) <---> (N) roles (via user_roles)
```

### Core Tables

| Table | Description |
|-------|-------------|
| `pez` | Marine species catalog |
| `zona` | Geographic fishing zones |
| `regulacion` | Fishing regulations per species/zone |
| `periodo_veda` | Veda (fishing ban) periods |
| `arte_pesca` | Fishing gear per regulation |
| `ingestions_logs` | CSV ingestion audit log |

---

## 6. Common Tasks

### Adding a New Entity
1. Create model class with JPA annotations
2. Create repository interface
3. Create service class
4. Create REST controller
5. Add Flyway migration if schema changes
6. Add exception handling
7. Write unit tests

### Modifying CSV Schema
1. Update `CsvParserService` to handle new columns
2. Update corresponding `*CsvRow` record
3. Add test cases for new schema

### Data Ingestion Pipeline
1. CSV files downloaded from Google Sheets URLs in `application.properties`
2. SHA-256 hash computed for change detection
3. If new content, parse with `CsvParserService`
4. Upsert records via repository
5. Log result in `ingestions_logs` table

---

## 7. Security

- HTTP Basic Auth via Spring Security
- Role-based access: PUBLIC (species queries) vs ADMIN (ingestion, metrics)
- Default dev user: `admin` / `admin123` (H2 only)
- Production must disable `DataSeeder` and use proper credentials

---

Last updated: 2026-04-07
