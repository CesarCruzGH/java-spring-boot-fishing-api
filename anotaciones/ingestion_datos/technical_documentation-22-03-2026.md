# Documentación Técnica: API Pesca Mérida
> **Ruta destino:** `C:\Users\cesar\IdeaProjects\java-spring-boot-fishing-api\anotaciones\ingestion_datos`  
> **Fecha de sincronización:** 2026-03-22  
> **Doc. de referencia (legacy):** `anotaciones/inicio/technical_documentation-11-03-2026`

---

## 1. Resumen de Cambios (Legacy → Estado Actual)

| # | Categoría | Estado anterior (11-03) | Estado actual (22-03) | Tipo |
|---|-----------|-------------------------|-----------------------|------|
| 1 | **Dependencias** | `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `h2`, `spring-boot-devtools`, `spring-boot-starter-test` | +`flyway-mysql`, +`lombok`, +`pdfbox 3.0.3` | ADICIÓN |
| 2 | **Spring Boot** | 3.x (no versionado) | `3.5.11` | CLARIFICACIÓN |
| 3 | **Java** | No especificado | `21` (LTS) | CLARIFICACIÓN |
| 4 | **Enum `ZonaPesca`** | Presente (`YUCATAN`, `CAMPECHE`, `QUINTANA_ROO`, `GOLFO_DE_MEXICO`) | **ELIMINADO**. `Pez.zona` ahora es `String` libre; validación trasladada a data source | ELIMINACIÓN |
| 5 | **Entidad `Pez`** | 7 campos: `id`, `nombre`, `zona`, `tipoVeda`, `inicioVeda`, `finVeda`, `enVeda` | 8 campos: `id`, `nombreComun`, `especie`, `nombreMaya`, `tallaMinima`, `habitat`, `tecnicaRecomendada`, `zona`. Sin campos de veda (delegados a `EspecieVeda`) | REFACTOR |
| 6 | **Entidad `EspecieVeda`** | NO existía | NUEVA. Modelo normalizado de vedas con FK a `Pez`, soporte TEMPORAL_FIJA / TEMPORAL_VARIABLE / PERMANENTE / PLURIANUAL | ADICIÓN |
| 7 | **Entidad `IngestionLog`** | NO existía | NUEVA. Trazabilidad de ingesta: hash SHA-256, estado, filas procesadas, detalle de error | ADICIÓN |
| 8 | **Controlador `IngestionController`** | NO existía | NUEVO. 6 endpoints REST bajo `/api/v1/ingestion` | ADICIÓN |
| 9 | **Servicio `IngestionService`** | NO existía | NUEVO. Orquestador de descarga CSV (Google Sheets) → hash → idempotencia → UPSERT | ADICIÓN |
| 10 | **Servicio `CsvParserService`** | NO existía | NUEVO. Parser RFC 4180, manejo de UTF-8 BOM, validación de headers | ADICIÓN |
| 11 | **Paquete `infrastructure.csv`** | NO existía | NUEVO. Records DTOs: `EspecieCsvRow`, `VedaCsvRow` | ADICIÓN |
| 12 | **Paquete `config`** | NO existía | NUEVO (vacío, preparado para configuration classes) | ADICIÓN |
| 13 | **Repositorios** | Solo `PezRepository` (`findByZona(ZonaPesca)`) | `PezRepository` (refactorizado: `findByZona(String)`, +`findByNombreComunIgnoreCase`), +`EspecieVedaRepository`, +`IngestionLogRepository` | REFACTOR/ADICIÓN |
| 14 | **Enums** | `ZonaPesca` | `TipoVeda` (NUEVO), `EstadoIngestion` (NUEVO). `ZonaPesca` eliminado | REEMPLAZO |
| 15 | **Flyway** | NO existía (DDL por Hibernate `create-drop`/`update`) | Habilitado. `spring.jpa.hibernate.ddl-auto=none`. Migración: `V2__create_vedas_schema.sql` | ADICIÓN |
| 16 | **Scheduling** | NO existía | `@EnableScheduling` en `ApiPescaMeridaApplication`. `@Scheduled` en `IngestionService` (initialDelay=30s, fixedDelay=10min) | ADICIÓN |
| 17 | **`PezController`** | Usaba `@Autowired` en constructor | Migrado a `@RequiredArgsConstructor` (Lombok). Constructor manual comentado | REFACTOR |

---

## 2. Tech Stack

| Componente | Tecnología | Versión |
|------------|-----------|---------|
| Runtime | Java | 21 (LTS) |
| Framework | Spring Boot | 3.5.11 |
| Web | spring-boot-starter-web | BOM |
| ORM | Spring Data JPA + Hibernate | BOM |
| BD (dev) | H2 Database (in-memory) | BOM |
| Migraciones | Flyway (flyway-mysql) | BOM |
| Boilerplate | Lombok | BOM |
| PDF | Apache PDFBox | 3.0.3 |
| Build | Maven (spring-boot-maven-plugin) | BOM |
| Test | spring-boot-starter-test | BOM |

---

## 3. Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Spring Boot Application                        │
│                      @EnableScheduling activo                          │
├──────────────┬──────────────────────┬───────────────────────────────────┤
│  Controller  │       Service        │           Repository             │
├──────────────┼──────────────────────┼───────────────────────────────────┤
│PezController │ PezService           │ PezRepository                    │
│              │                      │                                  │
│IngestionCtrl │ IngestionService     │ EspecieVedaRepository            │
│              │ CsvParserService     │ IngestionLogRepository           │
├──────────────┴──────────────────────┴───────────────────────────────────┤
│                        Infrastructure (DTOs)                           │
│              EspecieCsvRow (record) │ VedaCsvRow (record)              │
├─────────────────────────────────────────────────────────────────────────┤
│  Model: Pez │ EspecieVeda │ IngestionLog                               │
│  Enum: TipoVeda │ EstadoIngestion                                      │
│  Exception: PezNotFoundException │ PezNameNotFoundException            │
│             GlobalExceptionHandler (@RestControllerAdvice)             │
├─────────────────────────────────────────────────────────────────────────┤
│       H2 (in-memory) │ Flyway (V2__create_vedas_schema.sql)           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Flujo de Datos: Ingesta Automatizada

```
Google Sheets (CSV)
       │
       ▼
 IngestionService.ejecutarIngestion()     ← @Scheduled(fixedDelay=600000)
       │                                     o IngestionController POST /trigger
       ├── descargarCsv(url)              ← java.net.http.HttpClient (30s timeout, FOLLOW_REDIRECTS)
       │
       ├── calcularSha256(bytes)          ← MessageDigest SHA-256
       │
       ├── existsByHashSha256(hash)?      ← Idempotencia: SKIP si ya procesado
       │       │ YES → return null
       │       │ NO  ↓
       │
       ├── csvParser.parsePeces(bytes)    ← CsvParserService (RFC 4180, UTF-8 BOM)
       │       └── List<EspecieCsvRow>
       │
       ├── upsertEspecies(list)           ← @Transactional, findById → save (INSERT/UPDATE)
       │       └── → tabla `pez`
       │
       ├── csvParser.parseVedas(bytes)
       │       └── List<VedaCsvRow>
       │
       ├── upsertVedas(list)              ← @Transactional, findByPezAndZonaAndTipoVeda → save
       │       └── → tabla `especie_veda`
       │
       └── crearIngestionLog()            ← → tabla `ingestions_logs` (estado: COMPLETADO/ERROR)
