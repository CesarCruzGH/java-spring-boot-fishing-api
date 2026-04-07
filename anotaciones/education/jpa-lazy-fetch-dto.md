# Conceptos de JPA/Hibernate: Solución de LazyInitializationException

> Documento educativo sobre los problemas encontrados y las soluciones implementadas durante el desarrollo de los endpoints de Regulacion y PeriodoVeda.

---

## 1. El Problema Original

### 1.1 La Excepción

Al intentar acceder a los endpoints de regulaciones y periodos de veda, obtuvimos:

```
LazyInitializationException: Could not initialize proxy 
[com.pescayucatan.api_pesca_merida.model.Pez#1] - no session
```

### 1.2 ¿Qué estaba pasando?

El flujo era:

```
Cliente HTTP → RegulacionController → RegulacionRepository.findByPezId() → JSON Response
                                    ↓
                            Hibernate ejecuta query SQL
                            SELECT * FROM regulacion WHERE pez_id = ?
                                    ↓
                            Retorna objetos Regulacion
                                    ↓
                            Jackson intenta serializar a JSON
                                    ↓
                            Error: No puede acceder a pez.getNombreComun()
```

---

## 2. Entendiendo JPA y Hibernate

### 2.1 ¿Qué es JPA?

**JPA (Java Persistence API)** es una especificación (interfaz) que define cómo persistir objetos Java en bases de datos relacionales.

**Hibernate** es la implementación más popular de JPA.

### 2.2 Entity vs Tabla

```java
@Entity
@Table(name = "regulacion")
public class Regulacion {
    @Id
    private Long id;
    
    @ManyToOne                    // Relación muchos-a-uno
    @JoinColumn(name = "pez_id") // Columna en la BD que hace la FK
    private Pez pez;             // Campo Java (objeto)
}
```

| Concepto | Descripción |
|----------|-------------|
| `Entity` | Clase Java mapeada a una tabla |
| `Table` | La tabla en la base de datos |
| `Column` | Columna de la tabla |
| `JoinColumn` | Columna que representa una FK |

### 2.3 El Modelo de Datos Relacional

```
┌─────────────┐       ┌─────────────────┐       ┌─────────────┐
│    zona     │       │   regulacion    │       │     pez     │
├─────────────┤       ├─────────────────┤       ├─────────────┤
│ id          │◄──────│ zona_id (FK)    │       │ id          │
│ nombre      │       │ id              │──────►│ nombre_comun│
│ macro_zona  │       │ pez_id (FK)     │       │ especie     │
└─────────────┘       │ talla_minima    │       └─────────────┘
                      └─────────────────┘
                              │
                              │ 1:N
                              ▼
                      ┌─────────────────┐
                      │ periodo_veda   │
                      ├─────────────────┤
                      │ id              │
                      │ regulacion_id   │
                      │ tipo_veda       │
                      │ mes_inicio      │
                      └─────────────────┘
```

---

## 3. Lazy Loading (Carga Perezosa)

### 3.1 ¿Qué es?

Por defecto, JPA/Hibernate **no carga** las relaciones automáticamente cuando consultas una entidad.

```java
@ManyToOne(fetch = FetchType.LAZY)  // Por defecto es LAZY
@JoinColumn(name = "pez_id")
private Pez pez;  // Este campo NO se carga automáticamente
```

### 3.2 ¿Por qué existe?

**Performance.** Si cargaras 100 regulaciones, no quieres hacer 100 queries adicionales para cargar el pez relacionado (problema N+1).

```sql
-- Query principal
SELECT * FROM regulacion;

-- En lugar de hacer esto por cada regulacion (N+1 problem):
SELECT * FROM pez WHERE id = 1;
SELECT * FROM pez WHERE id = 2;
SELECT * FROM pez WHERE id = 3;
-- ... 100 queries!
```

### 3.3 Tipos de Fetch

| Tipo | Comportamiento | Cuando usarla |
|------|----------------|--------------|
| `LAZY` | Carga bajo demanda | Relaciones que no siempre necesitas |
| `EAGER` | Carga inmediatamente | Datos que SIEMPRE necesitas |

---

## 4. El Proxy de Hibernate

### 4.1 ¿Qué es un Proxy?

Cuando JPA retorna una entidad con `@ManyToOne(fetch = LAZY)`, Hibernate no retorna el objeto real. En su lugar, retorna un **proxy** (sustituto).

```java
// Lo que JPA retorna cuando haces:
// regulacion.getPez()

// No es un Pez real, es un proxy:
// Pez$HibernateProxy@7f8a1234
```

### 4.2 ¿Por qué un Proxy?

El proxy permite "simular" que tienes el objeto sin necesidad de cargarlo de la BD. Solo cuando intentas **acceder a un método** (como `getNombreComun()`), el proxy va a la BD.

