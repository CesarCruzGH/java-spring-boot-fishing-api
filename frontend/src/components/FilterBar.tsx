import { useState, useRef, useEffect } from 'react'
import { ChevronDown, Check, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { Zona } from '@/lib/api'

interface FilterBarProps {
  zonas: Zona[]
  filters: {
    estatus: 'all' | 'open' | 'closed'
    zonaId: number | null
  }
  onFiltersChange: (filters: { estatus: 'all' | 'open' | 'closed'; zonaId: number | null }) => void
  totalCount: number
  filteredCount: number
  className?: string
}

interface DropdownProps {
  label: string
  value: string
  options: Array<{ value: string; label: string }>
  onChange: (value: string) => void
}

function Dropdown({ label, value, options, onChange }: DropdownProps) {
  const [isOpen, setIsOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (ref.current && !ref.current.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const selectedOption = options.find((o) => o.value === value)

  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className={cn(
          'px-4 py-2 rounded-lg text-sm font-semibold transition-colors flex items-center gap-2',
          value !== 'all'
            ? 'bg-primary/5 border border-primary/20 text-primary'
            : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
        )}
      >
        <span className="text-[10px] font-bold text-outline-variant uppercase block">{label}</span>
        <span className="font-bold">{selectedOption?.label}</span>
        <ChevronDown className={cn('h-4 w-4 transition-transform', isOpen && 'rotate-180')} />
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 mt-2 w-48 bg-surface-container-lowest rounded-xl shadow-xl border border-outline-variant/20 overflow-hidden z-50">
          {options.map((option) => (
            <button
              key={option.value}
              type="button"
              onClick={() => {
                onChange(option.value)
                setIsOpen(false)
              }}
              className={cn(
                'w-full px-4 py-2.5 text-left text-sm hover:bg-surface-container-high transition-colors flex items-center justify-between',
                option.value === value && 'bg-surface-container'
              )}
            >
              <span>{option.label}</span>
              {option.value === value && <Check className="h-4 w-4 text-primary" />}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

export function FilterBar({
  zonas,
  filters,
  onFiltersChange,
  totalCount,
  filteredCount,
  className
}: FilterBarProps) {
  const hasActiveFilters = filters.estatus !== 'all' || filters.zonaId !== null

  const estatusOptions = [
    { value: 'all', label: 'Todos' },
    { value: 'open', label: 'Abiertas' },
    { value: 'closed', label: 'En Veda' },
  ]

  const zonaOptions = [
    { value: 'all', label: 'Todas' },
    ...zonas.map((z) => ({ value: z.id.toString(), label: z.nombre })),
  ]

  const handleEstatusChange = (value: string) => {
    onFiltersChange({ ...filters, estatus: value as 'all' | 'open' | 'closed' })
  }

  const handleZonaChange = (value: string) => {
    onFiltersChange({ ...filters, zonaId: value === 'all' ? null : parseInt(value, 10) })
  }

  const clearFilters = () => {
    onFiltersChange({ estatus: 'all', zonaId: null })
  }

  return (
    <div
      className={cn(
        'bg-surface-container-lowest p-4 rounded-2xl flex flex-wrap items-center gap-4',
        'shadow-[0_4px_20px_rgba(14,28,46,0.03)] border border-outline-variant/10',
        className
      )}
    >
      <div className="px-4 py-2 border-r border-outline-variant/20">
        <span className="text-[10px] font-bold text-outline-variant uppercase block">Filtrar por</span>
        <span className="text-sm font-bold text-primary">Parámetros</span>
      </div>

      <div className="flex flex-wrap gap-3">
        <Dropdown
          label="Estatus"
          value={filters.estatus}
          options={estatusOptions}
          onChange={handleEstatusChange}
        />

        <Dropdown
          label="Zona"
          value={filters.zonaId?.toString() || 'all'}
          options={zonaOptions}
          onChange={handleZonaChange}
        />
      </div>

      <div className="ml-auto flex items-center gap-4">
        <span className="text-xs font-medium text-outline">
          {hasActiveFilters ? (
            <>
              Mostrando <strong className="text-primary">{filteredCount}</strong> de {totalCount} especies
            </>
          ) : (
            <>Mostrando {totalCount} especies</>
          )}
        </span>
        {hasActiveFilters && (
          <button
            type="button"
            onClick={clearFilters}
            className="text-primary text-sm font-bold hover:underline flex items-center gap-1"
          >
            <X className="h-4 w-4" />
            Limpiar
          </button>
        )}
      </div>
    </div>
  )
}