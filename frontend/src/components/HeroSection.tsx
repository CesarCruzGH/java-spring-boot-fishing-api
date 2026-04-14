import { Link } from 'react-router-dom'
import { ChevronDown } from 'lucide-react'

export function HeroSection() {
  return (
    <div className="relative w-full h-[75vh] overflow-hidden">
      <img
        src="/images/hero-yucatan.jpg"
        alt="Costa de Yucatán"
        className="absolute inset-0 w-full h-full object-cover"
      />

      <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/30 to-transparent" />

      <div className="absolute inset-0 flex flex-col justify-end pb-32 px-8 md:px-16">
        <div className="max-w-3xl">
          <h1 className="text-4xl md:text-6xl font-bold text-white mb-4 font-['Manrope'] tracking-tight">
            Regulación Pesquera
          </h1>
          <p className="text-lg md:text-xl text-white/80 mb-8 max-w-2xl">
            Consulta las vedas, tallas mínimas y zonas de pesca en el Estado de Yucatán.
            Toda la información actualizada directamente del Diario Oficial de la Federación.
          </p>
          <div className="flex flex-wrap gap-4">
            <Link
              to="/especies"
              className="inline-flex items-center px-6 py-3 bg-primary text-white font-semibold rounded-lg hover:bg-primary/90 transition-colors"
            >
              Ver Especies
            </Link>
            <Link
              to="/zonas"
              className="inline-flex items-center px-6 py-3 bg-white/10 text-white font-semibold rounded-lg hover:bg-white/20 transition-colors backdrop-blur-sm border border-white/20"
            >
              Ver Zonas
            </Link>
          </div>
        </div>
      </div>

      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce">
        <ChevronDown className="h-8 w-8 text-white/60" />
      </div>
    </div>
  )
}
