import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, Fish, MapPin, AlertTriangle, Bug, Calendar, ExternalLink } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { usePez, usePeriodosVedaByPez } from '@/api/hooks'

export function SpeciesDetail() {
  const { id } = useParams<{ id: string }>()
  const pezId = Number(id)

  const { data: pez, isLoading: loadingPez } = usePez(pezId)
  const { data: periodosVeda } = usePeriodosVedaByPez(pezId)

  if (loadingPez) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="animate-pulse space-y-4">
          <div className="h-6 bg-gray-200 rounded w-24" />
          <div className="h-64 bg-gray-200 rounded-xl" />
          <div className="h-32 bg-gray-200 rounded-xl" />
        </div>
      </div>
    )
  }

  if (!pez) {
    return (
      <div className="container mx-auto px-4 py-6">
        <Card>
          <CardContent className="p-8 text-center">
            <Fish className="h-12 w-12 mx-auto text-text-secondary/50 mb-3" />
            <p className="text-text-secondary">Especie no encontrada</p>
            <Link to="/especies">
              <Button variant="ghost" className="mt-4">Volver al catálogo</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <Link to="/especies" className="inline-flex items-center gap-1 text-sm text-text-secondary hover:text-text-primary mb-4">
        <ArrowLeft className="h-4 w-4" />
        Volver
      </Link>

      <div className="relative mb-6">
        <div className="aspect-video bg-primary-light rounded-xl flex items-center justify-center max-h-64">
          <Fish className="h-24 w-24 text-primary/50" />
        </div>
      </div>

      <div className="mb-6">
        <h1 className="text-2xl font-bold">{pez.nombreComun}</h1>
        <p className="text-lg text-text-secondary italic">{pez.nombreCientifico}</p>
        {pez.nombreMaya && (
          <p className="text-sm text-text-secondary">Nombre maya: {pez.nombreMaya}</p>
        )}
        <div className="flex items-center gap-2 mt-3">
          <Badge variant="open">Abierta</Badge>
        </div>
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Información</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-2 gap-4">
          <div className="flex items-start gap-2">
            <MapPin className="h-4 w-4 text-text-secondary mt-0.5" />
            <div>
              <p className="text-xs text-text-secondary">Hábitat</p>
              <p className="text-sm font-medium capitalize">{pez.tipoAgua}</p>
            </div>
          </div>
          <div className="flex items-start gap-2">
            <AlertTriangle className="h-4 w-4 text-text-secondary mt-0.5" />
            <div>
              <p className="text-xs text-text-secondary">Ciguatera</p>
              <p className="text-sm font-medium">{pez.riesgoCiguatera || 'Sin riesgo'}</p>
            </div>
          </div>
          <div className="flex items-start gap-2">
            <Bug className="h-4 w-4 text-text-secondary mt-0.5" />
            <div>
              <p className="text-xs text-text-secondary">Invasiva</p>
              <p className="text-sm font-medium">{pez.esInvasiva ? 'Sí' : 'No'}</p>
            </div>
          </div>
          <div className="flex items-start gap-2">
            <Fish className="h-4 w-4 text-text-secondary mt-0.5" />
            <div>
              <p className="text-xs text-text-secondary">Protegida</p>
              <p className="text-sm font-medium">{pez.esProtegida ? 'Sí' : 'No'}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {periodosVeda && periodosVeda.length > 0 && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <Calendar className="h-4 w-4" />
              Períodos de Veda
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {periodosVeda.map(periodo => (
                <div key={periodo.id} className="flex items-center justify-between p-3 bg-surface rounded-lg">
                  <div>
                    <Badge variant="closed" className="mb-1">{periodo.tipoVeda}</Badge>
                    <p className="text-sm">
                      {periodo.diaInicio}/{periodo.mesInicio} - {periodo.diaFin}/{periodo.mesFin}
                    </p>
                  </div>
                  {periodo.fuenteDof && (
                    <a
                      href="#"
                      className="text-primary text-xs flex items-center gap-1 hover:underline"
                    >
                      <ExternalLink className="h-3 w-3" />
                      DOF
                    </a>
                  )}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {pez.descripcion && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Descripción</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-text-secondary">{pez.descripcion}</p>
          </CardContent>
        </Card>
      )}
    </div>
  )
}