# FRONTEND_PLAN.md - API Pesca Yucatán

## 1. Análisis de API

### Entidades Principales

**Pez (Fish)**
```
GET /api/v1/peces              - Lista todos los peces
GET /api/v1/peces/{id}        - Obtener pez por ID
GET /api/v1/peces?temporada    - Filtrar por zona/temporada
```

**EspecieVeda (Fishing Ban)**
```
GET /api/v1/vedas             - Lista todas las vedas
GET /api/v1/vedas/{id}        - Detalle de veda
GET /api/v1/vedas/actuales    - Vedas activas hoy
```

**Ingestion (Admin)**
```
POST /api/v1/ingestion/upload - Subir PDF al microservicio Python
POST /api/v1/ingestion/trigger - Trigger manual de ingestión
```

### Modelo de Datos

**Pez**
- `id`: Long
- `nombreComun`: String (e.g., "Mero")
- `especie`: String (scientific name)
- `nombreMaya`: String
- `tallaMinima`: String (e.g., "60 cm")
- `habitat`: String
- `tecnicaRecomendada`: String
- `zona`: String

**EspecieVeda**
- `id`: Long
- `pez`: Pez (relación)
- `zona`: String
- `tipoVeda`: Enum (TEMPORAL_FIJA, TEMPORAL_VARIABLE, PERMANENTE, PLURIANUAL)
- `inicioFijo`/`finFijo`: LocalDate (para veda fija)
- `inicioMes`/`inicioDia`/`finMes`/`finDia`: Integer (para veda cíclica)
- `cancelada`: Boolean
- `motivoCancelacion`: String
- `fuenteDof`: String

---

## 2. Site Map

```
/
├── /                     (Dashboard - Inicio)
├── /especies             (Catálogo de Especies)
│   └── /especies/:id     (Detalle de Especie)
├── /mapa                 (Mapa de Zonas - futuro)
├── /admin                (Panel de Administración)
│   ├── /admin/ingestion  (Gestión de PDFs)
│   └── /admin/actualizar (Trigger manual)
└── /acerca               (Acerca de la App)
```

### Navegación

**Móvil (Bottom Navigation Bar)**
```
┌─────────────────────────────────┐
│                                 │
│         [CONTENT AREA]          │
│                                 │
├─────────────────────────────────┤
│  🏠     📋     📤     ⚙️        │
│ Inicio  Especies  Admin Acerca  │
└─────────────────────────────────┘
```

**Escritorio (Sidebar)**
```
┌──────────┬──────────────────────────────┐
│          │                              │
│ 🐟       │      [CONTENT AREA]          │
│ Inicio   │                              │
│          │                              │
│ 📋       │                              │
│ Especies │                              │
│          │                              │
│ 📤       │                              │
│ Admin    │                              │
│          │                              │
│ ℹ️        │                              │
│ Acerca   │                              │
└──────────┴──────────────────────────────┘
```

---

## 3. Definición de Vistas

### 3.1 Dashboard / Inicio

**Objetivo**: Responder "¿Qué se puede pescar hoy?"

**Wireframe (Texto)**
```
┌────────────────────────────────────────┐
│ 🔍 Buscar especie...              🔔  │
├────────────────────────────────────────┤
│                                        │
│  📅 Jueves 2 de Abril, 2026           │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│                                        │
│  ✅ ABIERTAS A LA PESCA (12)          │
│  ┌────────┐ ┌────────┐ ┌────────┐     │
│  │  Mero  │ │ Barrac.│ │  Jurel │     │
│  │  ✅    │ │  ✅    │ │  ✅    │     │
│  └────────┘ └────────┘ └────────┘     │
│                                        │
│  🚫 EN VEDA (8)                        │
│  ┌────────┐ ┌────────┐ ┌────────┐     │
│  │ Pargo  │ │ Tibur. │ │ Papag. │     │
│  │ 🔴    │ │ 🔴    │ │ 🔴    │     │
│  │ Hasta │ │ Hasta │ │ Hasta │     │
│  │ 15/May│ │ 30/Jun│ │ 31/May│     │
│  └────────┘ └────────┘ └────────┘     │
│                                        │
│  📍 Zonas: [Todo] [Norte] [Sur] [Este]│
│                                        │
└────────────────────────────────────────┘
```

