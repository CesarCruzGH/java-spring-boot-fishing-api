import { Link } from 'react-router-dom'
import { cn } from '@/lib/utils'

interface SpeciesCardProps {
  id: number
  nombreComun: string
  nombreMaya?: string | null
  nombreCientifico: string
  imagenUrl?: string | null
  tipoAgua?: string | null
  riesgoCiguatera?: string | null
  className?: string
}

const CIGUATERA_COLORS: Record<string, string> = {
  ALTO: 'text-error',
  MEDIO: 'text-warning',
  BAJO: 'text-secondary',
  NULO: 'text-secondary',
}

const TIPO_AGUA_LABELS: Record<string, string> = {
  DULCE: 'Dulceacuícola',
  SALADA: 'Marino',
  SALOBRE: 'Salobre',
}

export function SpeciesCard({
  id,
  nombreComun,
  nombreMaya,
  nombreCientifico,
  imagenUrl,
  tipoAgua,
  riesgoCiguatera,
  className
}: SpeciesCardProps) {
  return (
    <Link
      to={`/especies/${id}`}
      className={cn(
        'group relative flex flex-col bg-transparent rounded-2xl glow-border transition-all duration-500 hover:scale-[1.02] overflow-hidden',
        className
      )}
    >
      <div className="relative h-72 overflow-hidden">
        {imagenUrl ? (
          <img
            src={imagenUrl}
            alt={nombreComun}
            className="w-full h-full object-cover mix-blend-screen opacity-80 group-hover:opacity-100 transition-opacity"
          />
        ) : (
          <div className="w-full h-full bg-surface-container-low flex items-center justify-center">
            <span className="text-6xl opacity-30">🐟</span>
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-[#0a1325] via-transparent to-transparent" />
        <div className="absolute top-4 right-4">
          {tipoAgua && (
            <span className="bg-tertiary-container/40 backdrop-blur-md px-3 py-1 rounded-full text-[10px] font-bold tracking-widest uppercase border border-tertiary/20 text-tertiary">
              {TIPO_AGUA_LABELS[tipoAgua] || tipoAgua}
            </span>
          )}
        </div>
      </div>

      <div className="p-6 pt-2">
        <h3 className="text-2xl font-headline font-bold text-primary mb-1">
          {nombreComun}
        </h3>
        {nombreMaya && (
          <p className="text-xs font-label text-tertiary/60 mb-1 uppercase tracking-wider">
            {nombreMaya} <span className="text-on-surface-variant font-normal lowercase ml-1">(maya)</span>
          </p>
        )}
        <p className="text-xs font-label text-tertiary/60 mb-4 italic">
          {nombreCientifico}
        </p>

        <div className="space-y-3 mb-6">
          {tipoAgua && (
            <div className="flex justify-between text-xs border-b border-white/5 pb-2">
              <span className="text-on-surface-variant uppercase tracking-tighter">Hábitat</span>
              <span className="text-on-surface font-medium">{TIPO_AGUA_LABELS[tipoAgua] || tipoAgua}</span>
            </div>
          )}
          {riesgoCiguatera && (
            <div className="flex justify-between text-xs border-b border-white/5 pb-2">
              <span className="text-on-surface-variant uppercase tracking-tighter">Ciguatera</span>
              <span className={cn('font-medium', CIGUATERA_COLORS[riesgoCiguatera] || 'text-on-surface')}>
                {riesgoCiguatera}
              </span>
            </div>
          )}
          <div className="flex justify-between text-xs pb-2">
            <span className="text-on-surface-variant uppercase tracking-tighter">Estatus</span>
            <span className="text-tertiary font-bold">AUTORIZADO</span>
          </div>
        </div>

        <button className="w-full py-3 abyssal-gradient text-on-primary-fixed font-headline font-bold text-xs uppercase tracking-widest rounded-lg shadow-[0_0_20px_rgba(152,203,255,0.2)]">
          Ver Detalles
        </button>
      </div>
    </Link>
  )
}
