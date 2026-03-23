package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.infrastructure.csv.EspecieCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.VedaCsvRow;
import com.pescayucatan.api_pesca_merida.model.EspecieVeda;
import com.pescayucatan.api_pesca_merida.model.IngestionLog;
import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.enums.EstadoIngestion;
import com.pescayucatan.api_pesca_merida.repository.EspecieVedaRepository;
import com.pescayucatan.api_pesca_merida.repository.IngestionLogRepository;
import com.pescayucatan.api_pesca_merida.repository.PezRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio orquestador de la ingestión de datos desde Google Sheets.
 *
 * RESPONSABILIDADES:
 * 1. Descargar CSVs de Google Sheets (Especies y Vedas)
 * 2. Calcular hash SHA-256 para detección de cambios
 * 3. Verificar si los datos ya fueron procesados (idempotencia)
 * 4. Parsear CSVs usando CsvParserService
 * 5. Hacer UPSERT en base de datos (Pez y EspecieVeda)
 * 6. Registrar resultado en IngestionLog
 *
 * EJECUCIÓN:
 * - Automática: vía @Scheduled según cron configurado
 * - Manual: vía endpoint REST (IngestionController)
 *
 * CONFIGURACIÓN:
 * - ingestion.sheets.especies-url: URL de export CSV de especies
 * - ingestion.sheets.vedas-url: URL de export CSV de vedas
 * - ingestion.cron: Expresión cron para ejecución automática
 * - ingestion.enabled: true/false para habilitar/deshabilitar
 *
 * @author Sistema de Ingesta CONAPESCA
 * @since 2026-03-18
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ingestion.enabled", havingValue = "true", matchIfMissing = true)
public class IngestionService {

    // ============================================================================
    // DEPENDENCIAS
    // ============================================================================

    private final CsvParserService csvParser;
    private final PezRepository pezRepository;
    private final EspecieVedaRepository especieVedaRepository;
    private final IngestionLogRepository ingestionLogRepository;

