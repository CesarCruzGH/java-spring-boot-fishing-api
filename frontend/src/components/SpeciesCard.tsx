import { Anchor, Layers, Grid, Waves, Flame, CircleDot, House } from 'lucide-react'
import { cn } from '@/lib/utils'

interface SpeciesCardProps {
  nombreComun: string
  nombreMaya?: string | null
  nombreCientifico: string
  estado: 'open' | 'closed' | 'invasive'
  categoria?: string
  imagenUrl?: string
  riesgoCiguatera?: string
  habitat?: string
  tallaMinima?: string
  artePesca?: string[]
  zona?: string
  onClick?: () => void
  className?: string
}

const ARTE_PESCA_ICONS: Record<string, React.ComponentType<{ className?: string }>> = {
  'Anzuelo': Anchor,
  'Currican': Waves,
  'Trampa': Layers,
  'Red': Grid,
  'Arpón': Flame,
  'Lazo': CircleDot,
  'Casitas': House,
  'default': Anchor,
}

const CIGUATERA_COLORS: Record<string, string> = {
  'Alto': 'text-tertiary',
  'Medio': 'text-warning',
  'Bajo': 'text-secondary',
  'Nulo': 'text-secondary',
}

export function SpeciesCard({
  nombreComun,
  nombreMaya,
  nombreCientifico,
  estado,
  categoria,
  imagenUrl,
  riesgoCiguatera,
  habitat,
  tallaMinima,
  artePesca,
  zona,
  onClick,
  className,
}: SpeciesCardProps) {
  const estadoLabels = {
    open: 'Permitido',
    closed: 'En Veda',
    invasive: 'Fomento',
  }

  const estadoVariants = {
    open: 'secondary-container',
    closed: 'tertiary',
    invasive: 'error',
  }

  const borderVariants = {
    open: 'border-l-4 border-secondary/40',
    closed: 'border-l-4 border-tertiary veda-border',
    invasive: 'border-l-4 border-error/40',
  }

  return (
    <article
      onClick={onClick}
      className={cn(
        'bg-surface-container-lowest rounded-xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col group cursor-pointer',
        borderVariants[estado],
        className
      )}
    >
      <div className="relative h-56 overflow-hidden">
        {imagenUrl ? (
          <img
            src={imagenUrl}
            alt={nombreComun}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
        ) : (
          <div className="w-full h-full bg-surface-container-high flex items-center justify-center">
            <span className="text-6xl opacity-30">🐟</span>
          </div>
        )}
        <div className="absolute top-4 left-4">
          <span
            className={cn(
              'px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest',
              `bg-${estadoVariants[estado]} text-on-${estadoVariants[estado]}`
            )}
          >
            {estadoLabels[estado]}
          </span>
        </div>
      </div>

      <div className="p-6 flex-1 flex flex-col">
        <div className="mb-4">
          <div className="flex justify-between items-start gap-2">
            <h3 className="text-xl font-bold text-primary leading-tight">{nombreComun}</h3>
            {categoria && (
              <span className="text-xs font-bold text-secondary-container bg-on-secondary-container px-2 py-0.5 rounded text-[10px] flex-shrink-0">
                {categoria}
              </span>
            )}
          </div>
          {nombreMaya && (
            <p className="text-secondary font-bold text-xs mt-1 uppercase tracking-wider">
              {nombreMaya} <span className="text-outline-variant font-normal lowercase ml-1">(maya)</span>
            </p>
          )}
          <p className="text-on-surface-variant italic text-xs font-body mt-0.5">{nombreCientifico}</p>
        </div>

        {(riesgoCiguatera || habitat) && (
          <div className="grid grid-cols-2 gap-3 mb-6">
            {riesgoCiguatera && (
              <div className="bg-surface-container-low p-2 rounded-lg">
                <span className="text-[9px] font-bold text-outline block uppercase">Ciguatera</span>
                <span className={cn('text-xs font-bold', CIGUATERA_COLORS[riesgoCiguatera] || 'text-on-surface')}>
                  {riesgoCiguatera}
                </span>
              </div>
            )}
            {habitat && (
              <div className="bg-surface-container-low p-2 rounded-lg">
                <span className="text-[9px] font-bold text-outline block uppercase">Hábitat</span>
                <span className="text-xs font-bold text-on-surface">{habitat}</span>
              </div>
            )}
          </div>
        )}

        <div className="mt-auto pt-4 border-t border-outline-variant/10 space-y-3">
          {tallaMinima && (
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold text-outline uppercase tracking-tighter">Talla Mínima</span>
              <span className="text-sm font-black text-primary">{tallaMinima}</span>
            </div>
          )}
          {artePesca && artePesca.length > 0 && (
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold text-outline uppercase tracking-tighter">Arte de Pesca</span>
              <div className="flex gap-2">
                {artePesca.slice(0, 3).map((arte) => {
                  const IconComponent = ARTE_PESCA_ICONS[arte] || ARTE_PESCA_ICONS['default']
                  return (
                    <span key={arte} title={arte}>
                      <IconComponent className="h-5 w-5 text-outline" />
                    </span>
                  )
                })}
              </div>
            </div>
          )}
          {zona && (
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold text-outline uppercase tracking-tighter">Zona</span>
              <span className="text-xs font-medium text-on-surface-variant">{zona}</span>
            </div>
          )}
        </div>
      </div>
    </article>
  )
}