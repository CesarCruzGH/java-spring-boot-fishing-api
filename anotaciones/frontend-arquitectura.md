# Frontend - API Pesca Yucatán

## 1. Resumen del Proyecto

El frontend es una aplicacion SPA (Single Page Application) construida con React 18, TypeScript y Vite. Esta diseñada para dispositivos moviles primero (mobile-first) y proporciona una interfaz para consultar regulaciones de pesca en Yucatan, Mexico.

### Tecnologias Principales

| Categoria | Tecnologia | Version | Proposito |
|-----------|-----------|---------|-----------|
| Framework | React | 18.3 | UI library |
| Bundler | Vite | 8.0 | Build tool y dev server |
| Lenguaje | TypeScript | 5.x | Type safety |
| Estilos | Tailwind CSS | 4.x | Utility-first CSS |
| Routing | React Router | 6.x | Navegacion SPA |
| Data Fetching | TanStack Query | 5.x | Cache y estado de servidor |
| UI Primitives | Radix UI | - | Componentes accesibles |
| Iconos | Lucide React | - | Iconos consistentes |

---

## 2. Arquitectura de Archivos

```
frontend/src/
├── api/
│   └── hooks.ts              # Custom hooks para TanStack Query
├── components/
│   ├── ui/                   # Componentes base reutilizables
│   │   ├── badge.tsx         # Etiquetas de estado (Abierta/En Veda)
│   │   ├── button.tsx        # Botones con variantes
│   │   ├── card.tsx          # Contenedores con borde y sombra
│   │   ├── input.tsx         # Campos de texto
│   │   ├── label.tsx         # Labels accesibles
│   │   └── index.ts          # Barrel exports
│   ├── Header.tsx            # Navegacion principal sticky
│   └── Layout.tsx            # Layout con header, footer y Outlet
├── lib/
│   ├── api.ts                # Cliente API y tipos TypeScript
│   └── utils.ts              # Utilidades (cn para className)
├── pages/
│   ├── Dashboard.tsx         # Pagina principal
│   ├── SpeciesCatalog.tsx    # Catalogo de especies
│   ├── SpeciesDetail.tsx     # Detalle de una especie
│   ├── Zonas.tsx             # Lista de zonas
│   ├── ZoneDetail.tsx        # Detalle de una zona
│   ├── Login.tsx             # Autenticacion HTTP Basic
│   └── AdminIngestion.tsx    # Panel de administracion
├── App.tsx                   # Configuracion del router
├── main.tsx                  # Entry point
└── index.css                 # Tailwind y theme
```

---

## 3. Patrones de Diseno

### 3.1 Mobile-First

El diseno sigue el enfoque mobile-first, optimizando primero para pantallas pequenas y expandiendose para pantallas mas grandes:

- **Touch targets minimos**: 44x44px para botones y elementos interactivos
- **Chips scrolleables**: Filtros de zona en scroll horizontal
- **Grid responsivo**: 1 columna en movil, 2 en tablet, 3 en desktop

### 3.2 Component Composition

Los componentes UI son primitivas de bajo nivel que se componen en paginas:

```tsx
// Componente primitivo
<Card>
  <CardContent>p9
    <Badge variant="open">Abierta</Badge>
  </CardContent>
</Card>

// Componente de pagina
<SpeciesCard pez={pez} onSelect={handleSelect} />
```

### 3.3 TanStack Query para Data Fetching

En lugar de usarEffect para fetching manual, se usan custom hooks basados en TanStack Query:

```tsx
// hooks.ts
export function usePeces() {
  return useQuery({
    queryKey: ['peces'],
    queryFn: api.peces.list,
  })
}

// Uso en componente
const { data: peces, isLoading } = usePeces()
```

**Beneficios:**
- Cache automatico de respuestas
- Loading states automaticos
- Retry en caso de error
- Invalidacion de cache al mutar datos

---

## 4. Sistema de Colores

El tema usa una paleta de colores definida en el CSS para garantizar consistencia:

```css
/* Estados de pesca */
--color-open: #22c55e;      /* Verde - Abierta a la pesca */
--color-closed: #ef4444;     /* Rojo - En veda */

/* UI */
--color-primary: #0369a1;    /* Azul oceano - Acciones principales */
--color-primary-light: #e0f2fe;
--color-surface: #f8fafc;   /* Fondo de tarjetas */
--color-border: #e2e8f0;     /* Bordes */

/* Texto */
--color-text-primary: #0f172a;
--color-text-secondary: #64748b;
```

---

## 5. Rutas y Navegacion

### 5.1 Configuracion de Rutas

```tsx
// App.tsx
<Routes>
  <Route path="/" element={<Layout />}>
    <Route index element={<Dashboard />} />
    <Route path="especies" element={<SpeciesCatalog />} />
    <Route path="especies/:id" element={<SpeciesDetail />} />
    <Route path="zonas" element={<Zonas />} />
    <Route path="zonas/:id" element={<ZoneDetail />} />
  </Route>
  <Route path="/login" element={<Login />} />
  <Route path="/admin" element={<AdminIngestion />} />
</Routes>
```

### 5.2 Estructura de Layout

El `Layout` component envuelve todas las rutas principales con header y footer comunes:

```
<Layout>
  <Header />           {/* Navegacion sticky */}
  <Outlet />          {/* Contenido de la pagina */}
  <footer />          {/* Pie de pagina */}
</Layout>
```

---

## 6. Cliente API

### 6.1 Tipos TypeScript

Los tipos definen la estructura de datos del backend:

