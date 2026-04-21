import { Link, useLocation } from 'react-router-dom'
import { useState, useRef, useEffect } from 'react'
import { ChevronRight } from 'lucide-react'
import { useVedasAgrupadas } from '@/api/hooks'
import { cn } from '@/lib/utils'

export function MegaMenu() {
  const [isOpen, setIsOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const location = useLocation()

  const { data: vedasAgrupadas } = useVedasAgrupadas()

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

  const isActive = location.pathname === '/vedas'

  return (
    <div
      ref={menuRef}
      className="relative"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <button
        className={cn(
          'pb-2 transition-all duration-300 font-headline tracking-tight font-bold uppercase text-sm',
          isActive ? 'text-tertiary border-b-2 border-tertiary bio-glow' : 'text-[#dae2fc]/70 hover:text-white'
        )}
      >
        Vedas
      </button>

      {isOpen && (
        <div className="fixed inset-x-0 top-[88px] left-0 w-full z-40 animate-fade-in">
          <div className="relative w-full bg-[#0a1325]/95 backdrop-blur-3xl border-b border-outline-variant/10 overflow-hidden">
            <div className="absolute inset-0 mega-menu-aura pointer-events-none" />
            <div className="max-w-screen-2xl mx-auto px-12 py-16 grid grid-cols-12 gap-12 relative z-10">
              <div className="col-span-3 border-r border-outline-variant/10 pr-8">
                <h2 className="font-headline text-xs font-black uppercase tracking-[0.2em] text-tertiary mb-8 bio-glow">Taxonomía</h2>
                <ul className="space-y-6">
                  {vedasAgrupadas?.map((veda) => (
                    <li key={veda.tipoVeda} className="group cursor-pointer">
                      <div className="flex items-center justify-between text-on-surface group-hover:text-primary transition-colors">
                        <span className="font-headline font-extrabold text-lg">{veda.tipoVedaLabel}</span>
                        <ChevronRight className="h-4 w-4 opacity-0 group-hover:opacity-100 -translate-x-2 group-hover:translate-x-0 transition-all" />
                      </div>
                      <p className="text-xs text-on-surface-variant/60 mt-1">{veda.peces.length} especies</p>
                    </li>
                  ))}
                </ul>
              </div>

              <div className="col-span-6 grid grid-cols-2 gap-x-12 gap-y-10">
                {vedasAgrupadas?.map((veda) => (
                  <div key={veda.tipoVeda}>
                    <h3 className="font-headline text-[10px] font-bold uppercase tracking-widest text-on-surface-variant/40 mb-6 border-b border-outline-variant/10 pb-2">
                      {veda.tipoVedaLabel}
                    </h3>
                    <ul className="space-y-4">
                      {veda.peces.slice(0, 5).map((pez) => (
                        <li key={pez.id}>
                          <Link 
                            to={`/especies/${pez.id}`}
                            className="text-sm text-on-surface/80 hover:text-tertiary hover:pl-2 transition-all duration-300 flex items-center gap-2"
                            onClick={() => setIsOpen(false)}
                          >
                            <span className="w-1 h-1 rounded-full bg-tertiary/40" />
                            {pez.nombreComun}
                          </Link>
                        </li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>

              <div className="col-span-3">
                <div className="relative group rounded-xl overflow-hidden aspect-[4/5] bg-surface-container-low border border-outline-variant/10">
                  {vedasAgrupadas?.[0]?.peces[0]?.imagenUrl ? (
                    <img 
                      src={vedasAgrupadas[0].peces[0].imagenUrl} 
                      alt={vedasAgrupadas[0].peces[0].nombreComun}
                      className="absolute inset-0 w-full h-full object-cover opacity-60 group-hover:scale-110 transition-transform duration-700"
                    />
                  ) : (
                    <div className="absolute inset-0 bg-gradient-to-br from-surface-container-low to-surface-container" />
                  )}
                  <div className="absolute inset-0 bg-gradient-to-t from-[#0a1325] via-transparent to-transparent" />
                  <div className="absolute bottom-0 left-0 p-6">
                    <span className="inline-block px-2 py-1 rounded bg-tertiary/20 text-tertiary text-[10px] font-bold uppercase tracking-widest mb-3 backdrop-blur-md border border-tertiary/30">
                      Especie del Día
                    </span>
                    <h4 className="text-xl font-headline font-black leading-tight text-white mb-2">
                      {vedasAgrupadas?.[0]?.peces[0]?.nombreComun || 'Pulpo'}
                    </h4>
                    <p className="text-xs text-on-surface-variant leading-relaxed mb-4">
                      Consulta las regulaciones vigentes para esta especie.
                    </p>
                    <Link 
                      to={vedasAgrupadas?.[0]?.peces[0] ? `/especies/${vedasAgrupadas[0].peces[0].id}` : '/especies'}
                      className="text-xs font-headline font-bold text-primary flex items-center gap-2 hover:gap-3 transition-all"
                      onClick={() => setIsOpen(false)}
                    >
                      Ver Especie <ChevronRight className="h-4 w-4" />
                    </Link>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-surface-container-low/50 py-4 border-t border-outline-variant/10">
              <div className="max-w-screen-2xl mx-auto px-12 flex justify-between items-center">
                <div className="flex items-center gap-4 text-xs font-headline font-bold text-on-surface-variant/40">
                  <span className="material-symbols-outlined text-sm">search</span>
                  BUSCAR EN EL REGISTRO
                </div>
                <div className="flex gap-4">
                  <span className="text-[10px] font-bold text-tertiary/60 tracking-widest">ESTADO: ACTIVO</span>
                  <span className="text-[10px] font-bold text-on-surface-variant/40 tracking-widest">SINCRONIZADO</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}