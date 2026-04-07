# FRONTEND_PLAN.md - API Pesca Yucatán

> Plan para el desarrollo del frontend React + TypeScript

---

## 1. Análisis del Estado Actual

### 1.1 Backend API (Completado ~80%)

El backend de Spring Boot está funcional con las siguientes capacidades:

| Endpoint | Método | Descripción | Estado |
|----------|--------|-------------|--------|
| `/api/v1/peces` | GET | Lista todos los peces | ✅ Funcional |
| `/api/v1/peces/{id}` | GET | Obtener pez por ID | ✅ Funcional |
| `/api/v1/ingestion/trigger` | POST | Trigger manual de ingesta | ✅ Funcional |
| `/api/v1/ingestion/status` | GET | Historial de ingestiones | ✅ Funcional |
| `/api/v1/ingestion/latest` | GET | Última ingesta | ✅ Funcional |
| `/api/v1/ingestion/stats` | GET | Estadísticas | ✅ Funcional |
| `/api/v1/ingestion/health` | GET | Health check | ✅ Funcional |

**Modelo de Datos Real:**

```
pez                    zona                    regulacion
├── id                 ├── id                  ├── id
├── nombreComun        ├── nombre              ├── pez_id (FK)
├── nombreCientifico    ├── macroZona           ├── zona_id (FK)
├── nombreMaya          ├── tipoRestriccion      ├── categoriaPesca
├── descripcion         ├── categoriaHidrica    ├── tallaMinima
├── riesgoCiguatera     ├── esAnp               ├── tallaMaxima
├── esInvasiva          ├── municipioSede        ├── tipoMedicion
├── esProtegida         └── notasEspecificas    ├── cuotaDiaria
├── tipoAgua                                   └── requierePermiso
└── migratorio

periodo_veda            arte_pesca             ingestions_logs
├── id                  ├── id                  ├── id
├── regulacion_id (FK)  ├── regulacion_id (FK) ├── nombreArchivo
├── tipoVeda            └── nombre              ├── hashSha256
├── mesInicio           └── esProhibido          ├── totalFilas
├── diaInicio                                    ├── filasExitosas
├── mesFin                                       ├── filasError
├── diaFin                                       ├── estado
└── fuenteDof                                   ├── procesadoEn
                                                └── detalleError
```

### 1.2 Brechas Identificadas del Backend

**Antes de iniciar frontend, el backend necesita:**

| Prioridad | Controlador | Endpoint | Justificación |
|-----------|-------------|----------|---------------|
| ALTA | `ZonaController` | `GET /api/v1/zonas` | Lista de zonas para filtros |
| ALTA | `ZonaController` | `GET /api/v1/zonas/{id}` | Detalle de zona con sus especies |
| ALTA | `RegulacionController` | `GET /api/v1/regulaciones/pez/{pezId}` | Regulaciones de un pez |
| ALTA | `RegulacionController` | `GET /api/v1/regulaciones/zona/{zonaId}` | Regulaciones en una zona |
| ALTA | `PeriodoVedaController` | `GET /api/v1/periodos-veda/actuales` | Dashboard: "¿Qué se puede pescar hoy?" |
| MEDIA | `PeriodoVedaController` | `GET /api/v1/periodos-veda/pez/{pezId}` | Vedas de un pez para timeline |
| MEDIA | `RegulacionController` | `GET /api/v1/regulaciones` | Posible paginación futura |

**Arquitectura de Controladores:**

```
pez (solo datos del pez)
   └── PezController (existente)

zona (solo datos de la zona)
   └── ZonaController (NUEVO)
       ├── GET /api/v1/zonas
       └── GET /api/v1/zonas/{id}

regulacion (pez + zona + reglas de pesca)
   └── RegulacionController (NUEVO)
       ├── GET /api/v1/regulaciones/pez/{pezId}
       └── GET /api/v1/regulaciones/zona/{zonaId}

periodo_veda (vedas por regulacion)
   └── PeriodoVedaController (NUEVO)
       ├── GET /api/v1/periodos-veda/actuales
       └── GET /api/v1/periodos-veda/pez/{pezId}
```

**Por qué esta estructura:**

1. **`RegulacionController`** - La tabla `regulacion` es el join table natural entre `pez` y `zona`. Filtrar peces por zona = `SELECT * FROM regulacion WHERE zona_id = ?`. No necesita pasar por `pez`.

