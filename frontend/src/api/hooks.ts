import { useQuery, useQueries } from '@tanstack/react-query'
import { api, type Pez, type Zona, type RegulacionDto, type PeriodoVedaDto } from '@/lib/api'

export function usePeces() {
  return useQuery({
    queryKey: ['peces'],
    queryFn: api.peces.list,
  })
}

export function usePez(id: number) {
  return useQuery({
    queryKey: ['peces', id],
    queryFn: () => api.peces.getById(id),
    enabled: !!id,
  })
}

export function usePecesAbiertos() {
  return useQuery({
    queryKey: ['peces', 'abiertos'],
    queryFn: api.peces.abiertos,
  })
}

export function useZonas() {
  return useQuery({
    queryKey: ['zonas'],
    queryFn: api.zonas.list,
  })
}

export function useZona(id: number) {
  return useQuery({
    queryKey: ['zonas', id],
    queryFn: () => api.zonas.getById(id),
    enabled: !!id,
  })
}

export function usePeriodosVedaActuales() {
  return useQuery({
    queryKey: ['periodos-veda', 'actuales'],
    queryFn: api.periodosVeda.actuales,
  })
}

export function usePeriodosVedaByPez(pezId: number) {
  return useQuery({
    queryKey: ['periodos-veda', 'pez', pezId],
    queryFn: () => api.periodosVeda.byPez(pezId),
    enabled: !!pezId,
  })
}

export function useVedasAgrupadas() {
  return useQuery({
    queryKey: ['periodos-veda', 'agrupados'],
    queryFn: api.periodosVeda.agrupados,
  })
}

export interface VedaActivaDashboard {
  id: number
  nombreComun: string
  nombreCientifico: string | null
  tipoVeda: string
  fechaFin: string
  descripcion: string
  imagenUrl: string
}

export function useVedasActivasDashboard() {
  const { data: vedasActuales, isLoading: loadingVedas } = usePeriodosVedaActuales()

  if (loadingVedas || !vedasActuales) {
    return { vedas: [], isLoading: !!loadingVedas }
  }

  const uniqueVedasMap = new Map<number, VedaActivaDashboard>()

  vedasActuales.forEach((v) => {
    if (uniqueVedasMap.has(v.pezId)) return

    const diaFin = v.diaFin?.toString().padStart(2, '0') ?? '01'
    const mesFinNum = v.mesFin
    const mesFinNombre = formatearMes(mesFinNum)

    let descripcion = ''
    if (v.tipoVeda === 'PERMANENTE') {
      descripcion = 'Captura prohibida permanentemente.'
    } else if (v.tipoVeda === 'PROTECCION_MULTI_ANUAL') {
      descripcion = 'Especie bajo protección multi-anual.'
    } else {
      descripcion = `Restricción vigente hasta el ${diaFin} de ${mesFinNombre}.`
    }

    uniqueVedasMap.set(v.pezId, {
      id: v.id,
      nombreComun: v.pezNombre,
      nombreCientifico: v.pezNombreCientifico,
      tipoVeda: formatearTipoVeda(v.tipoVeda),
      fechaFin: v.tipoVeda === 'PERMANENTE' ? 'Fuera de Temporada' : `${diaFin} de ${mesFinNombre}`,
      descripcion,
      imagenUrl: v.pezImagenUrl ?? `https://picsum.photos/seed/${v.pezId}/200/200`,
    })
  })

  return { vedas: Array.from(uniqueVedasMap.values()), isLoading: false }
}

function formatearTipoVeda(tipo: string): string {
  switch (tipo) {
    case 'PERMANENTE': return 'Veda Permanente'
    case 'TEMPORAL_FIJA': return 'Veda Estacional'
    case 'PROTECCION_MULTI_ANUAL': return 'Protección Multi-Anual'
    default: return tipo.replace(/_/g, ' ')
  }
}

function formatearMes(mes: number): string {
  const meses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ]
  return meses[mes - 1] ?? mes.toString()
}

export function useRegulacionesByPez(pezId: number) {
  return useQuery({
    queryKey: ['regulaciones', 'pez', pezId],
    queryFn: () => api.regulaciones.byPez(pezId),
    enabled: !!pezId,
  })
}

export function useRegulacionesByPezDetalle(pezId: number) {
  return useQuery({
    queryKey: ['regulaciones', 'pez', pezId, 'detalle'],
    queryFn: () => api.regulaciones.byPezDetalle(pezId),
    enabled: !!pezId,
  })
}

export function useRegulacionesByZona(zonaId: number) {
  return useQuery({
    queryKey: ['regulaciones', 'zona', zonaId],
    queryFn: () => api.regulaciones.byZona(zonaId),
    enabled: !!zonaId,
  })
}

export function usePecesConEstado() {
  const { data: peces, isLoading: loadingPeces } = usePeces()
  const { isLoading: loadingVedas } = usePeriodosVedaActuales()

  const pecesAbiertos: Pez[] = peces ?? []
  const pecesEnVeda: { pez: Pez; periodoFin: string }[] = []

  return {
    pecesAbiertos,
    pecesEnVeda,
    isLoading: loadingPeces || loadingVedas,
  }
}

