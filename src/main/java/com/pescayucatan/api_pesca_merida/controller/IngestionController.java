package com.pescayucatan.api_pesca_merida.controller;

import com.pescayucatan.api_pesca_merida.enums.EstadoIngestion;
import com.pescayucatan.api_pesca_merida.model.IngestionLog;
import com.pescayucatan.api_pesca_merida.repository.IngestionLogRepository;
import com.pescayucatan.api_pesca_merida.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión manual de la ingesta de datos.
 *
 * ENDPOINTS:
 * - POST   /api/v1/ingestion/trigger  → Ejecutar ingesta manualmente
 * - GET    /api/v1/ingestion/status   → Ver historial de ingestiones
 * - GET    /api/v1/ingestion/latest   → Ver último resultado
 * - DELETE /api/v1/ingestion/logs     → Limpiar logs antiguos (admin)
 *
 * CASOS DE USO:
 * 1. Testing: Ejecutar ingesta bajo demanda sin esperar al cron
 * 2. Emergencia: Forzar sincronización si se detecta inconsistencia
 * 3. Monitoreo: Verificar estado de sincronizaciones
 * 4. Troubleshooting: Ver historial de errores
 *
 * @author Sistema de Ingesta CONAPESCA
 * @since 2026-03-18
 */
@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
@Slf4j
public class IngestionController {

    private final IngestionService ingestionService;
    private final IngestionLogRepository ingestionLogRepository;