2. **`PeriodoVedaController`** - Las vedas pertenecen a una regulación, no directamente a un pez. Para saber si un pez está en veda: `pez → regulaciones → periodos_veda`.

3. **`ZonaController`** - Zona es entidad independiente con sus propios datos (macroZona, tipoRestriccion, etc).

**Lógica para "especies abiertas hoy":**
```
1. PeriodoVedaController/actuales → lista de periodo_veda activos AHORA
2. Por cada periodo_veda, obtener su regulacion.pez → lista de peces en veda
3. Resto de peces = abiertos
```

### 1.3 ¿Es buen momento para trabajar en frontend?

**Respuesta: Sí, pero con tareas pendientes del backend primero.**

| Factor | Evaluación |
|--------|------------|
| **API estable** | ✅ Los endpoints existentes no cambiarán |
| **Modelo de datos** | ✅ Definido y en DB (puede crecer, pero lo básico no cambia) |
| **Autenticación** | ✅ HTTP Basic implementada |
| **Falta de endpoints** | ⚠️ Necesarios para frontend completo |

**Recomendación:** Comenzar frontend **paralelo** al desarrollo de endpoints faltantes. El frontend puede:
1. Implementar las vistas con endpoints existentes primero
2. Usar datos mock para funcionalidades dependientes de endpoints faltantes
3. Integrar los endpoints reales conforme se implementen

---

## 2. Site Map Propuesto

```
/
├── /                          (Dashboard - Inicio)
├── /especies                  (Catálogo de Especies)
│   └── /especies/:id         (Detalle de Especie)
├── /zonas                     (Mapa de Zonas)
│   └── /zonas/:id            (Detalle de Zona)
├── /admin                     (Panel de Administración)
│   ├── /admin/ingestion       (Gestión de ingesta)
│   └── /admin/stats           (Estadísticas)
├── /login                     (Inicio de Sesión)
└── /acerca                   (Acerca de)
```

**Por qué esta estructura:**

1. **`/especies` en vez de `/peces`**: Más descriptivo para usuarios finales ("catálogo de especies" vs "lista de peces")
2. **`/zonas` separado**: Las zonas son primera clase, no solo filtros - el pescador quiere saber qué hay en cada zona
3. **`/admin/ingestion` vs `/admin`**: Separa concerns - administración de ingesta vs stats
4. **`/login` explícito**: HTTP Basic no tiene página de login; el frontend debe proporcionarla

---

## 3. Vistas Detalladas

### 3.1 Dashboard (/)

**Pregunta que responde:** "¿Qué puedo pescar hoy?"

**Wireframe:**
```
┌─────────────────────────────────────────────┐
│ 🐟 Pesca Yucatán           [🔔] [👤 Admin]  │
├─────────────────────────────────────────────┤
│                                             │
│  Jueves 7 de Abril, 2026                    │
│  ─────────────────────────────────────────  │
│                                             │
│  🔍 Buscar especie...                       │
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │  ABIERTAS A LA PESCA (12)      [Ver +] ││
│  │  ┌───────┐ ┌───────┐ ┌───────┐         ││
│  │  │ Mero  │ │Barra- │ │ Jurel │         ││
│  │  │  🟢   │ │cuda 🟢│ │  🟢   │         ││
│  │  └───────┘ └───────┘ └───────┘         ││
│  └─────────────────────────────────────────┘│
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │  EN VEDA HOY (8)              [Ver +]  ││
│  │  ┌───────┐ ┌───────┐ ┌───────┐         ││
│  │  │Pargo  │ │Tibur. │ │Papa-  │         ││
│  │  │  🔴   │ │  🔴   │ │llo 🔴 │         ││
│  │  │→15/May│ │→30/Jun│ │→31/May│         ││
│  │  └───────┘ └───────┘ └───────┘         ││
│  └─────────────────────────────────────────┘│
│                                             │
│  📍 Filtrar por zona:                      │
│  [Todas] [Costa Norte] [Costa Sur] [Ría]   │
│                                             │
└─────────────────────────────────────────────┘
```

**Componentes:**
- `<Header>` - Logo, notificaciones, usuario
- `<SearchBar>` - Búsqueda con debounce 300ms (SKILL: `async-cheap-condition-before-await`)
- `<SpeciesSection>` - Sección agrupada con "Ver más"
- `<SpeciesCard>` - Imagen, nombre, estado (verde/rojo), zona
- `<ZoneFilterChips>` - Chips horizontales scrolleables