```java
Pez proxy = regulacion.getPez();  // No va a la BD

String nombre = proxy.getNombreComun();  // AQUÍ va a la BD (si el session está abierto)
```

### 4.3 El Problema del Session

Hibernate necesita una **Session** (conexión a la BD) para inicializar el proxy. Cuando el código sale del método del controller, el session se cierra.

```
┌─────────────────────────────────────────────────────┐
│ TRANSACCIÓN / SESSION                               │
│                                                     │
│  @GetMapping                                        │
│  public ResponseEntity<List<Regulacion>> ... {      │
│      List<Regulacion> regs = repo.findAll();       │
│                                                     │
│      // Aquí el session está ABIERTO               │
│      // proxy.getNombreComun() FUNCIONA            │
│                                                     │
│      return ResponseEntity.ok(regs);                │
│  }  // Session se CIERRA aquí                      │
│                                                     │
│  Jackson serializa el JSON                          │
│  → proxy.getNombreComun() → ERROR! Session cerrado │
└─────────────────────────────────────────────────────┘
```

---

## 5. Soluciones Posibles

### 5.1 Comparación de Soluciones

| Solución | Pros | Contras |
|----------|------|---------|
| `EAGER` en relaciones | Simple, rápido de implementar | N+1 queries, mal rendimiento |
| `@JsonIgnoreProperties` | Rápido | Expone Hibernate internals, solo parchó el síntoma |
| `@Transactional` en controller | Funciona | Mala práctica, controller debería ser delgado |
| **DTOs + JOIN FETCH** | Limpio, eficiente, escalable | Más código |

### 5.2 Por qué NO EAGER

```java
// ❌ MAL - Carga todo aunque no lo necesites
@ManyToOne(fetch = EAGER)
private Pez pez;
```

```sql
-- Query para regulacion/pez/1
SELECT r.*, p.* FROM regulacion r 
LEFT JOIN pez p ON p.id = r.pez_id 
WHERE r.pez_id = 1

-- Query para regulacion/zona/1  
SELECT r.*, p.*, z.* FROM regulacion r 
LEFT JOIN pez p ON p.id = r.pez_id 
LEFT JOIN zona z ON z.id = r.zona_id 
WHERE r.zona_id = 1
```

Funciona, pero si necesitas 100 regulaciones, cargas 100 peces y 100 zonas aunque el JSON solo pida `pez_id`.

---

## 6. Solución Implementada: DTOs + JOIN FETCH

### 6.1 Data Transfer Objects (DTO)

Un DTO es un objeto cuya única finalidad es **transportar datos** entre capas.

```java
// ❌ Entidad JPA - representa la tabla de BD
public class Regulacion {
    @Id private Long id;
    @ManyToOne private Pez pez;      // Proxy Hibernate
    @ManyToOne private Zona zona;   // Proxy Hibernate
    // ... muchos otros campos
}

// ✅ DTO - solo los datos que necesito para el JSON
public record RegulacionDto(
    Long id,
    Long pezId,       // Solo IDs, no objetos
    String pezNombre, // Datos planos, no proxies
    Long zonaId,
    String zonaNombre,
    // ... solo lo que necesito enviar
) {}
```

### 6.2 ¿Por qué no exponer entidades?

| Aspecto | Entidad JPA | DTO |
|---------|------------|-----|
| Relaciones | Proxies de Hibernate | Datos planos |
| Session | Necesita sesión abierta | No necesita |
| Serialización JSON | Problemas con proxies | Sin problemas |
| Acoplamiento | Acoplado a BD | Independiente del schema |

### 6.3 Conversión Entidad → DTO

```java
public record RegulacionDto(
    Long id,
    Long pezId,
    String pezNombre,
    // ...
) {
    // Método factory para convertir
    public static RegulacionDto fromEntity(Regulacion r) {
        return new RegulacionDto(
            r.getId(),
            r.getPez().getId(),           // Accede al proxy - necesita session
            r.getPez().getNombreComun(), // Accede al proxy - necesita session
            // ...
        );
    }
}
```

**Problema:** `fromEntity` sigue necesitando el proxy inicializado. Por eso usamos `JOIN FETCH`.

---

## 7. JOIN FETCH: La Query Perfecta

### 7.1 ¿Qué es?

`JOIN FETCH` es una query de Hibernate que carga las relaciones junto con la entidad principal en UNA sola query.

```java
// ❌ Query simple - NO carga relaciones
@Query("SELECT r FROM Regulacion r WHERE r.pez.id = :pezId")
List<Regulacion> findByPezId(Long pezId);

// ✅ JOIN FETCH - carga r.pez Y r.zona en la misma query
@Query("SELECT r FROM Regulacion r JOIN FETCH r.pez JOIN FETCH r.zona WHERE r.pez.id = :pezId")
List<Regulacion> findByPezId(Long pezId);
```

### 7.2 La Query SQL Generada

