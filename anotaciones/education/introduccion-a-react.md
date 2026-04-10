# Introduccion a React - Guia para Principiantes

## Tabla de Contenidos

1. [Que es React y por que existe?](#1-que-es-react-y-por-que-existe)
2. [Conceptos Fundamentales](#2-conceptos-fundamentales)
3. [JSX: La sintaxis que parece HTML](#3-jsx-la-sintaxis-que-parece-html)
4. [Componentes: Los bloques de construccion](#4-componentes-los-bloques-de-construccion)
5. [Props: Comunicacion entre componentes](#5-props-comunicacion-entre-componentes)
6. [State: Datos que cambian](#6-state-datos-que-cambian)
7. [Hooks: Funciones especiales de React](#7-hooks-funciones-especiales-de-react)
8. [El Flujo de Datos Unidireccional](#8-el-flujo-de-datos-unidireccional)
9. [Ciclo de Vida de un Componente](#9-ciclo-de-vida-de-un-componente)
10. [El Ecosystem around React](#10-el-ecosistema-around-react)

---

## 1. Que es React y por que existe?

### El Problema Antes de React

Antes de 2013, cuando los desarrolladores querian crear interfaces dinamicas web, tenian que:

1. **Manipular el DOM directamente** - EI DOM es como una representacion en arbol de tu pagina web. Cambiarlo era lento y propenso a errores.

2. **Sincronizar datos con la interfaz** - Si tenias una lista de peces y el usuario anadia uno, debias escribir codigo que dijera "busca el elemento lista, crea un nuevo elemento, agregalo al final".

3. **Mantener el codigo** - A medida que la app crecia, el codigo de sincronizacion se volvia un desastre.

### La Solucion de React

React fue creado por Facebook (ahora Meta) para resolver estos problemas. La idea central es revolucionaria en su simplicidad:

> **En lugar de decirle a React COMO cambiar la interfaz, le dices QUE quieres ver.**

React se encarga de calcular los cambios minimos necesarios y aplicarlos. Esto se llama **Reactividad**.

### Analogia del Restaurante

Imagina dos formas de cocinar en un restaurante:

| Sin React (DOM directo) | Con React |
|--------------------------|-----------|
| El chef va manualmente a cada mesa y cambia los platos | El chef dice "Quiero 3 mesas con pescado" y un asistente lo hace |
| Mucho trabajo manual | Descripcion declarativa |
| Propenso a errores (se olvida una mesa, etc.) | React se encarga de todo |

### Caracteristicas Clave

- **Libreria, no framework** - React solo se encarga de la interfaz. Para otras cosas (rutas, HTTP, formularios) necesitas herramientas externas.
- **Virtual DOM** - React mantiene una copia en memoria del DOM real. Cuando cambia algo, calcula la diferencia (diffing) y actualiza SOLO lo necesario.
- **Componentes** - Todo en React es un componente.
- **Unidireccional** - Los datos fluyen en una sola direccion: de padres a hijos.

---

## 2. Conceptos Fundamentales

### 2.1 Que es un Componente?

Un **componente** es una pieza de codigo reutilizable que devuelve JSX (parecido a HTML).

Piensa en un componente como una **funcion** que:
- Toma entradas (llamadas `props`)
- Devuelve elementos de interfaz (JSX)
- Puede tener su propio estado interno

**Ejemplo sencillo:**

```jsx
// Este es un componente funcional
function Saludo() {
  return <h1>Hola, bienvenido!</h1>
}

// Lo usas en otro lugar asi:
<Saludo />
```

### 2.2 Tipos de Componentes

#### Componente Funcional (moderno, preferido)

```jsx
function Boton(props) {
  return <button className="btn">{props.texto}</button>
}
```

#### Componente de Clase (anticuado, ya no se usa)

```jsx
class Boton extends React.Component {
  render() {
    return <button className="btn">{this.props.texto}</button>
  }
}
```

**Hoy en dia, TODO se hace con componentes funcionales.** Los hooks (que veremos despues) permiten hacer todo lo que las clases hacian, pero con menos codigo.

### 2.3 Reglas Importantisimas

1. **Todo componente debe devolver JSX**
2. **Los nombres de componentes EMPIEZAN con mayuscula** - `<Boton />` funciona, `<boton />` no
3. **JSX debe tener un padre (root element)** - Esto falla:

```jsx
// ERROR
return (
  <h1>Titulo</h1>
  <p>Parrafo</p>
)
```

   Esto funciona:

```jsx
// CORRECTO
return (
  <div>
    <h1>Titulo</h1>
    <p>Parrafo</p>
  </div>
)
```

   O usando fragments:

```jsx
// CORRECTO - Fragment no crea un elemento extra en el DOM
return (
  <>
    <h1>Titulo</h1>
    <p>Parrafo</p>
  </>
)
```

---

## 3. JSX: La sintaxis que parece HTML

### 3.1 Que es JSX?

JSX es una extension de JavaScript que permite escribir HTML-like dentro de JavaScript.

**Sin JSX (molesto):**

```javascript
return React.createElement('div', null,
  React.createElement('h1', null, 'Titulo'),
  React.createElement('p', null, 'Parrafo')
)
```

**Con JSX (legible):**

```jsx
return (
  <div>
    <h1>Titulo</h1>
    <p>Parrafo</p>
  </div>
)
```

### 3.2 Diferencias Entre HTML y JSX

| HTML | JSX | Por que? |
|------|-----|----------|
| `class="algo"` | `className="algo"` | `class` es palabra reservada en JavaScript |
| `style="color:red"` | `style={{color: 'red'}}` | `style` acepta un objeto, no un string |
| `<input disabled>` | `<input disabled={true} />` | Los atributos son expresiones JavaScript |
| `for="label"` | `htmlFor="label"` | `for` es palabra reservada |

### 3.3 Expresiones en JSX

Puedes usar cualquier **expresion JavaScript** dentro de JSX usando `{}`:

```jsx
const nombre = 'Carlos'
const edad = 25

return (
  <div>
    <h1>Hola, {nombre}</h1>
    <p>Tienes {edad} anios</p>
    <p>En 5 anios tendras {edad + 5}</p>
  </div>
)
```

**Lo que NO funciona:**

```jsx
// ERROR: Las sentencias no son expresiones
return (
  <div>
    {if (nombre) { <p>{nombre}</p> }}  // NO funciona
  </div>
)

// CORRECTO: Usa operadores ternarios
return (
  <div>
    {nombre ? <p>{nombre}</p> : <p>Anonimo</p>}
  </div>
)
```

---

## 4. Componentes: Los bloques de construccion

### 4.1 Estructura Basica

Un componente funcional basico tiene esta estructura:

```jsx
// 1. Importaciones
import { useState } from 'react'

// 2. Definicion del componente
function Contador() {
  // 3. Hooks (estado)
  const [numero, setNumero] = useState(0)

  // 4. Funciones auxiliares
  function incrementar() {
    setNumero(numero + 1)
  }

  // 5. Retorno JSX (la interfaz)
  return (
    <div>
      <p>Valor: {numero}</p>
      <button onClick={incrementar}>Sumar</button>
    </div>
  )
}

// 6. Exportacion
export default Contador
```

### 4.2 Exportacion Named vs Default

**Export default (el que ves en tu proyecto):**

```jsx
export default App
```

Solo puedes tener UNO por archivo. Se importa asi:

```jsx
import App from './App'
```

**Export named:**

```jsx
export function App() { ... }
export functionOtroComponente() { ... }
```

Se importa asi:

```jsx
import { App, OtroComponente } from './App'
```

---

## 5. Props: Comunicacion entre componentes

### 5.1 Que son las Props?

**Props** = Properties = Propiedades

Las props son la forma de pasar datos de un componente padre a un componente hijo. Son **只读 (solo lectura)** - el hijo no puede modificarlas directamente.

### 5.2 Ejemplo Practico

```
App (padre)
  └── SpeciesCatalog (hijo)
        └── TarjetaEspecie (nieto)
```

**App.jsx:**

```jsx
function App() {
  const catalogo = ['Mero', 'Robalo', 'Pargo']

  return (
    <SpeciesCatalog lista={catalogo} titulo="Catalogo de Especies" />
  )
}
```

**SpeciesCatalog.jsx:**

```jsx
function SpeciesCatalog(props) {
  // props = { lista: ['Mero', 'Robalo', 'Pargo'], titulo: 'Catalogo de Especies' }

  return (
    <div>
      <h1>{props.titulo}</h1>
      <ul>
        {props.lista.map((especie, indice) => (
          <li key={indice}>{especie}</li>
        ))}
      </ul>
    </div>
  )
}
```

### 5.3 Destructuring de Props

En lugar de escribir `props.nombre` muchas veces, puedes "desestructurar":

```jsx
// SIN destructuring
function SpeciesCatalog(props) {
  console.log(props.titulo)
  return <h1>{props.titulo}</h1>
}

// CON destructuring
function SpeciesCatalog({ titulo, lista }) {
  return <h1>{titulo}</h1>
}
```

Ambas formas funcionan, la segunda es mas limpia.

---

## 6. State: Datos que cambian

### 6.1 Por que existe el State?

Los **props** van de padre a hijo y son inmutables. Pero que pasa cuando el usuario interactua y algo debe cambiar?

Ahi entra **useState** - un Hook que permite a un componente mantener su propio estado.

### 6.2 useState

```jsx
import { useState } from 'react'

function Contador() {
  // numero = valor actual, setNumero = funcion para cambiarlo
  // useState(0) = valor inicial de numero es 0
  const [numero, setNumero] = useState(0)

  return (
    <div>
      <p>Valor: {numero}</p>
      <button onClick={() => setNumero(numero + 1)}>
        Incrementar
      </button>
    </div>
  )
}
```

### 6.3 Entendiendo el ciclo de actualizacion

```
1. Usuario hace click en "Incrementar"
2. Se llama setNumero(1)
3. React marca el componente como "necesita actualizarse"
4. React vuelve a ejecutar la funcion del componente
5. React compara el nuevo JSX con el anterior (diffing)
6. React actualiza SOLO lo que cambio en el DOM
```

### 6.4 multiple States

Un componente puede tener multiples estados:

```jsx
function Formulario() {
  const [nombre, setNombre] = useState('')
  const [email, setEmail] = useState('')
  const [enviado, setEnviado] = useState(false)

  function handleSubmit(e) {
    e.preventDefault()
    setEnviado(true)
  }

  return (
    <form onSubmit={handleSubmit}>
      <input 
        value={nombre} 
        onChange={(e) => setNombre(e.target.value)} 
      />
      <input 
        value={email} 
        onChange={(e) => setEmail(e.target.value)} 
      />
      {enviado && <p>Formulario enviado!</p>}
    </form>
  )
}
```

---

## 7. Hooks: Funciones especiales de React

### 7.1 Que es un Hook?

Un **Hook** es una funcion especial que permite "conectarse" a funcionalidades de React. Siempre empiezan con `use`.

Hooks disponibles en React core:
- `useState` - Estado local
- `useEffect` - Efectos secundarios
- `useContext` - Context API
- `useRef` - Referencias
- `useMemo` - Optimizacion
- `useCallback` - Optimizacion

### 7.2 useEffect

`useEffect` permite ejecutar codigo "side effect" (efectos secundarios) como:
- Llamadas a APIs
- Suscripciones a eventos
- Temporizadores
- Modificar el DOM directamente

```jsx
import { useState, useEffect } from 'react'

function SpeciesDetail({ id }) {
  const [especie, setEspecie] = useState(null)
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    // Este codigo se ejecuta cuando el componente se monta
    // O cuando 'id' cambia
    
    async function cargarEspecie() {
      setCargando(true)
      const respuesta = await fetch(`/api/especies/${id}`)
      const datos = await respuesta.json()
      setEspecie(datos)
      setCargando(false)
    }

    cargarEspecie()
  }, [id]) // Array de dependencias: se re-ejecuta si 'id' cambia

  if (cargando) return <p>Cargando...</p>
  if (!especie) return <p>No encontrada</p>

  return (
    <div>
      <h1>{especie.nombre}</h1>
      <p>{especie.descripcion}</p>
    </div>
  )
}
```

### 7.3 Reglas de los Hooks

1. **Solo en componentes funcionales** - No puedes usar hooks dentro de condicionales, loops, o funciones internas
2. **Siempre al inicio del componente** - hook, hook, hook, luego logica
3. **El nombre debe empezar con use** - Esta es la convencion

```jsx
// CORRECTO
function MiComponente() {
  const [a, setA] = useState(0)
  const [b, setB] = useState(0)

  if (a > 0) {
    const [c, setC] = useState(0) // ERROR! No aqui
  }

  useEffect(() => { ... }, [])

  function algo() {
    useState(0) // ERROR! No dentro de funciones
  }
}

// CORRECTO: Todo hook al inicio
function MiComponente() {
  const [a, setA] = useState(0)
  const [b, setB] = useState(0)
  useEffect(() => { ... }, [])

  function algo() {
    // Logica normal, sin hooks
  }
}
```

---

## 8. El Flujo de Datos Unidireccional

### 8.1 Concepto

En React, los datos fluyen en UNA direccion:

```
PADRE  ----props---->  HIJO
  ^                      |
  |                      |
  ---callback function---/
```

El hijo puede LEER las props pero no puede MODIFICARLAS directamente. Si quiere cambiar algo, debepedirselo al padre a traves de una funcion callback.

### 8.2 Ejemplo Completo

```
App
  └── Layout
        └── Dashboard
              └── TarjetaMetrica
```

Imaginemos que en Dashboard queremos mostrar metricas:

**App.jsx (provee los datos):**

```jsx
function App() {
  const [metricas, setMetricas] = useState([
    { tipo: 'especies', valor: 150 },
    { tipo: 'zonas', valor: 12 }
  ])

  return (
    <Layout>
      <Dashboard metricas={metricas} />
    </Layout>
  )
}
```

**Dashboard.jsx (recibe y puede anadir/interactuar):**

```jsx
function Dashboard({ metricas }) {
  function recargarMetricas() {
    // Pedir al servidor actualizar metricas
    fetch('/api/metricas/actualizar')
      .then(res => res.json())
      .then(nuevas => setMetricas(nuevas))
  }

  return (
    <div>
      <h1>Dashboard</h1>
      <button onClick={recargarMetricas}>Actualizar</button>
      {metricas.map((m, i) => (
        <TarjetaMetrica key={i} tipo={m.tipo} valor={m.valor} />
      ))}
    </div>
  )
}
```

**TarjetaMetrica.jsx (solo muestra):**

```jsx
function TarjetaMetrica({ tipo, valor }) {
  return (
    <div className="tarjeta">
      <span className="tipo">{tipo}</span>
      <span className="valor">{valor}</span>
    </div>
  )
}
```

Nota como `TarjetaMetrica` SOLO recibe datos, no los modifica. `Dashboard` puede modificar `metricas` porque es su estado local. `App` es quien realmente posee los datos.

### 8.3 Por que esta arquitectura?

1. **Debugging facil** - Sabes exactamente de donde vienen los datos
2. **Predicibilidad** - Si los datos cambian, solo hay un lugar donde cambiarlos
3. **Reutilizacion** - Componentes "tontos" (que solo reciben props) son muy reutilizables

---

## 9. Ciclo de Vida de un Componente

### 9.1 Las tres fases

Un componente en React tiene tres fases:

```
┌─────────────────────────────────────────────┐
│               MONTAJE (Mount)               │
│  Componente se crea y aparece en pantalla   │
│  useEffect(() => {...}, []) se ejecuta     │
└─────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────┐
│            ACTUALIZACION (Update)           │
│  Props o estado cambian, se re-renderiza   │
│  useEffect(() => {...}, [deps]) se ejecuta  │
└─────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────┐
│             DESMONTAJE (Unmount)            │
│  Componente desaparece de pantalla          │
│  Cleanup de useEffect se ejecuta           │
└─────────────────────────────────────────────┘
```

### 9.2 useEffect y cleanup

Algunos efectos necesitan "limpieza" cuando el componente se desmonta:

```jsx
useEffect(() => {
  const suscripcion = evento.on('datos', callback)

  // Funcion de cleanup
  return () => {
    evento.off('datos', callback)
  }
}, [])
```

Sin cleanup, tendrias "memory leaks" (fugas de memoria).

---

## 10. El Ecosystem around React

React por si solo solo sabe renders. Para una app completa necesitas herramientas:

### 10.1 Routing (Navegacion)

**react-router-dom** - Tu proyecto usa esto

Permite crear rutas sin recargar la pagina:

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom'

<BrowserRouter>
  <Routes>
    <Route path="/" element={<Dashboard />} />
    <Route path="/especies" element={<SpeciesCatalog />} />
    <Route path="/especies/:id" element={<SpeciesDetail />} />
  </Routes>
</BrowserRouter>
```

El `:id` es un **parametro dinamico**.

### 10.2 Estado Global

**Zustand / Redux / Context API**

Cuando muchos componentes necesitan los mismos datos (ej: usuario logueado), necesitas estado global. Tu proyecto usa **Context API** via React Query.

### 10.3 React Query (tanstack-query)

**Tu proyecto usa esto!**

Maneja automaticamente:
- Cacheo de datos
- Loading states
- Error states
- Refetch on focus
- Retry on failure

```jsx
import { useQuery } from '@tanstack/react-query'

function SpeciesCatalog() {
  // useQuery devuelve { data, isLoading, error }
  const { data: especies, isLoading } = useQuery({
    queryKey: ['especies'],
    queryFn: () => fetch('/api/especies').then(res => res.json())
  })

  if (isLoading) return <p>Cargando...</p>

  return (
    <ul>
      {especies?.map(e => <li key={e.id}>{e.nombre}</li>)}
    </ul>
  )
}
```

### 10.4 Styling

Opciones populares:
- **CSS Modules** - CSS por archivo, importado en el componente
- **Styled Components** - CSS-in-JS
- **Tailwind CSS** - Utilidades (clases como `p-4 bg-blue-500`)

Tu proyecto usa CSS basico con `index.css`.

### 10.5 Herramientas del build

| Herramienta | Uso |
|-------------|-----|
| **Vite** | Build tool moderno y rapido (tu proyecto usa esto) |
| **Webpack** | Build tool tradicional |
| **Babel** | Transpila codigo moderno a JavaScript compatible |
| **ESLint** | Linter (detecta errores de estilo/codigo) |
| **Prettier** | Formateador de codigo |

---

## Glosario Rapido

| Termino | Definicion |
|---------|------------|
| **Component** | Funcion que devuelve JSX |
| **JSX** | Sintaxis para escribir HTML en JavaScript |
| **Props** | Datos pasados de padre a hijo |
| **State** | Datos internos de un componente que pueden cambiar |
| **Hook** | Funcion especial de React (useState, useEffect, etc.) |
| **Virtual DOM** | Copia en memoria del DOM real |
| **Re-render** | Cuando un componente se re-ejecuta |
| **Effect** | Codigo que se ejecuta "despues" del render |
| **Query** | Solicitud de datos a un servidor |

---

## Recursos para Continuar Aprendiendo

1. **Documentacion oficial de React** - https://react.dev
2. **React Router** - https://reactrouter.com
3. **Tanstack Query** - https://tanstack.com/query
4. **Tu proyecto** - Lee los archivos en `frontend/src/` para ver todo junto

---

## Resumen Final

```
main.tsx                 Entry point - aqui empieza todo
    └── App.tsx          Configura providers y rutas
          └── Layout     Envuelve las paginas
                └── Dashboard, SpeciesCatalog, etc.
                      └── Components (reutilizables)
                            └── useState, useEffect (hooks)
```

React es una libreria simple en su concepto pero poderosa en practica. La mejor forma de aprenderla es **construyendo cosas**. Empieza por entender `main.tsx` y `App.tsx` de tu proyecto, luego explora los componentes uno por uno.