**Componentes**:
- `<SearchBar>` - Búsqueda rápida con debounce (300ms)
- `<DateHeader>` - Fecha actual con indicador de temporada
- `<SpeciesCard>` - Tarjeta con estado visual (verde=abierto, rojo=veda)
- `<ZoneFilter>` - Chips para filtrar por zona
- `<QuickStats>` - Contadores de especies abiertas/cerradas

**Comportamiento**:
- Al cargar: `Promise.all([fetchPeces(), fetchVedasActuales()])`
- Filtrado por zona actualiza cards sin reload
- Tap en card navega a `/especies/:id`

---

### 3.2 Catálogo de Especies

**Objetivo**: Lista visual browsable de todas las especies

**Wireframe (Texto)**
```
┌────────────────────────────────────────┐
│ 🐟 Catálogo de Especies         🔍    │
├────────────────────────────────────────┤
│ [Todo] [Norte] [Sur] [Costa] [Cenote]│
├────────────────────────────────────────┤
│                                        │
│ ┌──────────────────────────────────┐  │
│ │ 🖼️                              │  │
│ │ [IMG]                            │  │
│ ├──────────────────────────────────┤  │
│ │ MERO                             │  │
│ │ Epinephelus morio                 │  │
│ │ ────────────────────             │  │
│ │ 🟢 Abierta | 📍 Norte | 🎣 Jureo │  │
│ │ Talla mínima: 60 cm              │  │
│ └──────────────────────────────────┘  │
│                                        │
│ ┌──────────────────────────────────┐  │
│ │ 🖼️                              │  │
│ │ [IMG]                            │  │
│ ├──────────────────────────────────┤  │
│ │ PARGO                           │  │
│ │ Lutjanus campechanus             │  │
│ │ ────────────────────             │  │
│ │ 🔴 En Veda | 📍 Sur | 🎣 Línea  │  │
│ │ Veda: 1 Mar - 15 May            │  │
│ └──────────────────────────────────┘  │
│                                        │
│        [ Cargar más... ]              │
│                                        │
└────────────────────────────────────────┘
```

**Componentes**:
- `<SpeciesGrid>` - Grid responsivo (1 col móvil, 2 tablet, 3 desktop)
- `<SpeciesCard>` - Imagen, nombre, estado veda, zona, técnica
- `<ZoneFilterBar>` - Filtros horizontales scrolleables
- `<LoadingSkeleton>` - Placeholder durante carga

**Optimización (SKILL.md)**:
- `async-parallel`: Cargar especies y vedas en paralelo
- `bundle-dynamic-imports`: Lazy load imágenes de tarjetas fuera de viewport
- `rendering-content-visibility`: Para listas largas
- `server-parallel-fetching`: Estructurar componentes para fetches paralelos

---

### 3.3 Detalle de Especie

**Objetivo**: Información técnica completa + calendario de veda visual

