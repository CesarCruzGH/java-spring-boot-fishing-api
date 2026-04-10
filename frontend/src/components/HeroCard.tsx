import { Sailboat } from 'lucide-react'
import { cn } from '@/lib/utils'

interface HeroCardProps {
  titulo: string
  descripcion: string
  mareas?: string
  temperatura?: string
  oleaje?: string
  lastUpdated?: string
  className?: string
}

export function HeroCard({
  titulo,
  descripcion,
  mareas,
  temperatura,
  oleaje,
  lastUpdated,
  className
}: HeroCardProps) {
  return (
    <div className={cn('bg-gradient-to-br from-primary to-primary-container p-10 rounded-2xl text-white relative overflow-hidden coastal-glow', className)}>
      <div className="relative z-10 space-y-6">
        <div className="flex items-center gap-3">
          <span className="px-3 py-1 bg-secondary-container text-on-secondary-container rounded-full text-xs font-bold tracking-widest uppercase">
            Live Status
          </span>
          {lastUpdated && (
            <span className="text-primary-fixed-dim text-sm font-medium">
              {lastUpdated}
            </span>
          )}
        </div>

        <h2 className="text-5xl font-extrabold tracking-tight font-['Manrope']">
          {titulo}
        </h2>

        <p className="text-primary-fixed max-w-xl text-lg leading-relaxed">
          {descripcion}
        </p>

        {(mareas || temperatura || oleaje) && (
          <div className="flex gap-10 pt-4">
            {mareas && (
              <div>
                <p className="text-primary-fixed-dim text-xs uppercase tracking-widest font-bold mb-1">
                  Mareas
                </p>
                <p className="text-2xl font-bold">{mareas}</p>
              </div>
            )}
            {temperatura && (
              <div>
                <p className="text-primary-fixed-dim text-xs uppercase tracking-widest font-bold mb-1">
                  Temperatura
                </p>
                <p className="text-2xl font-bold">{temperatura}</p>
              </div>
            )}
            {oleaje && (
              <div>
                <p className="text-primary-fixed-dim text-xs uppercase tracking-widest font-bold mb-1">
                  Oleaje
                </p>
                <p className="text-2xl font-bold">{oleaje}</p>
              </div>
            )}
          </div>
        )}
      </div>

      <div className="absolute -right-20 -bottom-20 opacity-10">
        <Sailboat className="h-[300px] w-[300px]" />
      </div>
    </div>
  )
}