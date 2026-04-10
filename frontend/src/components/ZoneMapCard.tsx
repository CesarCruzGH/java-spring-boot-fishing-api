import { CheckCircle, AlertTriangle } from 'lucide-react'
import { cn } from '@/lib/utils'
import { YucatanMap } from './YucatanMap'

interface ZoneMapCardProps {
  zones: Array<{
    id: number
    nombre: string
    status: 'open' | 'warning'
    nota?: string
    totalEspecies?: number
    especiesAbiertas?: number
    especiesEnVeda?: number
    vedasActivas?: Array<{
      pezId: number
      pezNombre: string
      tipoVeda: string
      fechaFin: string
    }>
  }>
  className?: string
}

export function ZoneMapCard({
  zones,
  className
}: ZoneMapCardProps) {
  const zonasConVeda = zones.filter(z => z.especiesEnVeda && z.especiesEnVeda > 0)
  const primeraNotaVeda = zonasConVeda[0]?.vedasActivas?.[0]

  return (
    <div className={cn('bg-surface-container-lowest rounded-2xl overflow-hidden coastal-glow flex flex-col h-full', className)}>
      <div className="aspect-square bg-surface-container-high relative">
        <YucatanMap zones={zones} className="w-full h-full" />
        <div className="absolute inset-0 bg-primary/5 pointer-events-none" />
      </div>

      <div className="p-6 space-y-4">
        {zones.map((zone) => (
          <div key={zone.id} className="flex items-center justify-between">
            <div className="flex-1 min-w-0">
              <span className="text-sm font-semibold block truncate">{zone.nombre}</span>
              {zone.totalEspecies !== undefined && (
                <span className="text-xs text-on-surface-variant">
                  {zone.totalEspecies} especies
                  {zone.especiesEnVeda !== undefined && zone.especiesEnVeda > 0 && (
                    <span className="text-tertiary"> | {zone.especiesEnVeda} en veda</span>
                  )}
                </span>
              )}
            </div>
            {zone.status === 'open' ? (
              <CheckCircle className="h-5 w-5 text-secondary flex-shrink-0 ml-2" />
            ) : (
              <AlertTriangle className="h-5 w-5 text-tertiary flex-shrink-0 ml-2" />
            )}
          </div>
        ))}

        {primeraNotaVeda && (
          <div className="pt-4 border-t border-outline-variant/20">
            <p className="text-xs text-on-surface-variant leading-relaxed">
              <strong className="text-on-surface block mb-1">Veda Activa:</strong>
              {primeraNotaVeda.pezNombre} - {primeraNotaVeda.tipoVeda} hasta {primeraNotaVeda.fechaFin}
            </p>
          </div>
        )}

        {!primeraNotaVeda && zones.some(z => z.nota) && (
          <div className="pt-4 border-t border-outline-variant/20">
            <p className="text-xs text-on-surface-variant leading-relaxed">
              <strong className="text-on-surface block mb-1">Nota del Navegante:</strong>
              {zones.find(z => z.nota)?.nota}
            </p>
          </div>
        )}
      </div>
    </div>
  )
}