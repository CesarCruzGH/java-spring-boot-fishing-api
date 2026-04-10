import { Fish } from 'lucide-react'
import { cn } from '@/lib/utils'

interface VedaCardProps {
  nombreComun: string
  tipoVeda: string
  fechaFin?: string
  descripcion?: string
  imagenUrl?: string
  className?: string
}

export function VedaCard({
  nombreComun,
  tipoVeda,
  fechaFin,
  descripcion,
  imagenUrl,
  className
}: VedaCardProps) {
  return (
    <div className={cn('bg-surface-container-lowest p-6 rounded-xl veda-border coastal-glow flex items-start gap-5', className)}>
      <div className="w-20 h-20 bg-surface-container-low rounded-lg flex-shrink-0 overflow-hidden">
        {imagenUrl ? (
          <img
            src={imagenUrl}
            alt={nombreComun}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <Fish className="h-10 w-10 text-on-surface-variant/50" />
          </div>
        )}
      </div>

      <div className="flex-1">
        <span className="inline-block px-2 py-0.5 bg-tertiary-fixed text-on-tertiary-fixed-variant text-[10px] font-bold rounded uppercase mb-2">
          {tipoVeda}
        </span>
        <h4 className="text-xl font-bold text-on-surface">{nombreComun}</h4>
        {fechaFin && (
          <p className="text-sm text-tertiary font-semibold mt-1">
            Finaliza: {fechaFin}
          </p>
        )}
        {descripcion && (
          <p className="text-xs text-on-surface-variant mt-2">{descripcion}</p>
        )}
      </div>
    </div>
  )
}