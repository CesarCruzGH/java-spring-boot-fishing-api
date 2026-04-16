import { Link } from 'react-router-dom'
import { useState, useRef, useEffect } from 'react'
import { Fish } from 'lucide-react'
import { useVedasAgrupadas } from '@/api/hooks'
import { cn } from '@/lib/utils'

export function MegaMenu() {
  const [isOpen, setIsOpen] = useState(false)
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
    }, 150)
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
          'text-sm font-medium transition-colors py-4',
          isOpen ? 'text-[#9CD5FF]' : 'text-white/80 hover:text-[#9CD5FF]'
        )}
      >
        Vedas
      </button>

      {isOpen && (
        <div 
          style={{ 
            backdropFilter: 'blur(12px)',
            WebkitBackdropFilter: 'blur(12px)',
            backgroundColor: 'rgba(0, 0, 0, 0.6)'
          }}
          className="fixed inset-x-0 top-16 overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200 z-[100]"
        >
          <div className="container mx-auto px-4 py-6">
            {isLoading ? (
              <div className="flex items-center justify-center py-12">
                <div className="animate-pulse text-white/60">
                  Cargando...
                </div>
              </div>
            ) : (
              <div className="grid grid-cols-4 gap-4">
                {vedasAgrupadas?.map((veda) => (
                  <div
                    key={veda.tipoVeda}
                    className="bg-white/5 rounded-lg overflow-hidden"
                  >
                    <div className="p-3 border-b border-white/10">
                      <h3 className="text-sm font-semibold text-[#9CD5FF] truncate">
                        {veda.tipoVedaLabel}
                      </h3>
                      <p className="text-xs text-white/40 mt-0.5">
                        {veda.peces.length} especies
                      </p>
                    </div>
                    <div className="p-2 max-h-[280px] overflow-y-auto">
                      <div className="grid grid-cols-2 gap-1">
                        {veda.peces.map((pez: { id: number; nombreComun: string }) => (
                          <Link
                            key={pez.id}
                            to={`/especies/${pez.id}`}
                            className="flex items-center gap-2 p-2 rounded hover:bg-white/10 transition-colors group"
                            onClick={() => setIsOpen(false)}
                          >
                            <Fish className="h-3 w-3 text-[#9CD5FF]/50 group-hover:text-[#9CD5FF] transition-colors shrink-0" />
                            <span className="text-xs text-white/80 group-hover:text-white transition-colors truncate">
                              {pez.nombreComun}
                            </span>
                          </Link>
                        ))}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