```

---

## 4. Modelo de Datos (JPA Entities)

### 4.1 `Pez` → tabla `pez`
| Campo | Tipo Java | Columna DB | Constraint | Nota |
|-------|----------|------------|------------|------|
| `id` | `Long` | `id` | `@Id` (sin auto-generación) | PK asignada desde CSV (ID CONAPESCA) |
| `nombreComun` | `String` | `nombre_comun` | — | |
| `especie` | `String` | `especie` | — | Nombre científico |
| `nombreMaya` | `String` | `nombre_maya` | — | |
| `tallaMinima` | `String` | `TALLA_MINIMA` | — | Texto libre (ej: "30 cm") |
| `habitat` | `String` | `habitat` | — | |
| `tecnicaRecomendada` | `String` | `tecnica_recomendada` | — | |
| `zona` | `String` | `zona` | `NOT NULL` | String libre, no enum |

> **Cambio clave vs legacy:** Los campos `tipoVeda`, `inicioVeda`, `finVeda`, `enVeda` fueron eliminados de `Pez` y la lógica de vedas se normalizó en la entidad `EspecieVeda`.

### 4.2 `EspecieVeda` → tabla `especie_veda`
| Campo | Tipo Java | Columna DB | Constraint | Nota |
|-------|----------|------------|------------|------|
| `id` | `Long` | `id` | `@Id @GeneratedValue(IDENTITY)` | Auto-increment |
| `pez` | `Pez` | `pez_id` | `@ManyToOne(LAZY)`, `NOT NULL`, `FK → pez(id)` | |
| `zona` | `String` | `zona` | — | |
| `tipoVeda` | `TipoVeda` | `tipo_veda` | `@Enumerated(STRING)`, `NOT NULL` | |
| `inicioFijo` | `LocalDate` | `inicio_fijo` | — | Para vedas FIJA/PLURIANUAL |
| `finFijo` | `LocalDate` | `fin_fijo` | — | |
| `inicioMes` | `Integer` | `inicio_mes` | `TINYINT` | 1-12, para vedas CÍCLICAS |
| `inicioDia` | `Integer` | `inicio_dia` | `TINYINT` | 1-31 |
| `finMes` | `Integer` | `fin_mes` | `TINYINT` | |
| `finDia` | `Integer` | `fin_dia` | `TINYINT` | |
| `cancelada` | `Boolean` | `cancelada` | `NOT NULL DEFAULT FALSE` | Override manual |
| `motivoCancelacion` | `String` | `motivo_cancelacion` | — | Texto libre (ref. DOF) |
| `canceladaEn` | `LocalDateTime` | `cancelada_en` | `TIMESTAMP` | |
| `fuente_dof` | `String` | `fuente_dof` | — | Hash/nombre del PDF origen |
| `creadoEn` | `LocalDateTime` | `creado_en` | `NOT NULL DEFAULT NOW()`, `updatable=false` | |
| `actualizadoEn` | `LocalDateTime` | `actualizado_en` | — | `@PreUpdate` auto-set |

**Índices SQL:**
- `idx_veda_zona_tipo` → `(zona, tipo_veda)`
- `idx_veda_fechas_fijas` → `(inicio_fijo, fin_fijo)`
- `idx_veda_ciclo` → `(inicio_mes, inicio_dia, fin_mes, fin_dia)`

### 4.3 `IngestionLog` → tabla `ingestions_logs`
| Campo | Tipo Java | Columna DB | Constraint | Nota |
|-------|----------|------------|------------|------|
| `id` | `Long` | `id` | `@Id @GeneratedValue(IDENTITY)` | |
| `nombreArchivo` | `String` | `nombre_archivo` | `NOT NULL` | `"especies.csv"` / `"vedas.csv"` / `"ERROR"` |
| `hashSha256` | `String` | `hash_sha256` | `NOT NULL UNIQUE`, `CHAR(64)` | Clave de idempotencia |
| `totalFilas` | `Integer` | `total_filas` | — | |
| `filasExitosas` | `Integer` | `filas_exitosas` | — | |
| `filasError` | `Integer` | `filas_error` | — | |
| `estado` | `EstadoIngestion` | `estado` | `@Enumerated(STRING)`, `NOT NULL` | PROCESANDO / COMPLETADO / ERROR |
| `procesadoEn` | `LocalDateTime` | `procesado_en` | `NOT NULL DEFAULT NOW()` | |
| `detalleError` | `String` | `detalle_error` | `CLOB` | Texto largo para stack traces |

---

## 5. Enums

### 5.1 `TipoVeda`
| Valor | Semántica | Mapeo CSV (`fromCsvValue`) |
|-------|----------|---------------------------|
| `TEMPORAL_FIJA` | Fechas cíclicas anuales fijas | `"TEMPORAL FIJA"` |
| `TEMPORAL_VARIABLE` | Fechas cíclicas que cambian por DOF | `"TEMPORAL VARIABLE"` |
| `PERMANENTE` | Prohibición total sin fechas | `"PERMANENTE"` |
| `PLURIANUAL` | Abarca múltiples años | ⚠️ Sin mapeo en `fromCsvValue` (lanzará `IllegalArgumentException`) |

### 5.2 `EstadoIngestion`
| Valor | Uso |
|-------|-----|
| `PROCESANDO` | Estado transitorio durante ejecución |
| `COMPLETADO` | Éxito |
| `ERROR` | Fallo con registro en `detalle_error` |

---

## 6. Infrastructure DTOs (Records)

### 6.1 `EspecieCsvRow` (8 campos)
```java
record EspecieCsvRow(
    Integer id,
    String nombreComun,
    String especieCientifica,
    String nombreMaya,
    String tallaMinima,
    String habitat,
    String tecnicaRecomendada,
    String zona
)
```
**Header CSV esperado:** `ID | NOMBRE COMÚN | ESPECIE | NOMBRE MAYA | TALLA MÍNIMA | HÁBITAT | TÉCNICA RECOMENDADA | ZONA`

### 6.2 `VedaCsvRow` (12 campos)
```java
record VedaCsvRow(
    Integer pezId,
    String nombreComun,
    String especieCientifica,
    String zona,
    String tipoVeda,
    Integer inicioMes,
    Integer inicioDia,
    Integer finMes,
    Integer finDia,
    String inicioFijo,
    String finFijo,
    String fuenteDof
)
```
**Header CSV esperado:** `Pez ID | Nombre Común | Especie Científica | Zona | Tipo de Veda | Inicio mes | Inicio día | Fin mes | Fin día | Inicio fijo | Fin fijo | Fuente DOF`

---

## 7. Contratos API

### 7.1 API de Peces (Legacy, mantenida)

| Método | Endpoint | Params | Response | Descripción |
|--------|----------|--------|----------|-------------|
| `GET` | `/peces` | — | `List<Pez>` | Listar todos |
| `GET` | `/peces/temporada` | `?zona=<string>` (opcional) | `List<Pez>` | Filtrar por zona. Si vacío, retorna todos. `404` si zona sin resultados |
| `GET` | `/peces/{id}` | `id` (path) | `Pez` | Por ID. `404` si no existe |
| `POST` | `/peces` | `@RequestBody Pez` | `Pez` (201) | Registrar nuevo pez |

### 7.2 API de Ingesta (NUEVA — `/api/v1/ingestion`)

| Método | Endpoint | Params | Response | Descripción |
|--------|----------|--------|----------|-------------|
| `POST` | `/trigger` | — | `Map{status, message, timestamp}` | Ejecuta ingesta síncrona. `200` éxito, `500` error |
| `GET` | `/status` | — | `List<IngestionLog>` (max 20) | Historial ordenado por `procesadoEn` DESC |
| `GET` | `/latest` | — | `IngestionLog` | Última ejecución. `404` si no hay registros |
| `GET` | `/stats` | — | `Map{totalEjecuciones, exitosas, errores, tasaExito, totalEspeciesProcesadas, totalVedasProcesadas, ultimaEjecucion, ultimoEstado}` | Estadísticas agregadas |
| `DELETE` | `/logs` | `?diasRetencion=30` (default) | `Map{status: "not_implemented"}` | ⚠️ TODO: Limpieza de logs antiguos |
| `GET` | `/health` | — | `Map{status, ultimaEjecucion, ultimoEstado, minutosDesdeUltimaEjecucion, erroresRecientes}` | Health check del subsistema |

---

## 8. Servicios Core

### 8.1 `PezService`
- **Inyección:** Constructor explícito con `@Autowired`
- **Métodos:** `listarTodosLosPeces()`, `guardarPez(Pez)`, `buscarPeces(String zona)`, `obtenerPorId(Long id)`
- **Validación:** Si `zona != null` y no hay resultados → `PezNotFoundException`
- **Sin cambios funcionales** respecto a legacy (excepto tipo de zona: `ZonaPesca` → `String`)

### 8.2 `IngestionService`
- **Anotaciones:** `@Service`, `@Slf4j`, `@RequiredArgsConstructor`, `@ConditionalOnProperty(name="ingestion.enabled", havingValue="true", matchIfMissing=true)`
- **Scheduling:** `@Scheduled(initialDelay=30000, fixedDelay=600000)` — 30s delay inicial, recurrencia cada 10 minutos
- **HttpClient:** `java.net.http.HttpClient`, timeout 30s, `Redirect.ALWAYS`
- **Idempotencia:** Hash SHA-256 sobre bytes crudos del CSV, verificado con `IngestionLogRepository.existsByHashSha256(hash)`
- **Transaccionalidad:** `upsertEspecies()` y `upsertVedas()` son `@Transactional`. Fallos individuales de fila se loguean como `WARN` sin romper el batch
- **Estrategia UPSERT Especies:** `findById(dto.id)` → si existe: UPDATE, si no: INSERT
- **Estrategia UPSERT Vedas:** `findByPezAndZonaAndTipoVeda(pez, zona, tipoVeda)` → combinación única como natural key

### 8.3 `CsvParserService`
- **Anotaciones:** `@Service`, `@Slf4j`
- **Zero dependencias externas:** Parser CSV nativo (RFC 4180 compliant)
- **BOM handling:** Detecta y elimina UTF-8 BOM (`\uFEFF`)
- **Validación de headers:** Compara cantidad de columnas (no contenido), loguea `WARN` si difiere
- **Tolerancia a errores:** Filas inválidas se saltan con log, no interrumpen el proceso

---

## 9. Repositorios

| Repositorio | Extiende | Derived Queries |
|------------|----------|-----------------|
| `PezRepository` | `JpaRepository<Pez, Long>` | `findByZona(String)`, `findByNombreComunIgnoreCase(String)` |
| `EspecieVedaRepository` | `JpaRepository<EspecieVeda, Long>` | `findByPezAndZonaAndTipoVeda(Pez, String, TipoVeda)` |
| `IngestionLogRepository` | `JpaRepository<IngestionLog, Long>` | `existsByHashSha256(String)` |

---

## 10. Exceptions

| Clase | Herencia | Status HTTP | Uso |
|-------|----------|-------------|-----|
| `PezNotFoundException` | `RuntimeException` | 404 | Búsqueda por ID o zona sin resultados |
| `PezNameNotFoundException` | `RuntimeException` | 404 | Búsqueda por nombre sin resultados |
| `GlobalExceptionHandler` | `@RestControllerAdvice` | — | Mapea excepciones custom a respuestas HTTP |

---

## 11. Migraciones Flyway

| Archivo | Contenido |
|---------|-----------|
| `V2__create_vedas_schema.sql` | Crea tablas `pez`, `especie_veda`, `ingestions_logs` + 3 índices compuestos |

**Configuración:**
```properties
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=false
spring.flyway.validate-on-migrate=true
```

> ⚠️ **Nota:** No existe `V1__*.sql`. La migración `V2` incluye la tabla `pez` con `CREATE TABLE IF NOT EXISTS`, lo que sugiere que V1 fue eliminada o el baseline se ajustó manualmente.

---

## 12. Configuración (`application.properties`)

```properties
# Aplicación
spring.application.name=api-pesca-merida

