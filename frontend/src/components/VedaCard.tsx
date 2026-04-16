import { Link } from 'react-router-dom'
import { Fish } from 'lucide-react'
import { cn } from '@/lib/utils'

interface VedaCardProps {
  id: number
  nombreComun: string
  nombreCientifico?: string | null
  tipoVeda: string
  fechaFin?: string
  descripcion?: string
  imagenUrl?: string
  className?: string
}

export function VedaCard({
  id,
  nombreComun,
  nombreCientifico,
  tipoVeda,
  fechaFin,
  descripcion,
  imagenUrl,
  className
}: VedaCardProps) {
  return (
    <div
      className={cn(
        'group relative overflow-hidden bg-surface-container-low rounded-xl transition-all duration-500 hover:bg-surface-container border border-white/5 shadow-2xl',
        className
      )}
    >
      <div className="h-48 overflow-hidden relative">
        {imagenUrl ? (
          <img
            src={imagenUrl}
            alt={nombreComun}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110 opacity-80"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-surface">
            <Fish className="h-16 w-16 text-white/20" />
          </div>
        )}
        <div className="absolute top-4 left-4 flex gap-2">
          <span className="bg-primary/20 backdrop-blur-md text-primary px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase border border-primary/30">
            {tipoVeda}
          </span>
        </div>
        <div className="absolute inset-0 bg-gradient-to-t from-surface-container-low via-transparent to-transparent" />
      </div>

      <div className="p-6">
        <h3 className="text-2xl font-headline font-bold text-sky-100 mb-1">
          {nombreComun}
        </h3>
        {nombreCientifico && (
          <p className="text-tertiary font-label text-[10px] font-bold tracking-widest uppercase mb-4">
            {nombreCientifico}
          </p>
        )}

        <div className="space-y-4">
          {fechaFin && (
            <div className="flex items-center gap-4 bg-surface-container-lowest/50 p-3 rounded-lg border border-white/5">
              <svg className="w-5 h-5 text-sky-400 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                <line x1="16" y1="2" x2="16" y2="6" />
                <line x1="8" y1="2" x2="8" y2="6" />
                <line x1="3" y1="10" x2="21" y2="10" />
              </svg>
              <div>
                <p className="text-[10px] text-slate-500 font-bold uppercase tracking-tighter">Finaliza</p>
                <p className="text-sky-100 font-semibold">{fechaFin}</p>
              </div>
            </div>
          )}

          {descripcion && (
            <div className="flex items-start gap-3 px-1">
              <svg className="w-4 h-4 text-error shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
              <p className="text-sm text-on-surface-variant leading-relaxed">{descripcion}</p>
            </div>
          )}

          <Link
            to={`/especies/${id}`}
            className="w-full mt-4 py-3 px-4 rounded-md bg-gradient-to-br from-primary to-on-primary-container text-on-primary font-bold text-xs uppercase tracking-widest transition-all hover:brightness-110 active:scale-95 shadow-lg shadow-primary/10 flex items-center justify-center gap-2"
          >
            Detalles de Veda
            <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          </Link>
        </div>
      </div>
    </div>
  )
}