**Fuentes de datos:**
1. `GET /api/v1/peces` → lista completa de especies
2. `GET /api/v1/periodos-veda/actuales` → periodos de veda activos ahora
3. `GET /api/v1/regulaciones` → para cruzar pez↔zona
4. `GET /api/v1/zonas` → para chips de filtro

**Por qué este diseño:**
- **Estado visual prominente**: El verde/rojo es lo primero que el pescador necesita ver - está abierta o cerrada la pesca
- **Búsqueda prominente**: El usuario sabe lo que busca (nombre común), no explora siempre
- **Agrupación por estado**: Más útil que lista plana - "qué puedo pescar" vs "todo"
- **Chips de zona**: Filtro rápido sin entrar a otra página

---

### 3.2 Catálogo de Especies (/especies)

**Pregunta que responde:** "¿Qué especies hay en el sistema?"

**Wireframe:**
```
┌─────────────────────────────────────────────┐
│ ← Volver                    🐟 Especies 🔍 │
├─────────────────────────────────────────────┤
│                                             │
│  [Costa Norte] [Costa Sur] [Ría] [Cenote]  │
│  ─────────────────────────────────────────  │
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │ 🖼️  ┌───────────────────────────────┐  ││
│  │[IMG]│ MERO                           │  ││
│  │     │ Epinephelus morio               │  ││
│  │     │ ───────────────────────────────│  ││
│  │     │ 🟢 Abierta | 📍 Costa Norte    │  ││
│  │     │ Talla mín: 60cm | 🎣 Jureo    │  ││
│  │     └───────────────────────────────┘  ││
│  └─────────────────────────────────────────┘│
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │ 🖼️  ┌───────────────────────────────┐  ││
│  │[IMG]│ PARGO ROJO                     │  ││
│  │     │ Lutjanus campechanus            │  ││
│  │     │ ───────────────────────────────│  ││
│  │     │ 🔴 En Veda | 📍 Costa Sur      │  ││
│  │     │ Veda: 1 Mar - 15 May           │  ││
│  │     └───────────────────────────────┘  ││
│  └─────────────────────────────────────────┘│
│                                             │
│           [ Cargar más... ]                 │
│                                             │
└─────────────────────────────────────────────┘
```

**Componentes:**
- `<SpeciesGrid>` - Grid responsivo (1 col móvil, 2 tablet, 3 desktop)
- `<SpeciesCard>` - Imagen, nombre, estado, zona, técnica
- `<ZoneFilterBar>` - Filtros horizontales (SKILL: `rendering-content-visibility`)
- `<LoadingSkeleton>` - Placeholder durante carga
- `<EmptyState>` - "No hay especies para este filtro"

**Optimizaciones (SKILL.md):**
- `async-parallel`: `Promise.all([fetchPeces(), fetchZonas()])`
- `bundle-dynamic-imports`: Lazy load imágenes fuera de viewport
- `server-parallel-fetching`: Componente puede hacer fetch independiente

**Por qué grid en vez de lista:**
- **Más visual**: Los pescadores reconocen especies por imagen, no por nombre
- **Densidad**: Más especies visibles sin scroll
- **Escaneo rápido**: El ojo encuentra patrones (color del estado) rápido

---

### 3.3 Detalle de Especie (/especies/:id)

**Pregunta que responde:** "¿Cuáles son las reglas para pescar esta especie?"

**Wireframe:**
```
┌─────────────────────────────────────────────┐
│ ← Volver                         🐟 Mero   │
├─────────────────────────────────────────────┤
│                                             │
│        ┌──────────────────────┐            │
│        │                      │            │
│        │     [IMAGEN]         │            │
│        │                      │            │
│        └──────────────────────┘            │
│                                             │
│  MERO                                       │
│  Epinephelus morio                          │
│  Chaktun (nombre maya)                      │
│  ─────────────────────────────────────────  │
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │ 📍 Zona        │ Costa Norte de Yucatán ││
│  │ 🏠 Hábitat    │ Arrecifes coralinos   ││
│  │ 📏 Talla      │ 60cm - 90cm           ││
│  │ 🎣 Técnica    │ Jureo con carnada viva ││
│  │ ⚠️ Ciguatera  │ Riesgo medio          ││
│  │ 🐟 Invasiva   │ No                    ││
│  └─────────────────────────────────────────┘│
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │  📅 PERÍODOS DE VEDA                    ││
│  │  ───────────────────────────────────────││
│  │                                          ││
│  │     Ene  Feb  Mar  Abr  May  Jun  Jul   ││
│  │  ━━━━╋━━━━━╋━━━━━╋━━━━━╋━━━━━╋━━━━━╋  ││
│  │      |█████|                         [●]││
│  │            └── VEDA FIJA ──┘            ││
│  │          1 Mar - 15 May                 ││
│  │                                          ││
│  └─────────────────────────────────────────┘│
│                                             │
│  📄 Fuente: DOF 2025-02-15                  │
│  🔄 Actualizado: 7 Abr 2026                │
│                                             │
└─────────────────────────────────────────────┘
```

