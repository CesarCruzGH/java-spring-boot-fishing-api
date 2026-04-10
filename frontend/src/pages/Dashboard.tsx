import { Link } from 'react-router-dom'
import { ChevronRight, Fish, ArrowRight } from 'lucide-react'
import { useState } from 'react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { HeroCard } from '@/components/HeroCard'
import { VedaCard } from '@/components/VedaCard'
import { SpeciesCard } from '@/components/SpeciesCard'
import { ZoneMapCard } from '@/components/ZoneMapCard'
import { usePeces, useZonasConEstadoDetallado } from '@/api/hooks'

export function Dashboard() {
  const [searchQuery, setSearchQuery] = useState('')
  const { data: peces, isLoading } = usePeces()
  const { zonas: zonasConEstado, isLoading: loadingZonas } = useZonasConEstadoDetallado()

  const mockVedas = [
    {
      id: 1,
      nombreComun: 'Mero (Grouper)',
      tipoVeda: 'Veda Estacional',
      fechaFin: '31 de Marzo',
      descripcion: 'Restricción total en toda la costa de Yucatán.',
      imagenUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBTpz37c15bnZBc4UaSXotAvKBYIbKBYtoBYtGOJpjzy7kdoBEdnA0da0B4SwkdFEUBl54qnJmDPc_FGM_b5JRWuEJrcqmaJIzBSXhdkeyuOBA6IG0Qx-Wc8PARHbbgVNuzwUplE8etNbKkIj08Wr5y9xuRNqeKGC_SQVIore1XbyNY1TZr3ataCcXX8HpAjcX4iVolc8ClBoGQkSK9oUXPXp9JDzXcFDKriWdhNWsWhet5Vt33ZES9j7ojYsb-SYXrMT4f6_wPKQK1'
    },
    {
      id: 2,
      nombreComun: 'Pulpo Maya',
      tipoVeda: 'Veda Permanente',
      fechaFin: 'Fuera de Temporada',
      descripcion: 'Captura prohibida para preservar población juvenil.',
      imagenUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBt9dhWvFND0vTr8MBVH-h4XPIg0RPCy-6RdkqlRK51WWw1UW-R7_lOCEGg29yjUqMbn3gmtfyuHBGVpM0adKf_5gepE9XszJqlhWhV2XXvTygBDvxoaV4VbQ64cuJIFI_eVcoiJ1JsihXJyUgk94QBa9tw4LjLjs2IgOZeXVjxyPxY3faYaPLgr4eCDqcSS-ERQUfTfPDx2swYBmcc2JtUmZHzeGyKAbVs0IwRyAF9f2scPtmqucNquIUbWP4jIL7bg8gYEDGi94gt'
    },
    {
      id: 3,
      nombreComun: 'Caracol Rosado',
      tipoVeda: 'Protección Multi-Anual',
      fechaFin: 'Especie Protegida',
      descripcion: 'Prohibición absoluta bajo norma federal NOM-029.',
      imagenUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBmfi1ZbhtM2IhElMSF7GEqRaXV1gyVMInhTMrwvJXfuMcaPD7DUQW9u9W6-ScpgUJlhcgqcPCs-8yeu1fMKahQNq1E0FLYfU89CC9evueyPbxT-U59gNAg0Uqu3jpJTkcXLvw6Mhpl5PpLCmQ3p7TcYJLnQQlQKCewHOVdFPsmQtWwzyKa_HT4n98wnGSz7VZUKF8rIvY_nOs1olq4pQ9LRoIyJCwCGKfysf4rZy1KTe5vw0qquHyAp6NIJZJE82mdMPjQFERZl0D8'
    }
  ]

  return (
    <div className="min-h-screen flex flex-col">
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

        <section className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-3">
            <HeroCard
              titulo="¿Qué puedo pescar hoy?"
              descripcion="Las condiciones marítimas en el litoral yucateco son favorables. La visibilidad es de 12 millas náuticas con vientos moderados del Noreste."
              mareas="Alta: 14:30h"
              temperatura="27°C Coral"
              oleaje="0.8m Calmo"
              lastUpdated="Actualizado hace 5m"
            />
          </div>
        </section>

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
            {mockVedas.map((veda) => (
              <VedaCard
                key={veda.id}
                nombreComun={veda.nombreComun}
                tipoVeda={veda.tipoVeda}
                fechaFin={veda.fechaFin}
                descripcion={veda.descripcion}
                imagenUrl={veda.imagenUrl}
              />
            ))}
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
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {peces?.slice(0, 4).map((pez) => (
                  <SpeciesCard
                    key={pez.id}
                    nombreComun={pez.nombreComun}
                    nombreCientifico={pez.nombreCientifico}
                    estado="open"
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