```typescript
// lib/api.ts
export interface Pez {
  id: number
  nombreComun: string
  nombreCientifico: string
  nombreMaya: string | null
  riesgoCiguatera: string | null
  esInvasiva: boolean
  esProtegida: boolean
  tipoAgua: string
  migratorio: boolean
}

export interface Zona {
  id: number
  nombre: string
  macroZona: string
  tipoRestriccion: string | null
  esAnp: boolean
}
```

### 6.2 Patron de Cliente API

```typescript
class ApiClient {
  private baseUrl: string

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl
  }

  private async request<T>(path: string, options?: RequestInit): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    })

    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`)
    }

    return response.json()
  }

  get<T>(path: string): Promise<T> {
    return this.request<T>(path)
  }
}

export const apiClient = new ApiClient(API_BASE_URL)
```

---

## 7. Custom Hooks (TanStack Query)

### 7.1 Hooks de Datos

```typescript
// api/hooks.ts

// Hook simple para listar
export function usePeces() {
  return useQuery({
    queryKey: ['peces'],
    queryFn: api.peces.list,
  })
}

// Hook con parametro
export function usePez(id: number) {
  return useQuery({
    queryKey: ['peces', id],
    queryFn: () => api.peces.getById(id),
    enabled: !!id,  // No ejecuta si id es 0 o null
  })
}

// Hook con mutacion
export function useTriggerIngestion() {
  return useMutation({
    mutationFn: api.ingestion.trigger,
    onSuccess: () => {
      // Invalidar cache para refrescar datos
      queryClient.invalidateQueries({ queryKey: ['ingestion'] })
    },
  })
}
```

### 7.2 Configuracion Global

```typescript
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,  // 5 minutos
      retry: 1,                   // Un retry en caso de error
    },
  },
})
```

---

## 8. Componentes UI Base

### 8.1 Button

El componente Button soporta multiples variantes:

```tsx
<Button variant="default"> Primary </Button>
<Button variant="secondary"> Secondary </Button>
<Button variant="ghost"> Ghost </Button>
<Button variant="destructive"> Danger </Button>
<Button variant="outline"> Outline </Button>
```

Tamanos: `default`, `sm`, `lg`, `icon`

### 8.2 Badge

Para etiquetas de estado:

```tsx
<Badge variant="open"> Abierta </Badge>
<Badge variant="closed"> En Veda </Badge>
<Badge variant="warning"> Warning </Badge>
```

### 8.3 Card

Contenedor basico con borde y sombra:

```tsx
<Card>
  <CardHeader>
    <CardTitle> Titulo </CardTitle>
  </CardHeader>
  <CardContent>
    Contenido
  </CardContent>
</Card>
```

---

## 9. Paginas

### 9.1 Dashboard (/)

Es la pagina principal que responde: "¿Que puedo pescar hoy?"

**Caracteristicas:**
- Fecha actual
- Barra de busqueda con debounce
- Chips de filtro por zona (horizontal scroll)
- Seccion "Abiertas a la pesca" con cards
- Seccion "En veda hoy" (placeholder por ahora)

### 9.2 Catalogo de Especies (/especies)

Grid responsivo de todas las especies.

**Caracteristicas:**
- Filtro por zona
- Loading skeleton durante carga
- Empty state cuando no hay datos

### 9.3 Detalle de Especie (/especies/:id)

Informacion detallada de una especie.

**Caracteristicas:**
- Informacion general (nombre comun, cientifico, maya)
- Grid de metadatos (habitat, ciguatera, invasiva, etc.)
- Lista de periodos de veda
- Enlace a fuente DOF

### 9.4 Zonas (/zonas)

Lista de todas las zonas de pesca.

### 9.5 Detalle de Zona (/zonas/:id)

Informacion de una zona especifica con lista de especies reguladas.

### 9.6 Login (/login)

Formulario simple para autenticacion HTTP Basic.

### 9.7 Admin Ingestion (/admin)

Panel para monitorear y triggerear ingestas de datos.

---

## 10. Variables de Entorno

```env
VITE_API_BASE_URL=http://localhost:8080
```

Por defecto apunta a `http://localhost:8080` si no esta definida.

---

## 11. Scripts Disponibles

```bash
npm run dev      # Iniciar dev server con HMR
npm run build    # Build de produccion
npm run preview  # Previsualizar build
npm run lint     # Linting con ESLint
```

---

## 12. Mejores Practicas Implementadas

### 12.1 TypeScript

- Tipos explícitos para todas las interfaces de API
- Generics en el cliente API para type safety
- Props tipadas en componentes

### 12.2 Accesibilidad

- Uso de Radix UI para primitivas accesibles
- Atributos `aria-label` en botones iconograficos
- Contraste de colores minima 4.5:1

### 12.3 Performance

- Lazy loading de rutas con React Router
- Cache de queries con TanStack Query
- Build con tree-shaking para reducir bundle

### 12.4 Responsive

- Mobile-first CSS
- Breakpoints: sm (640px), md (768px), lg (1024px)
- Touch targets de 44px minimos

---

## 13. Proximos Pasos

1. **Integrar endpoints faltantes del backend**: ZonaController, RegulacionController, PeriodoVedaController
2. **Implementar estado de veda real**: Calcular quais especies estan en veda basado en la fecha actual
3. **Tests E2E**: Configurar Playwright para testing
4. **Optimizaciones**: Implementar infinite scroll para listas grandes

---

## 14. Recursos

- [React Documentation](https://react.dev)
- [TanStack Query](https://tanstack.com/query)
- [Tailwind CSS](https://tailwindcss.com)
- [Radix UI](https://radix-ui.com)
- [React Router](https://reactrouter.com)

---

Ultima actualizacion: 2026-04-08