**Componentes:**
- `<SpeciesHeader>` - Imagen hero, nombre, nombre maya
- `<SpeciesInfoGrid>` - Datos técnicos en grid 2x3
- `<VedaTimeline>` - Visualización mensual con rangos
- `<VedaStatusBadge>` - Abierta/En Veda
- `<SourceFooter>` - Metadata de fuente DOF

**Por qué este diseño:**
- **Info grid 2 columnas**: Datos técnicos scaneables verticalmente
- **Timeline visual**: Más intuitivo que "1 Mar - 15 May" en texto
- **Nombre maya prominente**: Valor cultural, no esconderlo
- **Fuente DOF visible**: El pescador quiere saber de dónde viene la info (legitimidad)

---

### 3.4 Zonas (/zonas)

**Pregunta que responde:** "¿Qué se puede pescar en cada zona?"

**Wireframe:**
```
┌─────────────────────────────────────────────┐
│                    📍 Zonas de Pesca        │
├─────────────────────────────────────────────┤
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │  🏝️ COSTA NORTE                         ││
│  │  Arrecifes y aguas abiertas            ││
│  │  ──────────────────────────────────────││
│  │  15 especies | 3 en veda               ││
│  │  [Ver especies →]                      ││
│  └─────────────────────────────────────────┘│
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │  🌊 COSTA SUR                           ││
│  │  Aguas tranquilas, manglares           ││
│  │  ──────────────────────────────────────││
│  │  12 especies | 5 en veda               ││
│  │  [Ver especies →]                      ││
│  └─────────────────────────────────────────┘│
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │  🏞️ RÍA                                 ││
│  │  Estuarios y aguas interiores         ││
│  │  ──────────────────────────────────────││
│  │  8 especies | 1 en veda                ││
│  │  [Ver especies →]                      ││
│  └─────────────────────────────────────────┘│
│                                             │
└─────────────────────────────────────────────┘
```

**Componentes:**
- `<ZoneCard>` - Nombre, descripción, contador de especies, link
- `<ZoneStats>` - Pills con contadores

**Por qué vista separada:**
- **El pescador piensa en zonas**: "Voy a la Costa Norte" antes que "Voy a pescar Mero"
- **Contexto geográfico**: Cada zona tiene identidad propia
- **Estadísticas de veda por zona**: Útil para planeación de viaje

---

### 3.5 Login (/login)

**Por qué explícito:**
- HTTP Basic no tiene página de login nativa
- El frontend necesita manejar credenciales y sesión
- Cooluri de_redirect después de login

**Wireframe:**
```
┌─────────────────────────────────────────────┐
│                                             │
│           🐟                                │
│      Pesca Yucatán                          │
│                                             │
│      ─────────────────────────────────     │
│                                             │
│      Usuario                                │
│      ┌─────────────────────────────────┐    │
│      │                                 │    │
│      └─────────────────────────────────┘    │
│                                             │
│      Contraseña                             │
│      ┌─────────────────────────────────┐    │
│      │                                 │    │
│      └─────────────────────────────────┘    │
│                                             │
│      [      Iniciar Sesión       ]          │
│                                             │
└─────────────────────────────────────────────┘
```

---

### 3.6 Admin - Ingesta (/admin/ingestion)

**Pregunta que responde:** "¿Está la data actualizada? ¿Hay errores?"

```
┌─────────────────────────────────────────────┐
│ ← Volver              Panel Admin 🔓        │
├─────────────────────────────────────────────┤
│                                             │
│  ESTADO DE INGESTA                           │
│  ─────────────────────────────────────────  │
│  🟢 Última ingesta: Hace 2 horas           │
│  ✓ 1,245 especies | 89 regulaciones        │
│                                             │
│  ┌─────────────────────────────────────────┐│
│  │  📊 HISTORIAL DE INGESTIONES            ││
│  │  ───────────────────────────────────────││
│  │  7/Abr 14:00  ✓ COMPLETADO  1,245 filas││
│  │  6/Abr 03:00  ✓ COMPLETADO  1,244 filas││
│  │  5/Abr 03:00  ⚠ ERROR     1,200 filas ││
│  │  4/Abr 03:00  ✓ COMPLETADO  1,200 filas││
│  └─────────────────────────────────────────┘│
│                                             │
│  ACCIONES                                   │
│  ─────────────────────────────────────────  │
│  [ 🔄 Trigger Ingesta Manual ]              │
│  [ 📊 Ver Estadísticas Completas ]          │
│                                             │
└─────────────────────────────────────────────┘
```

