import { Link } from 'react-router-dom'
import { useState, useRef, useEffect } from 'react'
import { ChevronDown, Fish } from 'lucide-react'
import { useVedasAgrupadas } from '@/api/hooks'
import { cn } from '@/lib/utils'

export function MegaMenu() {
  const [isOpen, setIsOpen] = useState(false)
  const [activeSubmenu, setActiveSubmenu] = useState<number | null>(null)
  const menuRef = useRef<HTMLDivElement>(null)
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const { data: vedasAgrupadas, isLoading } = useVedasAgrupadas()

  const handleMouseEnter = () => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current)
    }
    setIsOpen(true)
  }

  const handleMouseLeave = () => {
    timeoutRef.current = setTimeout(() => {
      setIsOpen(false)
      setActiveSubmenu(null)
    }, 150)
  }

  const handleSubmenuEnter = (index: number) => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current)
    }
    setActiveSubmenu(index)
  }

  const handleSubmenuLeave = () => {
    timeoutRef.current = setTimeout(() => {
      setActiveSubmenu(null)
    }, 100)
  }

  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }
    }
  }, [])

  return (
    <div
      ref={menuRef}
      className="relative"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <button
        className={cn(
          'flex items-center gap-1 text-sm font-medium transition-colors py-4',
          isOpen
            ? 'text-white'
            : 'text-white/80 hover:text-white'
        )}
      >
        Vedas
        <ChevronDown
          className={cn(
            'h-4 w-4 transition-transform duration-200',
            isOpen && 'rotate-180'
          )}
        />
      </button>

      {isOpen && (
        <div className="absolute left-0 top-full w-[600px] bg-white rounded-b-xl shadow-2xl border border-border overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200">
          <div className="flex">
            <div className="w-48 bg-surface shrink-0">
              <div className="p-4 border-b border-border">
                <h3 className="text-xs font-semibold text-text-secondary uppercase tracking-wider">
                  Categorías
                </h3>
              </div>
              <div className="py-2" onMouseLeave={handleSubmenuLeave}>
                {isLoading ? (
                  <div className="px-4 py-3 text-sm text-text-secondary">
                    Cargando...
                  </div>
                ) : (
                  vedasAgrupadas?.map((veda, index) => (
                    <button
                      key={veda.tipoVeda}
                      onMouseEnter={() => handleSubmenuEnter(index)}
                      className={cn(
                        'w-full text-left px-4 py-3 text-sm transition-colors flex items-center justify-between',
                        activeSubmenu === index
                          ? 'bg-primary/10 text-primary font-medium'
                          : 'text-text-secondary hover:bg-surface hover:text-text-primary'
                      )}
                    >
                      <span className="truncate pr-2">{veda.tipoVedaLabel}</span>
                      <span className="text-xs text-text-muted shrink-0">
                        {veda.peces.length}
                      </span>
                    </button>
                  ))
                )}
              </div>
            </div>

            <div className="flex-1 p-4 min-h-[300px]">
              {isLoading ? (
                <div className="flex items-center justify-center h-full">
                  <div className="animate-pulse text-text-secondary">
                    Cargando especies...
                  </div>
                </div>
              ) : activeSubmenu !== null && vedasAgrupadas ? (
                <div>
                  <h4 className="text-sm font-semibold text-text-primary mb-3">
                    {vedasAgrupadas[activeSubmenu].tipoVedaLabel}
                  </h4>
                  <div className="grid grid-cols-2 gap-2">
                    {vedasAgrupadas[activeSubmenu].peces.map((pez: { id: number; nombreComun: string }) => (
                      <Link
                        key={pez.id}
                        to={`/especies/${pez.id}`}
                        className="flex items-center gap-2 p-2 rounded-lg hover:bg-surface transition-colors group"
                        onClick={() => {
                          setIsOpen(false)
                          setActiveSubmenu(null)
                        }}
                      >
                        <Fish className="h-4 w-4 text-primary/50 group-hover:text-primary transition-colors shrink-0" />
                        <span className="text-sm text-text-secondary group-hover:text-text-primary transition-colors truncate">
                          {pez.nombreComun}
                        </span>
                      </Link>
                    ))}
                  </div>
                </div>
              ) : (
                <div className="flex flex-col items-center justify-center h-full text-center">
                  <Fish className="h-12 w-12 text-text-secondary/30 mb-3" />
                  <p className="text-sm text-text-secondary">
                    Selecciona una categoría para ver las especies
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