# Base de datos (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:pescayucatandb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=false
spring.flyway.validate-on-migrate=true

# Ingesta de datos (Google Sheets → CSV)
ingestion.sheets.especies-url=https://docs.google.com/spreadsheets/d/[SHEET_ID]/export?format=csv&gid=382368478
ingestion.sheets.vedas-url=https://docs.google.com/spreadsheets/d/[SHEET_ID]/export?format=csv&gid=0
ingestion.cron=0 */1 * * * *
ingestion.enabled=true
```

---

## 13. Estructura de Paquetes (Estado Actual)

```
com.pescayucatan.api_pesca_merida
├── ApiPescaMeridaApplication.java          ← @SpringBootApplication @EnableScheduling
├── config/                                 ← (vacío, preparado)
├── controller/
│   ├── PezController.java                  ← CRUD Peces (/peces)
│   └── IngestionController.java            ← Gestión ingesta (/api/v1/ingestion)  [NUEVO]
├── enums/
│   ├── TipoVeda.java                       ← TEMPORAL_FIJA|TEMPORAL_VARIABLE|PERMANENTE|PLURIANUAL  [NUEVO]
│   └── EstadoIngestion.java                ← PROCESANDO|COMPLETADO|ERROR  [NUEVO]
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── PezNotFoundException.java
│   └── PezNameNotFoundException.java
├── infrastructure/
│   └── csv/
│       ├── EspecieCsvRow.java               ← record DTO (8 campos)  [NUEVO]
│       └── VedaCsvRow.java                  ← record DTO (12 campos)  [NUEVO]
├── model/
│   ├── Pez.java                            ← Refactorizado (sin campos de veda)
│   ├── EspecieVeda.java                    ← Entidad normalizada de vedas  [NUEVO]
│   └── IngestionLog.java                   ← Trazabilidad de ingesta  [NUEVO]
├── repository/
│   ├── PezRepository.java                  ← +findByNombreComunIgnoreCase
│   ├── EspecieVedaRepository.java          ← [NUEVO]
│   └── IngestionLogRepository.java         ← [NUEVO]
└── service/
    ├── PezService.java                     ← zona: ZonaPesca → String
    ├── IngestionService.java               ← Orquestador descarga+hash+upsert  [NUEVO]
    └── CsvParserService.java               ← Parser CSV nativo RFC 4180  [NUEVO]