**Wireframe (Texto)**
```
┌────────────────────────────────────────┐
│ ← Volver                          ⋮   │
├────────────────────────────────────────┤
│                                        │
│        ┌──────────────────┐           │
│        │                  │           │
│        │   [IMAGEN]       │           │
│        │                  │           │
│        └──────────────────┘           │
│                                        │
│  MERO                                  │
│  Epinephelus morio                     │
│  ─────────────────────────────────────│
│                                        │
│  📍 Zona: Costa Norte de Yucatán       │
│  🏠 Hábitat: Arrecifes coralinos       │
│  📏 Talla mínima: 60 cm               │
│  🎣 Técnica: Jureo con carnada viva   │
│  🗣️ Nombre maya: Chaktun              │
│                                        │
├────────────────────────────────────────┤
│  📅 CALENDARIO DE VEDA                 │
│  ─────────────────────────────────────│
│                                        │
│       Mar   Abr   May   Jun   Jul     │
│  ━━━━╋━━━━━╋━━━━━╋━━━━━╋━━━━━╋━━━━━  │
│      |█████|                 [ACTUAL]  │
│                |<--->|                │
│              VEDA FIJA                │
│            1 Mar - 15 May              │
│                                        │
│  ┌──────────────────────────────────┐  │
│  │ ⚠️ ESTA VEDA HA SIDO CANCELADA  │  │
│  │ DOF 15-Nov-2025                  │  │
│  │ Motivo: Afluencia extrema       │  │
│  └──────────────────────────────────┘  │
│                                        │
│  ─────────────────────────────────────│
│  📄 Fuente: DOF 2025-02-15            │
│  🔄 Actualizado: 1 Mar 2026           │
│                                        │
└────────────────────────────────────────┘
```

**Componentes**:
- `<SpeciesHeader>` - Imagen, nombre común/científico
- `<SpeciesInfoGrid>` - Datos técnicos en grid
- `<VedaCalendar>` - Visualización mensual con rangos
- `<VedaStatusBadge>` - Estado actual (Abierta/En Veda/Cancelada)
- `<CancellationAlert>` - Banner si veda cancelada
- `<SourceFooter>` - Metadata de fuente y actualización

**VedaCalendar Detalle**:
```
         Ene  Feb  Mar  Abr  May  Jun
  2026   [  ][  ][███][███][███][  ]
                    └── VEDA ──┘
```

**Optimización (SKILL.md)**:
- `async-parallel`: Cargar datos de pez y vedas en paralelo
- `server-cache-react`: Usar React.cache() para dedupe de requests
- `rerender-memo`: Memoizar SpeciesCard si se reutiliza en listas
- `bundle-dynamic-imports`: Componente de calendario cargado lazily

---

### 3.4 Panel de Admin - Carga de PDFs

**Objetivo**: Interactuar con microservicio Python para extraer tablas de PDFs

**Wireframe (Texto)**
```
┌────────────────────────────────────────┐
│ ← Volver              Panel Admin 🔓  │
├────────────────────────────────────────┤
│                                        │
│  📤 CARGA DE DOCUMENTOS               │
│  ─────────────────────────────────────│
│                                        │
│  ┌──────────────────────────────────┐ │
│  │                                  │ │
│  │     📄                          │ │
│  │  Arrastra el PDF aquí           │ │
│  │  o haz clic para seleccionar    │ │
│  │                                  │ │
│  │  [Seleccionar archivo]          │ │
│  │                                  │ │
│  └──────────────────────────────────┘ │
│                                        │
│  📋 Documentos recientes:             │
│  ┌──────────────────────────────────┐ │
│  │ veda_conapesca_2026.pdf  ✓ 2/Abr │ │
│  │ veda_conapesca_2025.pdf  ✓ 15/Ene│ │
│  │ DOF_nov2025.pdf          ⚠ Error │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ─────────────────────────────────────│
│  ⚙️ ACCIONES                           │
│                                        │
│  [🔄 Trigger Ingesta Manual]           │
│  [📊 Ver Logs de Ingestión]            │
│  [💾 Backup de Base de Datos]          │
│                                        │
└────────────────────────────────────────┘
```

**Componentes**:
- `<DropZone>` - Área de drag & drop para PDFs
- `<FileUploadButton>` - Botón alternativo de selección
- `<UploadProgress>` - Barra de progreso con estado
- `<RecentDocumentsList>` - Historial de PDFs cargados
- `<AdminActionsPanel>` - Botones de acciones administrativas
- `<IngestionLogsViewer>` - Tabla de logs de proceso

**Flujo de Usuario**:
1. Usuario arrastra PDF a DropZone
2. Frontend envía a Python API (`POST /pdf-extractor/upload`)
3. Python extrae tablas → retorna datos estructurados
4. Frontend muestra preview para confirmación
5. Usuario confirma → Backend Java recibe y persiste
6. Mostrar toast de éxito/error