```sql
-- JOIN FETCH genera:
SELECT r.id, r.pez_id, r.zona_id, ..., 
       p.id, p.nombre_comun, ...,
       z.id, z.nombre, ...
FROM regulacion r
LEFT JOIN pez p ON p.id = r.pez_id
LEFT JOIN zona z ON z.id = r.zona_id
WHERE r.pez_id = 1
```

Todo en **UNA sola query**, no N+1.

### 7.3 ¿Por qué LEFT JOIN?

```java
@ManyToOne  // Relación opcional
private Zona zona;
```

Si una regulación no tiene zona asignada (`zona_id = NULL`):
- `INNER JOIN` excluiría esa regulación
- `LEFT JOIN` la incluye con `zona = NULL`

---

## 8. Flujo Completo de la Solución

```
┌──────────────────────────────────────────────────────────────┐
│ SOLUCIÓN: DTOs + JOIN FETCH                                 │
│                                                              │
│ 1. Repository define query con JOIN FETCH                    │
│    @Query("SELECT r FROM Regulacion r                        │
│            JOIN FETCH r.pez JOIN FETCH r.zona                │
│            WHERE r.pez.id = :pezId")                        │
│                                                              │
│ 2. Hibernate ejecuta UNA query con todos los datos necesarios │
│    SELECT r.*, p.*, z.* FROM regulacion r                   │
│    LEFT JOIN pez p ON ... LEFT JOIN zona z ON ...            │
│                                                              │
│ 3. El resultado son entidades CON sus relaciones              │
│    INICIALIZADAS (no proxies)                                │
│                                                              │
│ 4. Controller convierte a DTO                                │
│    List<RegulacionDto> dtos = regulacionRepository...        │
│        .stream()                                             │
│        .map(RegulacionDto::fromEntity)                       │
│        .toList();                                            │
│                                                              │
│ 5. Jackson serializa el DTO (sin problemas de session)       │
│    → JSON Response                                           │
└──────────────────────────────────────────────────────────────┘
```

---

## 9. Estructura de Packages Resultante

```
com.pescayucatan.api_pesca_merida
├── controller/
│   ├── ZonaController.java
│   ├── RegulacionController.java      # Retorna DTOs
│   └── PeriodoVedaController.java     # Retorna DTOs
│
├── dto/                              # NUEVO - Data Transfer Objects
│   ├── RegulacionDto.java
│   └── PeriodoVedaDto.java
│
├── model/                            # Entidades JPA (mapeo BD)
│   ├── Zona.java
│   ├── Pez.java
│   ├── Regulacion.java
│   └── PeriodoVeda.java
│
└── repository/
    ├── ZonaRepository.java
    ├── RegulacionRepository.java      # JOIN FETCH queries
    └── PeriodoVedaRepository.java      # JOIN FETCH queries
```

---

## 10. Recomendaciones para el Futuro

### 10.1 Cuándo usar DTOs

| Escenario | Recomendación |
|-----------|---------------|
| API que retorna JSON | **Siempre usar DTOs** |
| Transferencia entre capas internas | Puede usar entidades |
| Datos que viajan fuera del servidor | **DTOs obligatorios** |

### 10.2 Cuándo usar JOIN FETCH

| Escenario | Recomendación |
|-----------|---------------|
| Sabes que necesitas la relación | Usar `JOIN FETCH` |
| No necesitas la relación | Query simple sin JOIN |
| Necesitas paginación | Considerar `@EntityGraph` |

### 10.3 Alternativa: `@EntityGraph`

Para casos donde no quieres escribir JPQL:

```java
@EntityGraph(attributePaths = {"pez", "zona"})
List<Regulacion> findByPezId(Long pezId);
```

Equivale a `JOIN FETCH` pero más declarativo.

---

## 11. Recursos para Profundizar

- [Documentación oficial de Hibernate sobre Fetching](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#fetching)
- [Diferencia entre Lazy y Eager](https://www.baeldung.com/hibernate-lazy-eager-loading)
- [DTOs vs Entities](https://www.baeldung.com/java-dto-pattern)

---

## Resumen de Conceptos Aprendidos

| Concepto | Definición |
|----------|------------|
| **JPA** | Java Persistence API - especificación para ORM |
| **Hibernate** | Implementación más popular de JPA |
| **Entity** | Clase Java mapeada a una tabla de BD |
| **Lazy Loading** | Carga de relaciones "bajo demanda", no automático |
| **Eager Loading** | Carga de relaciones inmediata con la entidad |
| **Proxy** | Sustituto de Hibernate que permite acceso perezoso |
| **Session** | Conexión/contexto de Hibernate hacia la BD |
| **LazyInitializationException** | Error cuando intentas acceder a un proxy sin session |
| **DTO** | Data Transfer Object - objeto solo para transportar datos |
| **JOIN FETCH** | Query que carga relaciones junto con la entidad principal |

---

Última actualización: 2026-04-07
