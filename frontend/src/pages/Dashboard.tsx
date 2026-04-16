import { Link } from 'react-router-dom'
import { ChevronRight, Fish, ArrowRight } from 'lucide-react'
import { useState } from 'react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { HeroSection } from '@/components/HeroSection'
import { VedaCard } from '@/components/VedaCard'
import { SpeciesCard } from '@/components/SpeciesCard'
import { ZoneMapCard } from '@/components/ZoneMapCard'
import { usePecesAbiertos, useZonasConEstadoDetallado, useVedasActivasDashboard } from '@/api/hooks'

export function Dashboard() {
  const [searchQuery, setSearchQuery] = useState('')
  const { data: pecesAbiertos, isLoading } = usePecesAbiertos()
  const { zonas: zonasConEstado, isLoading: loadingZonas } = useZonasConEstadoDetallado()
  const { vedas: vedasActivas, isLoading: loadingVedas } = useVedasActivasDashboard()

  return (
    <div className="min-h-screen flex flex-col">
      <HeroSection />

      <div className="p-8 space-y-12 flex-1">
        <div className="relative">
          <Input
            type="search"
            placeholder="Buscar especie o zona..."
            className="pl-10 w-full max-w-md"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <section className="space-y-6">
          <div className="flex items-end justify-between">
            <div>
              <h2 className="text-3xl font-black text-on-surface tracking-tight font-['Manrope']">
                Alertas de Veda
              </h2>
              <p className="text-on-surface-variant mt-1">
                Especies actualmente en restricción total o parcial.
              </p>
            </div>
            <Link
              to="/especies"
              className="text-primary font-bold flex items-center gap-1 group"
            >
              Ver Calendario Completo
              <ArrowRight className="h-4 w-4 group-hover:translate-x-1 transition-transform" />
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {loadingVedas ? (
              <>
                {[1, 2, 3].map(i => (
                  <div key={i} className="bg-surface-container-lowest p-6 rounded-xl animate-pulse">
                    <div className="flex items-start gap-5">
                      <div className="w-20 h-20 bg-surface-container-low rounded-lg" />
                      <div className="flex-1 space-y-2">
                        <div className="h-4 bg-surface-container-low rounded w-3/4" />
                        <div className="h-3 bg-surface-container-low rounded w-1/2" />
                      </div>
                    </div>
                  </div>
                ))}
              </>
            ) : vedasActivas.length > 0 ? (
              vedasActivas.map((veda) => (
                <VedaCard
                  key={veda.id}
                  id={veda.id}
                  nombreComun={veda.nombreComun}
                  nombreCientifico={veda.nombreCientifico}
                  tipoVeda={veda.tipoVeda}
                  fechaFin={veda.fechaFin}
                  descripcion={veda.descripcion}
                  imagenUrl={veda.imagenUrl}
                />
              ))
            ) : (
              <p className="text-on-surface-variant col-span-full">No hay alertas de veda activas.</p>
            )}
          </div>
        </section>

        <div className="grid grid-cols-1 xl:grid-cols-3 gap-8">
          <div className="xl:col-span-2 space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold text-on-surface font-['Manrope']">
                Especies Autorizadas
              </h2>
              <div className="flex gap-2">
                <Button variant="ghost" size="icon">
                  <Fish className="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="icon">
                  <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            </div>

            {isLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {[1, 2, 3, 4].map(i => (
                  <div key={i} className="bg-surface-container-lowest p-5 rounded-xl animate-pulse">
                    <div className="flex gap-4">
                      <div className="w-16 h-16 bg-surface-container-low rounded-lg" />
                      <div className="flex-1 space-y-2">
                        <div className="h-4 bg-surface-container-low rounded w-3/4" />
                        <div className="h-3 bg-surface-container-low rounded w-1/2" />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
                {pecesAbiertos?.slice(0, 6).map((pez) => (
                  <SpeciesCard
                    key={pez.id}
                    id={pez.id}
                    nombreComun={pez.nombreComun}
                    nombreMaya={pez.nombreMaya}
                    nombreCientifico={pez.nombreCientifico}
                    imagenUrl={pez.imagenUrl}
                    tipoAgua={pez.tipoAgua}
                    riesgoCiguatera={pez.riesgoCiguatera}
                  />
                ))}
              </div>
            )}

            <Link
              to="/especies"
              className="inline-flex items-center gap-2 text-primary font-bold hover:underline"
            >
              Ver todas las especies <ArrowRight className="h-4 w-4" />
            </Link>
          </div>

          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-on-surface font-['Manrope']">
              Estatus por Zonas
            </h2>
            {loadingZonas ? (
              <div className="bg-surface-container-lowest p-6 rounded-xl animate-pulse">
                <div className="h-64 bg-surface-container-low rounded-lg mb-4" />
                <div className="space-y-2">
                  <div className="h-4 bg-surface-container-low rounded w-3/4" />
                  <div className="h-4 bg-surface-container-low rounded w-1/2" />
                </div>
              </div>
            ) : zonasConEstado.length > 0 ? (
              <ZoneMapCard zones={zonasConEstado} />
            ) : (
              <div className="bg-surface-container-lowest p-6 rounded-xl text-center text-on-surface-variant">
                No hay zonas disponibles
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}