**Componentes:**
- `<IngestionStatusCard>` - Estado actual con timestamp
- `<IngestionHistoryTable>` - Lista de ejecuciones
- `<TriggerButton>` - Botón con loading state

**Por qué no drag & drop de PDFs:**
- El pipeline de ingesta ya está automatizado desde Google Sheets
- Ya NO hay extracción de PDFs (Python eliminado)
- El admin solo necesita monitorear y triggerear si es necesario

---

## 4. Stack Tecnológico

### 4.1 Decisiones Principales

| Librería | Elección | Por qué |
|----------|----------|---------|
| **Framework** | React 18 + Vite | Ecosistema maduro, HMR rápido, opt-in a RSC futuro |
| **TypeScript** | Sí (obligatorio) | El backend ya tiene tipos claros; reduce bugs |
| **Styling** | Tailwind CSS + Shadcn/UI | Productividad, consistencia, accesible |
| **Routing** | React Router 6 | Estándar de facto |
| **Data Fetching** | TanStack Query | Cache, dedup, loading states out of box |
| **State** | Zustand (mínimo) | Solo para UI state, no global |
| **Icons** | Lucide React | Tree-shakeable, consistente |

### 4.2 Por qué NO otras opciones

| Alternativa | Por qué NO |
|-------------|------------|
| **Next.js** | Overkill - SPA simple, no SSR necesario ahora |
| **SWR** | TanStack Query tiene más features (pagination, infinite queries) |
| **Redux** | Demasiado boilerplate para este proyecto |
| **CSS Modules** | Tailwind es más productivo para UI responsiva |
| **styled-components** | Runtime CSS = menor performance |

### 4.3 Dependencias Completas

```json
{
  "dependencies": {
    "react": "^18.3.0",
    "react-dom": "^18.3.0",
    "react-router-dom": "^6.22.0",
    "@tanstack/react-query": "^5.28.0",
    "zustand": "^4.5.0",
    "tailwindcss": "^3.4.0",
    "@radix-ui/react-*": "shadcn dependencies",
    "lucide-react": "^0.358.0",
    "clsx": "^2.1.0",
    "date-fns": "^3.6.0",
    "react-hook-form": "^7.51.0",
    "zod": "^3.22.0",
    "@hookform/resolvers": "^3.3.0"
  },
  "devDependencies": {
    "typescript": "^5.4.0",
    "vite": "^5.2.0",
    "@types/react": "^18.3.0",
    "vitest": "^1.4.0",
    "playwright": "^1.42.0",
    "tailwindcss-animate": "^1.0.0"
  }
}
```

---

## 5. Roadmap

### Fase 1: Foundation (Semanas 1-2)

**Objetivo:** Proyecto base funcional con navegación

```
Semana 1:
├── [ ] Inicializar Vite + React + TypeScript
├── [ ] Configurar Tailwind CSS + Shadcn/UI
├── [ ] Configurar React Router con site map
├── [ ] Implementar layout base (Header, navegación)
└── [ ] Implementar API client con TanStack Query

Semana 2:
├── [ ] Vista: Dashboard (con datos mock)
├── [ ] Vista: Catálogo de Especies (con datos mock)
├── [ ] Vista: Login
├── [ ] Integrar with real API (pez endpoint)
└── [ ] Responsive mobile-first testing
```

**Entregable:** App navegable con datos reales de `/api/v1/peces`

### Fase 2: Core Features (Semanas 3-4)

**Objetivo:** Vistas completas con datos reales

```
Semana 3:
├── [ ] Backend: ZonaController (zonas, zonas/{id})
├── [ ] Backend: RegulacionController (regulaciones/pez/{id}, regulaciones/zona/{id})
├── [ ] Backend: PeriodoVedaController (periodos-veda/actuales, periodos-veda/pez/{id})
├── [ ] Vista: Detalle de Especie con regulaciones
├── [ ] Dashboard actualizado con vedas actuales
└── [ ] Filtros por zona funcionales

Semana 4:
├── [ ] Vista: Zonas
├── [ ] Vista: Detalle de Zona
├── [ ] Link species <-> zones
├── [ ] Empty states y loading states
└── [ ] Testing E2E con Playwright (dashboard, especies)
```