**Optimización (SKILL.md)**:
- `bundle-conditional`: Cargar componentes de admin solo si user tiene rol ADMIN
- `async-defer-await`: No bloquear UI durante upload
- `rerender-transitions`: Usar startTransition para updates no críticos

---

## 4. Stack Recomendado

### Core
| Librería | Propósito | Versión |
|----------|-----------|---------|
| **React 18+** | Framework UI | 18.x |
| **Vite** | Build tool y dev server | 5.x |
| **TypeScript** | Tipado estático | 5.x |
| **React Router 6** | Navegación | 6.x |

### UI y Estilos
| Librería | Propósito | Notas |
|----------|-----------|-------|
| **Tailwind CSS** | Utilidades CSS | Mobile-first, high contrast |
| **Shadcn/UI** | Componentes base | Headless, accesibles |
| **Lucide React** | Iconos | Modernos, tree-shakeable |
| **clsx** | Conditional classes | Ligero |

### Data Fetching & State
| Librería | Propósito | Notas |
|----------|-----------|-------|
| **SWR** | Data fetching con cache | Request deduplication built-in |
| **TanStack Query** | Alternative a SWR | Más features, más peso |
| **Zustand** | Estado global | Simple, performant |

### Formularios
| Librería | Propósito |
|----------|-----------|
| **React Hook Form** | Formularios performant |
| **Zod** | Validación de esquemas |

### Calendar/Date
| Librería | Propósito |
|----------|-----------|
| **date-fns** | Manipulación de fechas (lightweight) |

### PDF (Admin)
| Librería | Propósito |
|----------|-----------|
| **react-dropzone** | Drag & drop file upload |

### Testing
| Librería | Propósito |
|----------|-----------|
| **Vitest** | Unit testing |
| **Playwright** | E2E testing |

---

## 5. Flujo de Usuario

### Flujo Principal (Pescador)
```
1. Abre app → Dashboard
2. Ve especies abiertas hoy (verde)
3. Opcional: Filtra por zona
4. Tap en especie → Detalle
5. Consulta calendario de veda
6. ¿Abierta? → Va a pescar ✅
```

### Flujo Admin
```
1. Navega a Admin (requiere auth)
2. Arrastra PDF al DropZone
3. Sistema procesa con Python API
4. Preview de datos extraídos
5. Confirma → Datos persistidos
6. Notificación de éxito
```

---

## 6. Responsive Breakpoints

| Breakpoint | Dispositivo | Layout |
|------------|-------------|--------|
| `< 640px` | Móvil | Single column, Bottom Nav |
| `640px - 1024px` | Tablet | 2-column grid, Sidebar collapsed |
| `> 1024px` | Desktop | 3-column grid, Full Sidebar |

---

## 7. Consideraciones de Diseño

### High Contrast (Uso Exterior)
- Fondo claro con texto oscuro: `bg-white text-gray-900`
- Verde veda abierta: `text-green-600 bg-green-50`
- Rojo veda: `text-red-600 bg-red-50`
- Tamaño mínimo de touch target: 44x44px
- Iconografía grande y clara

### Performance (SKILL.md Rules Applied)
- **Bundle**: Dynamic imports para Shadcn, React Router lazy
- **Fetching**: SWR con deduplicación, Promise.all en paralelo
- **Rendering**: Memo de componentes pesados, content-visibility
- **Re-renders**: useDeferredValue para search, Zustand para estado mínimo

---

## 8. Próximos Pasos (Post-Plan)

1. Inicializar proyecto: `npm create vite@latest frontend -- --template react-ts`
2. Configurar Tailwind CSS + Shadcn/UI
3. Crear estructura de carpetas
4. Implementar API client con SWR
5. Desarrollar componentes base
6. Implementar vistas una por una
7. Testing E2E con Playwright

---

Última actualización: 2026-04-02
