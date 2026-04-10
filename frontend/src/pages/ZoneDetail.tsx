import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, MapPin, Fish, ChevronRight } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { useZona, useRegulacionesByZona } from '@/api/hooks'

export function ZoneDetail() {
  const { id } = useParams<{ id: string }>()
  const zonaId = Number(id)

  const { data: zona, isLoading: loadingZona } = useZona(zonaId)
  const { data: regulaciones } = useRegulacionesByZona(zonaId)

  if (loadingZona) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="animate-pulse space-y-4">
          <div className="h-6 bg-gray-200 rounded w-24" />
          <div className="h-48 bg-gray-200 rounded-xl" />
        </div>
      </div>
    )
  }

  if (!zona) {
    return (
      <div className="container mx-auto px-4 py-6">
        <Card>
          <CardContent className="p-8 text-center">
            <MapPin className="h-12 w-12 mx-auto text-text-secondary/50 mb-3" />
            <p className="text-text-secondary">Zona no encontrada</p>
            <Link to="/zonas">
              <button className="mt-4 text-primary hover:underline">Volver a zonas</button>
            </Link>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <Link to="/zonas" className="inline-flex items-center gap-1 text-sm text-text-secondary hover:text-text-primary mb-4">
        <ArrowLeft className="h-4 w-4" />
        Volver
      </Link>

      <div className="mb-6">
        <div className="flex items-center gap-3 mb-2">
          <div className="w-16 h-16 bg-primary-light rounded-xl flex items-center justify-center">
            <MapPin className="h-8 w-8 text-primary" />
          </div>
          <div>
            <h1 className="text-2xl font-bold">{zona.nombre}</h1>
            <p className="text-text-secondary">{zona.macroZona}</p>
          </div>
        </div>
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Información de la Zona</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-xs text-text-secondary">Tipo de Restricción</p>
            <p className="text-sm font-medium">{zona.tipoRestriccion || 'Sin restricción'}</p>
          </div>
          <div>
            <p className="text-xs text-text-secondary">Categoría Hídrica</p>
            <p className="text-sm font-medium">{zona.categoriaHidrica || 'N/A'}</p>
          </div>
          <div>
            <p className="text-xs text-text-secondary">Municipio Sede</p>
            <p className="text-sm font-medium">{zona.municipioSede || 'N/A'}</p>
          </div>
          <div>
            <p className="text-xs text-text-secondary">Área Natural Protegida</p>
            <p className="text-sm font-medium">{zona.esAnp ? 'Sí' : 'No'}</p>
          </div>
        </CardContent>
      </Card>

      {regulaciones && regulaciones.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center justify-between">
              <span className="flex items-center gap-2">
                <Fish className="h-4 w-4" />
                Especies reguladas ({regulaciones.length})
              </span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {regulaciones.map(reg => (
                <Link
                  key={reg.id}
                  to={`/especies/${reg.pezId}`}
                  className="flex items-center justify-between p-3 bg-surface rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <span className="text-sm font-medium">Especie #{reg.pezId}</span>
                  {reg.tallaMinima && (
                    <span className="text-xs text-text-secondary">
                      Talla mín: {reg.tallaMinima}{reg.tipoMedicion ? ` ${reg.tipoMedicion}` : ''}
                    </span>
                  )}
                  <ChevronRight className="h-4 w-4 text-text-secondary" />
                </Link>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}