    /**
     * Ejecuta manualmente el proceso de ingesta.
     *
     * COMPORTAMIENTO:
     * - Dispara el mismo proceso que ejecutaría el @Scheduled
     * - Respuesta inmediata (no espera a que termine el proceso)
     * - Ver logs del servidor para seguimiento detallado
     *
     * EJEMPLO DE USO:
     * curl -X POST http://localhost:8080/api/v1/ingestion/trigger
     *
     * @return Mensaje de confirmación
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerManual() {
        log.info("🎯 Trigger manual de ingesta solicitado vía API");

        try {
            // Ejecutar en el mismo thread (síncrono)
            // Para ejecución asíncrona, usar @Async
            ingestionService.ejecutarIngestion();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Ingesta ejecutada correctamente");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error en trigger manual: {}", e.getMessage(), e); //security

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error ejecutando ingesta: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Obtiene el historial de ingestiones (últimas 20).
     *
     * RESPUESTA INCLUYE:
     * - ID de la ejecución
     * - Nombre del archivo procesado (especies.csv / vedas.csv)
     * - Hash SHA-256 del contenido
     * - Total de filas procesadas
     * - Estado (COMPLETADO / ERROR / PROCESANDO)
     * - Timestamp de procesamiento
     *
     * EJEMPLO DE USO:
     * curl http://localhost:8080/api/v1/ingestion/status
     *
     * @return Lista de IngestionLog ordenados por fecha descendente
     */
    @GetMapping("/status")
    public ResponseEntity<List<IngestionLog>> getStatus() {
        log.debug("Consultando historial de ingestiones");

        // Obtener últimas 20 ingestiones, más recientes primero
        List<IngestionLog> logs = ingestionLogRepository.findAll(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "procesadoEn"))
        ).getContent();

        return ResponseEntity.ok(logs);
    }

    /**
     * Obtiene el resultado de la última ingesta ejecutada.
     *
     * ÚTIL PARA:
     * - Dashboards que muestran estado actual
     * - Healthchecks que verifican última sincronización
     * - Alertas si la última ejecución falló
     *
     * EJEMPLO DE USO:
     * curl http://localhost:8080/api/v1/ingestion/latest
     *
     * @return IngestionLog más reciente, o 404 si no hay registros
     */
    @GetMapping("/latest")
    public ResponseEntity<IngestionLog> getLatest() {
        log.debug("Consultando última ingesta");

        List<IngestionLog> logs = ingestionLogRepository.findAll(
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "procesadoEn"))
        ).getContent();

        if (logs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(logs.get(0));
    }

    /**
     * Obtiene estadísticas agregadas de ingestiones.
     *
     * MÉTRICAS:
     * - Total de ejecuciones
     * - Ejecuciones exitosas
     * - Ejecuciones con error
     * - Última ejecución
     * - Total de especies procesadas (acumulado)
     * - Total de vedas procesadas (acumulado)
     *
     * EJEMPLO DE USO:
     * curl http://localhost:8080/api/v1/ingestion/stats
     *
     * @return Mapa con estadísticas
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.debug("Consultando estadísticas de ingesta");

        List<IngestionLog> allLogs = ingestionLogRepository.findAll();

        long totalEjecuciones = allLogs.size();
        long exitosas = allLogs.stream()
                .filter(l -> l.getEstado() == EstadoIngestion.COMPLETADO)
                .count();
        long errores = allLogs.stream()
                .filter(l -> l.getEstado() == EstadoIngestion.ERROR)
                .count();

        long totalEspecies = allLogs.stream()
                .filter(l -> "especies.csv".equals(l.getNombreArchivo()))
                .mapToLong(IngestionLog::getTotalFilas)
                .sum();

        long totalVedas = allLogs.stream()
                .filter(l -> "vedas.csv".equals(l.getNombreArchivo()))
                .mapToLong(IngestionLog::getTotalFilas)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEjecuciones", totalEjecuciones);
        stats.put("exitosas", exitosas);
        stats.put("errores", errores);
        stats.put("tasaExito", totalEjecuciones > 0 ? (exitosas * 100.0 / totalEjecuciones) : 0);
        stats.put("totalEspeciesProcesadas", totalEspecies);
        stats.put("totalVedasProcesadas", totalVedas);

        // Última ejecución
        if (!allLogs.isEmpty()) {
            allLogs.sort((a, b) -> b.getProcesadoEn().compareTo(a.getProcesadoEn()));
            stats.put("ultimaEjecucion", allLogs.get(0).getProcesadoEn());
            stats.put("ultimoEstado", allLogs.get(0).getEstado());
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * Limpia logs de ingesta antiguos (mayores a N días).
     *
     * ⚠️ ENDPOINT ADMINISTRATIVO - Considerar agregar seguridad
     *
     * PARÁMETROS:
     * - diasRetencion: Mantener solo logs de los últimos N días (default: 30)
     *
     * EJEMPLO DE USO:
     * curl -X DELETE "http://localhost:8080/api/v1/ingestion/logs?diasRetencion=30"
     *
     * @param diasRetencion Días de retención (default 30)
     * @return Cantidad de registros eliminados
     */
    @DeleteMapping("/logs")
    public ResponseEntity<Map<String, Object>> cleanupLogs(
            @RequestParam(defaultValue = "30") int diasRetencion) {

        log.info("🗑️  Limpieza de logs solicitada (retención: {} días)", diasRetencion);

        /*
            TODO Implementar logica de limpieza
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(diasRetencion);
            long deleted = ingestionLogRepository.deleteByProcesadoEnBefore(cutoffDate);
        */
        Map<String, Object> response = new HashMap<>();
        response.put("status", "not_implemented");
        response.put("message", "Funcionalidad pendiente de implementar");

        return ResponseEntity.ok(response);
    }

    /**
     * Health check específico de ingesta.
     *
     * VERIFICA:
     * - ¿Se está ejecutando alguna ingesta?
     * - ¿Cuándo fue la última ejecución exitosa?
     * - ¿Hay errores recientes?
     *
     * EJEMPLO DE USO:
     * curl http://localhost:8080/api/v1/ingestion/health
     *
     * @return Estado de salud del sistema de ingesta
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        List<IngestionLog> recentLogs = ingestionLogRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "procesadoEn"))
        ).getContent();

        if (recentLogs.isEmpty()) {
            health.put("status", "unknown");
            health.put("message", "No hay registros de ingesta");
            return ResponseEntity.ok(health);
        }

        IngestionLog latest = recentLogs.get(0);

        // Verificar si última ejecución fue exitosa
        boolean isHealthy = latest.getEstado() == EstadoIngestion.COMPLETADO;

        health.put("status", isHealthy ? "healthy" : "unhealthy");
        health.put("ultimaEjecucion", latest.getProcesadoEn());
        health.put("ultimoEstado", latest.getEstado());
        health.put("minutosDesdeUltimaEjecucion",
                java.time.Duration.between(latest.getProcesadoEn(),
                        java.time.LocalDateTime.now()).toMinutes());

        // Contar errores recientes (últimas 5 ejecuciones)
        long erroresRecientes = recentLogs.stream()
                .filter(l -> l.getEstado() == EstadoIngestion.ERROR)
                .count();

        health.put("erroresRecientes", erroresRecientes);

        return ResponseEntity.ok(health);
    }
}