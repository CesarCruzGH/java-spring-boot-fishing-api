import { Link } from 'react-router-dom'
import { MapPin, ChevronRight } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { useZonas } from '@/api/hooks'

export function Zonas() {
  const { data: zonas, isLoading } = useZonas()

  return (
    <div className="min-h-screen flex flex-col">
      <div className="p-8 space-y-6">
        <div className="flex items-center gap-3">
          <MapPin className="h-6 w-6 text-primary" />
          <h1 className="text-3xl font-black text-on-surface tracking-tight font-['Manrope']">
            Zonas de Pesca
          </h1>
        </div>

        {isLoading ? (
          <div className="space-y-4">
            {[1, 2, 3].map(i => (
              <Card key={i}>
                <CardContent className="p-6">
                  <div className="animate-pulse space-y-3">
                    <div className="h-5 bg-surface-container-low rounded w-1/3" />
                    <div className="h-4 bg-surface-container-low rounded w-2/3" />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : zonas && zonas.length > 0 ? (
          <div className="space-y-4">
            {zonas.map(zona => (
              <Link key={zona.id} to={`/zonas/${zona.id}`}>
                <Card className="hover:border-primary/50 transition-colors cursor-pointer">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex items-start gap-4">
                        <div className="w-14 h-14 bg-primary-container rounded-xl flex items-center justify-center flex-shrink-0">
                          <MapPin className="h-7 w-7 text-on-primary-container" />
                        </div>
                        <div>
                          <h2 className="font-bold text-lg text-on-surface">{zona.nombre}</h2>
                          <p className="text-sm text-on-surface-variant">{zona.macroZona}</p>
                          {zona.notasEspecificas && (
                            <p className="text-xs text-on-surface-variant mt-2 line-clamp-2">
                              {zona.notasEspecificas}
                            </p>
                          )}
                        </div>
                      </div>
                      <ChevronRight className="h-5 w-5 text-on-surface-variant flex-shrink-0" />
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        ) : (
          <Card>
            <CardContent className="p-8 text-center">
              <MapPin className="h-12 w-12 mx-auto text-on-surface-variant/50 mb-3" />
              <p className="text-on-surface-variant">No hay zonas disponibles</p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}