    // Cliente HTTP reutilizable (thread-safe)
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.ALWAYS) // <--- Esta es la clave
            .build();

    // ============================================================================
    // CONFIGURACIÓN (desde application.properties)
    // ============================================================================

    @Value("${ingestion.sheets.especies-url}")
    private String especiesUrl;

    @Value("${ingestion.sheets.vedas-url}")
    private String vedasUrl;

    // ============================================================================
    // MÉTODO PRINCIPAL (ORQUESTADOR)
    // ============================================================================

    /**
     * Ejecuta el proceso completo de ingestión.
     *
     * FLUJO:
     * 1. Ingerir especies (descargar → validar → parsear → upsert)
     * 2. Ingerir vedas (mismo proceso)
     * 3. Logging de resultados
     *
     * Puede ser invocado por:
     * - @Scheduled (automático según cron)
     * - IngestionController (manual vía API)
     */
    @Scheduled(initialDelay = 30000, fixedDelay = 600000)
    public void ejecutarIngestion() {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║  🔄 INICIANDO PROCESO DE INGESTIÓN CONAPESCA                  ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");

        LocalDateTime inicio = LocalDateTime.now();

        try {
            // Fase 1: Ingerir especies
            log.info("📥 Fase 1/2: Procesando CSV de Especies...");
            IngestionLog especiesLog = procesarEspecies();

            // Fase 2: Ingerir vedas
            log.info("📥 Fase 2/2: Procesando CSV de Vedas...");
            IngestionLog vedasLog = procesarVedas();

            // Resumen final
            LocalDateTime fin = LocalDateTime.now();
            Duration duracion = Duration.between(inicio, fin);

            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║  ✅ INGESTIÓN COMPLETADA EXITOSAMENTE                         ║");
            log.info("╠════════════════════════════════════════════════════════════════╣");
            log.info("║  Especies: {} filas procesadas                                ",
                    especiesLog != null ? especiesLog.getTotalFilas() : 0);
            log.info("║  Vedas:    {} filas procesadas                                ",
                    vedasLog != null ? vedasLog.getTotalFilas() : 0);
            log.info("║  Duración: {} segundos                                        ",
                    duracion.getSeconds());
            log.info("╚════════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║  ❌ ERROR CRÍTICO EN INGESTIÓN                                ║");
            log.error("╚════════════════════════════════════════════════════════════════╝");
            log.error("Error: {}", e.getMessage(), e);

            // Registrar error en log
            registrarError(e);
        }
    }

    // ============================================================================
    // PROCESAMIENTO DE ESPECIES
    // ============================================================================

    /**
     * Descarga, valida y procesa el CSV de especies.
     *
     * @return IngestionLog con resultado del procesamiento, o null si se saltó
     */
    private IngestionLog procesarEspecies() {
        try {
            // 1. Descargar CSV
            log.debug("Descargando CSV de especies desde: {}", especiesUrl);
            byte[] csvBytes = descargarCsv(especiesUrl);

            // 2. Calcular hash
            String hash = calcularSha256(csvBytes);
            log.debug("Hash SHA-256 calculado: {}", hash);

            // 3. Verificar si ya fue procesado (idempotencia)
            if (ingestionLogRepository.existsByHashSha256(hash)) {
                log.info("⏭️  CSV de especies sin cambios (hash: {}...), SKIP",
                        hash.substring(0, 8));
                return null;
            }

            // 4. Parsear CSV
            List<EspecieCsvRow> especies = csvParser.parsePeces(csvBytes);
            log.info("✅ Parseadas {} especies", especies.size());

            // 5. Crear registro de log (PROCESANDO)
            IngestionLog registro = crearIngestionLog("especies.csv", hash,
                    especies.size(), EstadoIngestion.PROCESANDO);

            // 6. UPSERT en base de datos
            int actualizadas = upsertEspecies(especies);
            log.info("💾 {} especies actualizadas/insertadas en BD", actualizadas);

            // 7. Actualizar log a COMPLETADO
            registro.setEstado(EstadoIngestion.COMPLETADO);
            return ingestionLogRepository.save(registro);

        } catch (Exception e) {
            log.error("❌ Error procesando especies: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en procesamiento de especies", e);
        }
    }

    // ============================================================================
    // PROCESAMIENTO DE VEDAS
    // ============================================================================

    /**
     * Descarga, valida y procesa el CSV de vedas.
     *
     * @return IngestionLog con resultado del procesamiento, o null si se saltó
     */
    private IngestionLog procesarVedas() {
        try {
            // 1. Descargar CSV
            log.debug("Descargando CSV de vedas desde: {}", vedasUrl);
            byte[] csvBytes = descargarCsv(vedasUrl);

            // 2. Calcular hash
            String hash = calcularSha256(csvBytes);
            log.debug("Hash SHA-256 calculado: {}", hash);

            // 3. Verificar si ya fue procesado
            if (ingestionLogRepository.existsByHashSha256(hash)) {
                log.info("⏭️  CSV de vedas sin cambios (hash: {}...), SKIP",
                        hash.substring(0, 8));
                return null;
            }

            // 4. Parsear CSV
            List<VedaCsvRow> vedas = csvParser.parseVedas(csvBytes);
            log.info("✅ Parseadas {} vedas", vedas.size());

            // 5. Crear registro (Cambiamos el nombre de la variable a 'registro')
            IngestionLog registro = crearIngestionLog("vedas.csv", hash,
                    vedas.size(), EstadoIngestion.PROCESANDO);

            // 6. UPSERT en base de datos
            int actualizadas = upsertVedas(vedas);
            log.info("💾 {} vedas actualizadas/insertadas en BD", actualizadas);

            // 7. Actualizar log a COMPLETADO
            registro.setEstado(EstadoIngestion.COMPLETADO);
            return ingestionLogRepository.save(registro);

        } catch (Exception e) {
            log.error("❌ Error procesando vedas: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en procesamiento de vedas", e);
        }
    }

    // ============================================================================
    // UPSERT DE ESPECIES
    // ============================================================================

    /**
     * Inserta o actualiza especies en la base de datos.
     *
     * ESTRATEGIA:
     * - Buscar por codigo_conapesca (natural key)
     * - Si existe → UPDATE
     * - Si no existe → INSERT
     *
     * @param especies Lista de DTOs parseados del CSV
     * @return Cantidad de registros afectados
     */
    @Transactional
    protected int upsertEspecies(List<EspecieCsvRow> especies) {
        int contador = 0;

        for (EspecieCsvRow dto : especies) {
            try {
                // Buscar si ya existe por código CONAPESCA
                Pez pez = pezRepository.findById(Long.valueOf(dto.id()))
                        .orElse(new Pez());

                // Mapear DTO → Entity
                pez.setId(Long.valueOf(dto.id()));
                pez.setNombreComun(dto.nombreComun());
                pez.setEspecie(dto.especieCientifica());
                pez.setNombreMaya(dto.nombreMaya());
                pez.setTallaMinima(dto.tallaMinima());
                pez.setHabitat(dto.habitat());
                pez.setTecnicaRecomendada(dto.tecnicaRecomendada());
                pez.setZona(dto.zona());

                // Save (Spring decide si es INSERT o UPDATE)
                pezRepository.save(pez);
                contador++;

            } catch (Exception e) {
                log.warn("⚠️  Error procesando especie ID {}: {}",
                        dto.id(), e.getMessage());
                // Continuar con siguiente (no rompe todo el proceso)
            }
        }

        return contador;
    }

    // ============================================================================
    // UPSERT DE VEDAS
    // ============================================================================

    /**
     * Inserta o actualiza vedas en la base de datos.
     *
     * ESTRATEGIA:
     * - Buscar Pez por codigo_conapesca (FK)
     * - Si Pez no existe → SKIP veda (log warning)
     * - Buscar veda por (pez_id, zona, tipo_veda) combinación única
     * - Si existe → UPDATE
     * - Si no existe → INSERT
     *
     * @param vedas Lista de DTOs parseados del CSV
     * @return Cantidad de registros afectados
     */
    @Transactional
    protected int upsertVedas(List<VedaCsvRow> vedas) {
        int contador = 0;

        for (VedaCsvRow dto : vedas) {
            try {
                // 1. Buscar Pez (FK requerida)
                Pez pez = pezRepository.findById(Long.valueOf(dto.pezId()))
                        .orElse(null);

                if (pez == null) {
                    log.warn("⚠️  Pez ID {} no encontrado, skipping veda en zona {}",
                            dto.pezId(), dto.zona());
                    continue;
                }

                // 2. Convertir tipo de veda (String → Enum)
                TipoVeda tipoVeda;
                try {
                    tipoVeda = TipoVeda.fromCsvValue(dto.tipoVeda());
                } catch (IllegalArgumentException e) {
                    log.warn("⚠️  Tipo de veda desconocido '{}' para Pez ID {}, skipping",
                            dto.tipoVeda(), dto.pezId());
                    continue;
                }

                // 3. Buscar si ya existe (por combinación única)
                EspecieVeda veda = especieVedaRepository
                        .findByPezAndZonaAndTipoVeda(pez, dto.zona(), tipoVeda)
                        .orElse(new EspecieVeda());

                // 4. Mapear DTO → Entity
                veda.setPez(pez);
                veda.setZona(dto.zona());
                veda.setTipoVeda(tipoVeda);
                veda.setInicioMes(dto.inicioMes());
                veda.setInicioDia(dto.inicioDia());
                veda.setFinMes(dto.finMes());
                veda.setFinDia(dto.finDia());
                veda.setFuenteDof(dto.fuenteDof());

                // Nota: inicioFijo y finFijo se parsean vacíos en CSV actual
                // Dejarlos null por ahora

                // 5. Save
                especieVedaRepository.save(veda);
                contador++;

            } catch (Exception e) {
                log.warn("⚠️  Error procesando veda para Pez ID {}: {}",
                        dto.pezId(), e.getMessage());
            }
        }

        return contador;
    }

    // ============================================================================
    // UTILIDADES
    // ============================================================================

    /**
     * Descarga contenido CSV desde una URL de Google Sheets.
     *
     * @param url URL del export CSV (con gid específico)
     * @return Bytes crudos del CSV
     * @throws Exception Si falla la descarga o timeout
     */
    private byte[] descargarCsv(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    String.format("Error descargando CSV: HTTP %d", response.statusCode())
            );
        }

        return response.body();
    }

    /**
     * Calcula hash SHA-256 de un array de bytes.
     *
     * PROPÓSITO:
     * - Detectar cambios en el CSV
     * - Evitar reprocesamiento (idempotencia)
     *
     * @param data Bytes del CSV
     * @return Hash hexadecimal (64 caracteres)
     */
    private String calcularSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);

            // Convertir bytes a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error calculando SHA-256", e);
        }
    }

    /**
     * Crea un registro de IngestionLog.
     *
     * @param nombreArchivo Nombre descriptivo del archivo procesado
     * @param hash SHA-256 del contenido
     * @param totalFilas Cantidad de filas procesadas
     * @param estado PROCESANDO, COMPLETADO o ERROR
     * @return Entidad guardada en BD
     */
    private IngestionLog crearIngestionLog(String nombreArchivo, String hash,
                                           int totalFilas, EstadoIngestion estado) {
        IngestionLog log = new IngestionLog();
        log.setNombreArchivo(nombreArchivo);
        log.setHashSha256(hash);
        log.setTotalFilas(totalFilas);
        log.setEstado(estado);
        log.setProcesadoEn(LocalDateTime.now());

        return ingestionLogRepository.save(log);
    }

    /**
     * Registra un error crítico en la tabla de logs.
     *
     * @param e Excepción capturada
     */
    private void registrarError(Exception e) {
        try {
            IngestionLog errorLog = new IngestionLog();
            errorLog.setNombreArchivo("ERROR");
            errorLog.setHashSha256("N/A");
            errorLog.setTotalFilas(0);
            errorLog.setEstado(EstadoIngestion.ERROR);
            errorLog.setProcesadoEn(LocalDateTime.now());
            // Nota: Agregar campo 'mensaje_error' en IngestionLog si necesitas más detalle

            ingestionLogRepository.save(errorLog);
        } catch (Exception loggingException) {
            log.error("No se pudo registrar el error en BD: {}",
                    loggingException.getMessage());
        }
    }

}