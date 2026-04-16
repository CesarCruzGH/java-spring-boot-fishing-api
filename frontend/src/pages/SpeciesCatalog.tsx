import { useState, useMemo } from 'react'
import { Fish } from 'lucide-react'
import { SpeciesCard } from '@/components/SpeciesCard'
import { FilterBar } from '@/components/FilterBar'
import { Pagination } from '@/components/Pagination'
import { usePeces, useZonas, usePeriodosVedaActuales } from '@/api/hooks'

const ITEMS_PER_PAGE = 6

export function SpeciesCatalog() {
  const [currentPage, setCurrentPage] = useState(1)
  const [filters, setFilters] = useState({
    estatus: 'all' as 'all' | 'open' | 'closed',
    zonaId: null as number | null,
  })

  const { data: peces, isLoading: loadingPeces } = usePeces()
  const { data: zonas } = useZonas()
  const { data: vedasActuales } = usePeriodosVedaActuales()

  const vedasActivasIds = useMemo(
    () => new Set(vedasActuales?.map((v) => v.pezId) ?? []),
    [vedasActuales]
  )

  const filteredPeces = useMemo(() => {
    if (!peces) return []

    return peces.filter((pez) => {
      const isInVeda = vedasActivasIds.has(pez.id)

      if (filters.estatus === 'open' && isInVeda) return false
      if (filters.estatus === 'closed' && !isInVeda) return false

      return true
    })
  }, [peces, vedasActivasIds, filters])

  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE
  const visiblePeces = filteredPeces.slice(startIndex, startIndex + ITEMS_PER_PAGE)

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleFiltersChange = (newFilters: typeof filters) => {
    setFilters(newFilters)
    setCurrentPage(1)
  }

  return (
    <div className="min-h-screen flex flex-col">
      <div className="p-8 space-y-6">
        <div className="flex items-center gap-3">
          <Fish className="h-6 w-6 text-primary" />
          <h1 className="text-3xl font-black text-on-surface tracking-tight font-['Manrope']">
            Catálogo de Especies
          </h1>
        </div>

        {zonas && zonas.length > 0 && (
          <FilterBar
            zonas={zonas}
            filters={filters}
            onFiltersChange={handleFiltersChange}
            totalCount={peces?.length ?? 0}
            filteredCount={filteredPeces.length}
          />
        )}

        {loadingPeces ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="bg-surface-container-lowest rounded-xl overflow-hidden animate-pulse">
                <div className="h-56 bg-surface-container" />
                <div className="p-6 space-y-4">
                  <div className="h-6 bg-surface-container rounded w-3/4" />
                  <div className="h-4 bg-surface-container rounded w-1/2" />
                  <div className="h-4 bg-surface-container rounded w-2/3" />
                </div>
              </div>
            ))}
          </div>
        ) : filteredPeces.length === 0 ? (
          <div className="bg-surface-container-lowest p-12 rounded-xl text-center coastal-glow">
            <Fish className="h-16 w-16 mx-auto text-on-surface-variant/50 mb-4" />
            <p className="text-lg font-semibold text-on-surface-variant">
              No hay especies que coincidan con los filtros
            </p>
            <p className="text-sm text-on-surface-variant/70 mt-1">
              Prueba ajustando los filtros o limpia la búsqueda
            </p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {visiblePeces.map((pez) => (
                <SpeciesCard
                  key={pez.id}
                  id={pez.id}
                  nombreComun={pez.nombreComun}
                  nombreMaya={pez.nombreMaya}
                  nombreCientifico={pez.nombreCientifico}
                  imagenUrl={pez.imagenUrl}
                  tipoAgua={pez.tipoAgua}
                  riesgoCiguatera={pez.riesgoCiguatera}
                />
              ))}
            </div>

            <Pagination
              totalItems={filteredPeces.length}
              itemsPerPage={ITEMS_PER_PAGE}
              currentPage={currentPage}
              onPageChange={handlePageChange}
            />
          </>
        )}
      </div>
    </div>
  )
}