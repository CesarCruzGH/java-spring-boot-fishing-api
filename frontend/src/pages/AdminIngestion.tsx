import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowLeft, RefreshCw, CheckCircle, AlertCircle, Clock, Loader2, BarChart3 } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { api, type IngestionLog } from '@/lib/api'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleDateString('es-MX', {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function AdminIngestion() {
  const queryClient = useQueryClient()
  const [isTriggering, setIsTriggering] = useState(false)
  const [triggerError, setTriggerError] = useState<string | null>(null)

  const { data: latestLog, isLoading: loadingLatest } = useQuery({
    queryKey: ['ingestion', 'latest'],
    queryFn: api.ingestion.latest,
    refetchInterval: 30000,
  })

  const { data: statusLogs, isLoading: loadingStatus } = useQuery({
    queryKey: ['ingestion', 'status'],
    queryFn: api.ingestion.status,
  })

  const triggerMutation = useMutation({
    mutationFn: async () => {
      setIsTriggering(true)
      setTriggerError(null)
      try {
        await api.ingestion.trigger()
        queryClient.invalidateQueries({ queryKey: ['ingestion'] })
      } catch (err) {
        setTriggerError(err instanceof Error ? err.message : 'Error al triggerear ingesta')
        throw err
      } finally {
        setIsTriggering(false)
      }
    },
  })

  const handleTrigger = () => {
    triggerMutation.mutate()
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <Link to="/" className="inline-flex items-center gap-1 text-sm text-text-secondary hover:text-text-primary mb-4">
        <ArrowLeft className="h-4 w-4" />
        Volver
      </Link>

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">Panel de Administración</h1>
          <p className="text-sm text-text-secondary">Gestión de ingesta de datos</p>
        </div>
        <Link to="/admin/stats">
          <Button variant="outline" size="sm">
            <BarChart3 className="h-4 w-4 mr-2" />
            Estadísticas
          </Button>
        </Link>
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Estado de Ingesta</CardTitle>
        </CardHeader>
        <CardContent>
          {loadingLatest ? (
            <div className="animate-pulse space-y-2">
              <div className="h-4 bg-gray-200 rounded w-1/4" />
              <div className="h-4 bg-gray-200 rounded w-1/2" />
            </div>
          ) : latestLog ? (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <Badge variant={latestLog.estado === 'COMPLETED' ? 'open' : 'closed'}>
                  {latestLog.estado === 'COMPLETED' ? (
                    <CheckCircle className="h-3 w-3 mr-1" />
                  ) : (
                    <AlertCircle className="h-3 w-3 mr-1" />
                  )}
                  {latestLog.estado}
                </Badge>
                <span className="text-sm text-text-secondary">
                  <Clock className="h-3 w-3 inline mr-1" />
                  {formatDate(latestLog.procesadoEn)}
                </span>
              </div>
              <div className="grid grid-cols-3 gap-4 text-center">
                <div className="bg-surface rounded-lg p-3">
                  <p className="text-2xl font-bold">{latestLog.totalFilas.toLocaleString()}</p>
                  <p className="text-xs text-text-secondary">Total filas</p>
                </div>
                <div className="bg-open/10 rounded-lg p-3">
                  <p className="text-2xl font-bold text-open">{latestLog.filasExitosas.toLocaleString()}</p>
                  <p className="text-xs text-text-secondary">Exitosas</p>
                </div>
                <div className="bg-closed/10 rounded-lg p-3">
                  <p className="text-2xl font-bold text-closed">{latestLog.filasError.toLocaleString()}</p>
                  <p className="text-xs text-text-secondary">Errores</p>
                </div>
              </div>
            </div>
          ) : (
            <p className="text-sm text-text-secondary">No hay datos de ingesta</p>
          )}
        </CardContent>
      </Card>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Acciones</CardTitle>
        </CardHeader>
        <CardContent>
          <Button onClick={handleTrigger} disabled={isTriggering} className="w-full sm:w-auto">
            {isTriggering ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Procesando...
              </>
            ) : (
              <>
                <RefreshCw className="h-4 w-4 mr-2" />
                Trigger Ingesta Manual
              </>
            )}
          </Button>
          {triggerError && (
            <p className="text-sm text-closed mt-2">{triggerError}</p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Historial de Ingestiones</CardTitle>
        </CardHeader>
        <CardContent>
          {loadingStatus ? (
            <div className="animate-pulse space-y-2">
              {[1, 2, 3].map(i => (
                <div key={i} className="h-12 bg-gray-200 rounded" />
              ))}
            </div>
          ) : statusLogs && statusLogs.length > 0 ? (
            <div className="space-y-2">
              {statusLogs.map((log: IngestionLog) => (
                <div key={log.id} className="flex items-center justify-between p-3 bg-surface rounded-lg">
                  <div className="flex items-center gap-3">
                    {log.estado === 'COMPLETED' ? (
                      <CheckCircle className="h-4 w-4 text-open" />
                    ) : (
                      <AlertCircle className="h-4 w-4 text-closed" />
                    )}
                    <div>
                      <p className="text-sm font-medium">{formatDate(log.procesadoEn)}</p>
                      <p className="text-xs text-text-secondary">{log.nombreArchivo}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <Badge variant={log.estado === 'COMPLETED' ? 'open' : 'closed'} className="text-xs">
                      {log.estado}
                    </Badge>
                    <p className="text-xs text-text-secondary mt-1">
                      {log.filasExitosas}/{log.totalFilas} filas
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-text-secondary text-center py-4">No hay historial de ingestiones</p>
          )}
        </CardContent>
      </Card>
    </div>
  )
}