```

---

## 14. TODOs y Deuda Técnica Detectada

| # | Componente | Descripción | Prioridad |
|---|-----------|-------------|-----------|
| 1 | `IngestionController.cleanupLogs()` | Endpoint `DELETE /logs` retorna `"not_implemented"`. Falta lógica de `deleteByProcesadoEnBefore(cutoffDate)` | MEDIA |
| 2 | `TipoVeda.fromCsvValue()` | No mapea `"PLURIANUAL"`. Llegará como `IllegalArgumentException` y se saltará la fila | ALTA |
| 3 | `V1__*.sql` | Faltante. Solo existe `V2__create_vedas_schema.sql`. Posible conflicto si se usa `baseline-on-migrate=true` en futuro | BAJA |
| 4 | `EspecieVeda.inicioFijo/finFijo` | Comentario en code indica "parsean vacíos en CSV actual, dejar null". Sin lógica de parseo implementada en `upsertVedas()` para `VedaCsvRow.inicioFijo`/`finFijo` (son `String` en DTO, `LocalDate` en entity) | MEDIA |
| 5 | `IngestionService` | HttpClient hardcodeado como campo de instancia. No es testeable con mocks fácilmente. Considerar inyección via `@Bean` | BAJA |
| 6 | `PezController` | Constructor manual `@Autowired` comentado coexiste con `@RequiredArgsConstructor`. Eliminar dead code | BAJA |
| 7 | `IngestionService.ingestion.cron` | Propiedad `ingestion.cron` definida en properties pero NO referenciada por `@Scheduled` (usa `fixedDelay` hardcodeado en código) | MEDIA |
| 8 | `config/` | Paquete vacío. Sin `@Configuration` classes | INFO |
| 9 | Seguridad | `IngestionController` no tiene autenticación. Endpoints administrativos expuestos | ALTA |