export interface ZonaConEstado {
  id: number
  nombre: string
  status: 'open' | 'warning'
  nota?: string
  macroZona: string
  esAnp: boolean
}

export interface ZonaConEstadoDetallado extends ZonaConEstado {
  totalEspecies: number
  especiesAbiertas: number
  especiesEnVeda: number
  vedasActivas: Array<{
    pezId: number
    pezNombre: string
    tipoVeda: string
    fechaFin: string
  }>
}

export function useZonasConEstado(): {
  zonas: ZonaConEstado[]
  isLoading: boolean
  isError: boolean
} {
  const { data: zonas, isLoading: loadingZonas, isError: errorZonas } = useZonas()

  const isLoading = loadingZonas
  const isError = !!errorZonas

  if (isLoading || !zonas) {
    return { zonas: [], isLoading, isError }
  }

  const zonasConEstado: ZonaConEstado[] = zonas.map((zona: Zona) => {
    let status: 'open' | 'warning' = 'open'
    let nota: string | undefined = undefined

    if (zona.esAnp) {
      status = 'warning'
      nota = zona.notasEspecificas ?? 'Área Natural Protegida - consulte regulaciones locales'
    } else if (zona.notasEspecificas) {
      status = 'warning'
      nota = zona.notasEspecificas
    } else if (zona.tipoRestriccion) {
      status = 'warning'
      nota = zona.tipoRestriccion
    }

    return {
      id: zona.id,
      nombre: zona.nombre,
      status,
      nota,
      macroZona: zona.macroZona,
      esAnp: zona.esAnp,
    }
  })

  return { zonas: zonasConEstado, isLoading: false, isError }
}

export function useZonasConEstadoDetallado(): {
  zonas: ZonaConEstadoDetallado[]
  isLoading: boolean
  isError: boolean
} {
  const { data: zonas, isLoading: loadingZonas, isError: errorZonas } = useZonas()
  const { data: vedasActuales, isLoading: loadingVedas } = usePeriodosVedaActuales()

  const isLoading = loadingZonas || loadingVedas
  const isError = !!errorZonas

  const regulacionesQueries = useQueries({
    queries: (zonas ?? []).map((zona) => ({
      queryKey: ['regulaciones', 'zona', zona.id],
      queryFn: () => api.regulaciones.byZona(zona.id),
      enabled: !!zona.id,
    })),
  })

  const isLoadingRegulaciones = regulacionesQueries.some((q) => q.isLoading)
  const finalLoading = isLoading || isLoadingRegulaciones

  if (finalLoading || !zonas) {
    return { zonas: [], isLoading: finalLoading, isError }
  }

  const vedasActivasIds = new Set(vedasActuales?.map((v) => v.pezId) ?? [])

  const vedasActivasMap = new Map<number, PeriodoVedaDto[]>()
  vedasActuales?.forEach((v) => {
    const existing = vedasActivasMap.get(v.pezId) ?? []
    existing.push(v)
    vedasActivasMap.set(v.pezId, existing)
  })

  const zonasDetalladas: ZonaConEstadoDetallado[] = zonas.map((zona, index) => {
    const regulacionesData = regulacionesQueries[index]?.data ?? []

    const totalEspecies = new Set<number>()
    const especiesEnVeda: Array<{ pezId: number; pezNombre: string; tipoVeda: string; fechaFin: string }> = []

    regulacionesData.forEach((reg: RegulacionDto) => {
      totalEspecies.add(reg.pezId)

      if (vedasActivasIds.has(reg.pezId)) {
        const vedasPez = vedasActivasMap.get(reg.pezId) ?? []
        vedasPez.forEach((v) => {
          const mesFin = v.mesFin.toString().padStart(2, '0')
          const diaFin = v.diaFin?.toString().padStart(2, '0') ?? '01'
          especiesEnVeda.push({
            pezId: reg.pezId,
            pezNombre: reg.pezNombre,
            tipoVeda: v.tipoVeda,
            fechaFin: `${diaFin}/${mesFin}`,
          })
        })
      }
    })

    const uniqueVedas = Array.from(
      new Map(especiesEnVeda.map((v) => [v.pezId, v])).values()
    )

    const especiesAbiertas = totalEspecies.size - uniqueVedas.length

    let status: 'open' | 'warning' = 'open'
    let nota: string | undefined = undefined

    if (uniqueVedas.length > 0) {
      status = 'warning'
      nota = `${uniqueVedas.length} especie(s) en veda actualmente`
    } else if (zona.esAnp) {
      status = 'warning'
      nota = zona.notasEspecificas ?? 'Área Natural Protegida - consulte regulaciones locales'
    } else if (zona.notasEspecificas) {
      status = 'warning'
      nota = zona.notasEspecificas
    } else if (zona.tipoRestriccion) {
      status = 'warning'
      nota = zona.tipoRestriccion
    }

    return {
      id: zona.id,
      nombre: zona.nombre,
      status,
      nota,
      macroZona: zona.macroZona,
      esAnp: zona.esAnp,
      totalEspecies: totalEspecies.size,
      especiesAbiertas,
      especiesEnVeda: uniqueVedas.length,
      vedasActivas: uniqueVedas,
    }
  })

  return { zonas: zonasDetalladas, isLoading: false, isError }
}