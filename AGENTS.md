# AGENTS.md - API Pesca Yucatán

Guidelines for agentic coding systems working in this repository.

## Project Overview

**Main Application**: Spring Boot 3.5 REST API for managing fishing seasons (vedas) and marine species in Yucatán, Mexico.
**PDF Extractor**: Python FastAPI microservice for extracting tables from PDF documents.
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

### PDF Extractor (Python FastAPI)

```bash
cd pdf-extractor
.\venv\Scripts\uvicorn main:app --reload --port 8000
.\venv\Scripts\pytest
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
public record VedaCsvRow(Integer pezId, String nombreComun, String especieCientifica) {
    public static VedaCsvRow fromCsvLine(String[] cells) { /* ... */ }
}
```

#### Testing Conventions
- Use JUnit 5 (Jupiter)
- Use `@Nested` classes to group related tests
- Use `@DisplayName` for human-readable test names in Spanish
- Use Arrange-Act-Assert (AAA) pattern
- Test file location: `src/test/java/...` mirrors `src/main/java/...`

### 2.2 Python - FastAPI (PDF Extractor)

- Files: snake_case, Functions: snake_case with verb prefix
- Classes: PascalCase, Constants: UPPER_SNAKE_CASE
- Type hints: Required for function signatures
- Raise `HTTPException` for API errors, never expose raw exception details

### 2.3 General Guidelines

#### API Design
- Use plural nouns: `/api/v1/peces`
- Proper HTTP methods: GET (retrieve), POST (create), PUT/PATCH (update), DELETE (remove)
- Return appropriate status codes: 200, 201, 404, 500

#### Database Conventions
- Table names: singular, lowercase (`pez`, `especie_veda`)
- Column names: snake_case (`nombre_comun`, `talla_minima`)

#### Logging
- Use structured logging with meaningful messages
- Include relevant context (IDs, file names, row numbers)

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

---

## 4. Environment Setup

### Prerequisites
- Java 21+, Maven 3.9+, Python 3.10+ (PDF extractor), Node.js 18+ (frontend)

### Quick Start
```bash
./mvnw clean package
./mvnw test
./mvnw spring-boot:run
# API: http://localhost:8080
# H2 Console: http://localhost:8080/h2-console
```

---

## 5. Common Tasks

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

---

Last updated: 2026-04-02
