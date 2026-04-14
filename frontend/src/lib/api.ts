const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export interface Pez {
  id: number
  nombreComun: string
  nombreCientifico: string
  nombreMaya: string | null
  descripcion: string | null
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
  categoriaHidrica: string | null
  esAnp: boolean
  municipioSede: string | null
  notasEspecificas: string | null
}

export interface RegulacionDto {
  id: number
  pezId: number
  pezNombre: string
  zonaId: number
  zonaNombre: string
  categoriaPesca: string | null
  tallaMinima: number | null
  tallaMaxima: number | null
  tipoMedicion: string | null
  cuotaDiaria: number | null
  requierePermiso: boolean
}

export interface PeriodoVedaDto {
  id: number
  regulacionId: number
  pezId: number
  pezNombre: string
  pezImagenUrl: string | null
  tipoVeda: string
  mesInicio: number
  diaInicio: number | null
  mesFin: number
  diaFin: number | null
  fuenteDof: string | null
}

export interface IngestionLog {
  id: number
  nombreArchivo: string
  hashSha256: string
  totalFilas: number
  filasExitosas: number
  filasError: number
  estado: string
  procesadoEn: string
  detalleError: string | null
}

export interface PezBasicoDto {
  id: number
  nombreComun: string
}

export interface VedaAgrupadaDto {
  tipoVeda: string
  tipoVedaLabel: string
  peces: PezBasicoDto[]
}

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
      throw new Error(`API Error: ${response.status} ${response.statusText}`)
    }

    return response.json()
  }

  get<T>(path: string): Promise<T> {
    return this.request<T>(path)
  }

  post<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>(path, {
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    })
  }
}

export const apiClient = new ApiClient(API_BASE_URL)

export const api = {
  peces: {
    list: () => apiClient.get<Pez[]>('/api/v1/peces'),
    getById: (id: number) => apiClient.get<Pez>(`/api/v1/peces/${id}`),
  },
  zonas: {
    list: () => apiClient.get<Zona[]>('/api/v1/zonas'),
    getById: (id: number) => apiClient.get<Zona>(`/api/v1/zonas/${id}`),
  },
  regulaciones: {
    byPez: (pezId: number) => apiClient.get<RegulacionDto[]>(`/api/v1/regulaciones/pez/${pezId}`),
    byZona: (zonaId: number) => apiClient.get<RegulacionDto[]>(`/api/v1/regulaciones/zona/${zonaId}`),
  },
  periodosVeda: {
    actuales: () => apiClient.get<PeriodoVedaDto[]>('/api/v1/periodos-veda/actuales'),
    byPez: (pezId: number) => apiClient.get<PeriodoVedaDto[]>(`/api/v1/periodos-veda/pez/${pezId}`),
    agrupados: () => apiClient.get<VedaAgrupadaDto[]>('/api/v1/periodos-veda/agrupados'),
  },
  ingestion: {
    trigger: () => apiClient.post('/api/v1/ingestion/trigger'),
    status: () => apiClient.get<IngestionLog[]>('/api/v1/ingestion/status'),
    latest: () => apiClient.get<IngestionLog>('/api/v1/ingestion/latest'),
    stats: () => apiClient.get<{ total: number; successful: number; failed: number }>('/api/v1/ingestion/stats'),
    health: () => apiClient.get<{ status: string }>('/api/v1/ingestion/health'),
  },
}