**Entregable:** App completa funcional para usuario público

### Fase 3: Admin & Polish (Semanas 5-6)

**Objetivo:** Panel admin y refinamiento UX

```
Semana 5:
├── [ ] Vista: Login funcional con HTTP Basic
├── [ ] Endpoint: /api/v1/ingestion/stats
├── [ ] Vista: Admin - Estado de Ingesta
├── [ ] Botón trigger manual
├── [ ] Historial de ingestiones
└── [ ] Manejo de errores (toasts, retry)

Semana 6:
├── [ ] Optimizaciones de performance (SKILL rules)
├── [ ] Testing adicional (edge cases)
├── [ ] Responsive testing tablet/desktop
├── [ ] Deploy a staging en VPS
└── [ ] Documentación
```

**Entregable:** Listo para producción en VPS

---

## 6. Configuración de Deploy (VPS)

### 6.1 Arquitectura Propuesta

```
                    ┌─────────────────┐
                    │   VPS (Ubuntu)  │
                    │                 │
Browser ──────────► │  ┌───────────┐  │
                    │  │  Nginx    │  │
                    │  │ (Reverse  │  │
                    │  │  Proxy)   │  │
                    │  └─────┬─────┘  │
                    │        │        │
                    │  ┌─────▼─────┐  │
                    │  │ Spring    │  │
                    │  │ Boot JAR  │  │
                    │  │ :8080     │  │
                    │  └───────────┘  │
                    └─────────────────┘
```

### 6.2 Comandos de Deploy

```bash
# Build frontend
cd frontend && npm run build

# Copiar a VPS (via scp o git pull en server)
# Nginx sirve los archivos estáticos del build

# Backend (systemd service)
sudo systemctl restart api-pesca
```

### 6.3 Variables de Entorno Necesarias

```env
# Backend
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pescayucatan
# CORS origins punto a dominio real
cors.allowed.origins="https://pesca-merida.com"

# Frontend
VITE_API_BASE_URL=https://api.pesca-merida.com
```

---

## 7. Consideraciones de Diseño

### 7.1 High Contrast Mode (Uso Exterior)

El usuario típico usa la app en la playa/sol. Diseñar para:
- **Contraste mínimo 4.5:1** para texto
- **Colores de estado bien diferenciados**: Verde (#22c55e) vs Rojo (#ef4444)
- **Touch targets mínimo 44x44px**
- **Iconografía grande y clara**

### 7.2 Paleta de Colores

```css
:root {
  /* Estado - Veda */
  --open: #22c55e;      /* Verde - Abierta */
  --closed: #ef4444;     /* Rojo - En Veda */
  --warning: #f59e0b;   /* Amarillo - Warning */
  
  /* UI */
  --background: #ffffff;
  --surface: #f8fafc;
  --text-primary: #0f172a;
  --text-secondary: #64748b;
  --border: #e2e8f0;
  
  /* Brand */
  --primary: #0369a1;    /* Azul océano */
  --primary-light: #e0f2fe;
}
```

### 7.3 Performance Targets

| Métrica | Target |
|---------|--------|
| **LCP** | < 2.5s |
| **FID** | < 100ms |
| **CLS** | < 0.1 |
| **Bundle size** | < 150KB gzipped |

---

## 8. Errores a Evitar

| Error Común | Prevención |
|-------------|------------|
| **Over-engineering** | Empezar simple, agregar complejidad solo cuando sea necesario |
| **Espera backend completo** | Trabajar con mocks, integrar después |
| **No pensar en mobile** | Mobile-first desde día 1 |
| **Ignorar errores HTTP** | TanStack Query maneja, pero diseñar UI para errores |
| **Datos no validados** | Usar Zod para validar respuesta de API |

---

## 9. Definición de "Done"

Una vista está completa cuando:
- [ ] Datos reales de API integrados
- [ ] Responsive (móvil, tablet, desktop)
- [ ] Loading state implementado
- [ ] Error state implementado (API failures)
- [ ] Empty state implementado (no data)
- [ ] Tests E2E pasando
- [ ] Sin errores de TypeScript

---

Última actualización: 2026-04-07
