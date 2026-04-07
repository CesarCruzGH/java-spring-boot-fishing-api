package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.infrastructure.csv.ArtePescaCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.EspecieCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.ZonaCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.RegulacionCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.PeriodoVedaCsvRow;
import com.pescayucatan.api_pesca_merida.model.ArtePesca;
import com.pescayucatan.api_pesca_merida.model.IngestionLog;
import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.model.Zona;
import com.pescayucatan.api_pesca_merida.model.Regulacion;
import com.pescayucatan.api_pesca_merida.model.PeriodoVeda;
import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.enums.EstadoIngestion;
import com.pescayucatan.api_pesca_merida.enums.CategoriaHidrica;
import com.pescayucatan.api_pesca_merida.enums.MacroZona;
import com.pescayucatan.api_pesca_merida.enums.TipoRestriccion;
import com.pescayucatan.api_pesca_merida.enums.CategoriaPesca;
import com.pescayucatan.api_pesca_merida.enums.TipoMedicion;
import com.pescayucatan.api_pesca_merida.repository.ArtePescaRepository;
import com.pescayucatan.api_pesca_merida.repository.IngestionLogRepository;
import com.pescayucatan.api_pesca_merida.repository.PezRepository;
import com.pescayucatan.api_pesca_merida.repository.ZonaRepository;
import com.pescayucatan.api_pesca_merida.repository.RegulacionRepository;
import com.pescayucatan.api_pesca_merida.repository.PeriodoVedaRepository;
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
import java.time.LocalDate;
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
    private final IngestionLogRepository ingestionLogRepository;
    private final ZonaRepository zonaRepository;
    private final RegulacionRepository regulacionRepository;
    private final PeriodoVedaRepository periodoVedaRepository;
    private final ArtePescaRepository artePescaRepository;

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

    @Value("${ingestion.sheets.zonas-url}")
    private String zonasUrl;

    @Value("${ingestion.sheets.regulaciones-url}")
    private String regulacionesUrl;

    @Value("${ingestion.sheets.periodo-vedas-url}")
    private String periodoVedasUrl;

    @Value("${ingestion.sheets.arte-pesca-url}")
    private String artePescaUrl;

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
    //@Scheduled(cron = "${ingestion.cron}")
    @Scheduled(initialDelay = 10000, fixedDelay = 180000)
    public void ejecutarIngestion() {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║  🔄 INICIANDO PROCESO DE INGESTIÓN CONAPESCA                  ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");

        LocalDateTime inicio = LocalDateTime.now();

        try {
            // Fase 1: Ingerir especies
            log.info("📥 Fase 1/5: Procesando CSV de Especies...");
            IngestionLog especiesLog = procesarEspecies();

            // Fase 2: Ingerir zonas
            log.info("📥 Fase 2/5: Procesando CSV de Zonas...");
            IngestionLog zonasLog = procesarZonas();

            // Fase 3: Ingerir regulaciones
            log.info("📥 Fase 3/5: Procesando CSV de Regulaciones...");
            IngestionLog regulacionesLog = procesarRegulaciones();

            // Fase 4: Ingerir periodo vedas
            log.info("📥 Fase 4/5: Procesando CSV de Periodo Vedas...");
            IngestionLog periodoVedasLog = procesarPeriodoVedas();

            // Fase 5: Ingerir artes pesca
            log.info("📥 Fase 5/5: Procesando CSV de Artes Pesca...");
            IngestionLog artePescaLog = procesarArtePesca();

            // Resumen final
            LocalDateTime fin = LocalDateTime.now();
            Duration duracion = Duration.between(inicio, fin);

            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║  ✅ INGESTIÓN COMPLETADA EXITOSAMENTE                         ║");
            log.info("╠════════════════════════════════════════════════════════════════╣");
            log.info("║  Especies: {} filas procesadas                                ",
                    especiesLog != null ? especiesLog.getTotalFilas() : 0);
            log.info("║  Zonas:    {} filas procesadas                                ",
                    zonasLog != null ? zonasLog.getTotalFilas() : 0);
            log.info("║  Regulaciones: {} filas procesadas                          ",
                    regulacionesLog != null ? regulacionesLog.getTotalFilas() : 0);
            log.info("║  Periodo Vedas: {} filas procesadas                        ",
                    periodoVedasLog != null ? periodoVedasLog.getTotalFilas() : 0);
            log.info("║  Artes Pesca: {} filas procesadas                           ",
                    artePescaLog != null ? artePescaLog.getTotalFilas() : 0);
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
    // PROCESAMIENTO DE ZONAS
    // ============================================================================

    /**
     * Descarga, valida y procesa el CSV de zonas.
     *
     * @return IngestionLog con resultado del procesamiento, o null si se saltó
     */
    private IngestionLog procesarZonas() {
        try {
            log.debug("Descargando CSV de zonas desde: {}", zonasUrl);
            byte[] csvBytes = descargarCsv(zonasUrl);

            String hash = calcularSha256(csvBytes);
            log.debug("Hash SHA-256 calculado: {}", hash);

            if (ingestionLogRepository.existsByHashSha256(hash)) {
                log.info("⏭️  CSV de zonas sin cambios (hash: {}...), SKIP",
                        hash.substring(0, 8));
                return null;
            }

            List<ZonaCsvRow> zonas = csvParser.parseZonas(csvBytes);
            log.info("✅ Parseadas {} zonas", zonas.size());

            IngestionLog registro = crearIngestionLog("zonas.csv", hash,
                    zonas.size(), EstadoIngestion.PROCESANDO);

            int actualizadas = upsertZonas(zonas);
            log.info("💾 {} zonas actualizadas/insertadas en BD", actualizadas);

            registro.setEstado(EstadoIngestion.COMPLETADO);
            return ingestionLogRepository.save(registro);

        } catch (Exception e) {
            log.error("❌ Error procesando zonas: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en procesamiento de zonas", e);
        }
    }

    // ============================================================================
    // PROCESAMIENTO DE REGULACIONES
    // ============================================================================

    /**
     * Descarga, valida y procesa el CSV de regulaciones.
     *
     * @return IngestionLog con resultado del procesamiento, o null si se saltó
     */
    private IngestionLog procesarRegulaciones() {
        try {
            log.debug("Descargando CSV de regulaciones desde: {}", regulacionesUrl);
            byte[] csvBytes = descargarCsv(regulacionesUrl);

            String hash = calcularSha256(csvBytes);
            log.debug("Hash SHA-256 calculado: {}", hash);

            if (ingestionLogRepository.existsByHashSha256(hash)) {
                log.info("⏭️  CSV de regulaciones sin cambios (hash: {}...), SKIP",
                        hash.substring(0, 8));
                return null;
            }

            List<RegulacionCsvRow> regulaciones = csvParser.parseRegulaciones(csvBytes);
            log.info("✅ Parseadas {} regulaciones", regulaciones.size());

            IngestionLog registro = crearIngestionLog("regulaciones.csv", hash,
                    regulaciones.size(), EstadoIngestion.PROCESANDO);

            int actualizadas = upsertRegulaciones(regulaciones);
            log.info("💾 {} regulaciones actualizadas/insertadas en BD", actualizadas);

            registro.setEstado(EstadoIngestion.COMPLETADO);
            return ingestionLogRepository.save(registro);

        } catch (Exception e) {
            log.error("❌ Error procesando regulaciones: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en procesamiento de regulaciones", e);
        }
    }

    // ============================================================================
    // PROCESAMIENTO DE PERIODO VEDAS
    // ============================================================================

    /**
     * Descarga, valida y procesa el CSV de periodo vedas.
     *
     * @return IngestionLog con resultado del procesamiento, o null si se saltó
     */
    private IngestionLog procesarPeriodoVedas() {
        try {
            log.debug("Descargando CSV de periodo vedas desde: {}", periodoVedasUrl);
            byte[] csvBytes = descargarCsv(periodoVedasUrl);

            String hash = calcularSha256(csvBytes);
            log.debug("Hash SHA-256 calculado: {}", hash);

            if (ingestionLogRepository.existsByHashSha256(hash)) {
                log.info("⏭️  CSV de periodo vedas sin cambios (hash: {}...), SKIP",
                        hash.substring(0, 8));
                return null;
            }

            List<PeriodoVedaCsvRow> periodoVedas = csvParser.parsePeriodoVedas(csvBytes);
            log.info("✅ Parseados {} periodo vedas", periodoVedas.size());

            IngestionLog registro = crearIngestionLog("periodo_vedas.csv", hash,
                    periodoVedas.size(), EstadoIngestion.PROCESANDO);

            int actualizadas = upsertPeriodoVedas(periodoVedas);
            log.info("💾 {} periodo vedas actualizadas/insertadas en BD", actualizadas);

            registro.setEstado(EstadoIngestion.COMPLETADO);
            return ingestionLogRepository.save(registro);

        } catch (Exception e) {
            log.error("❌ Error procesando periodo vedas: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en procesamiento de periodo vedas", e);
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
                Pez pez = pezRepository.findById(Long.valueOf(dto.id()))
                        .orElse(new Pez());

                pez.setId(Long.valueOf(dto.id()));
                pez.setNombreComun(dto.nombreComun());
                pez.setNombreCientifico(dto.nombreCientifico());
                pez.setNombreMaya(dto.nombreMaya());
                pez.setDescripcion(dto.descripcion());
                if (dto.riesgoCiguatera() != null && !dto.riesgoCiguatera().isBlank()) {
                    pez.setRiesgoCiguatera(com.pescayucatan.api_pesca_merida.enums.RiesgoCiguatera.valueOf(dto.riesgoCiguatera().toUpperCase().trim()));
                }
                pez.setEsInvasiva(dto.esInvasiva());
                pez.setEsProtegida(dto.esProtegida());
                if (dto.tipoAgua() != null && !dto.tipoAgua().isBlank()) {
                    pez.setTipoAgua(com.pescayucatan.api_pesca_merida.enums.TipoAgua.valueOf(dto.tipoAgua().toUpperCase().trim()));
                }
                pez.setMigratorio(dto.migratorio());

                pezRepository.save(pez);
                contador++;

            } catch (Exception e) {
                log.warn("⚠️  Error procesando especie ID {}: {}",
                        dto.id(), e.getMessage());
            }
        }

        return contador;
    }

    // ============================================================================
    // UPSERT DE ZONAS
    // ============================================================================

    /**
     * Inserta o actualiza zonas en la base de datos.
     *
     * @param zonas Lista de DTOs parseados del CSV
     * @return Cantidad de registros afectados
     */
    @Transactional
    protected int upsertZonas(List<ZonaCsvRow> zonas) {
        int contador = 0;

        for (ZonaCsvRow dto : zonas) {
            try {
                Zona zona = zonaRepository.findById(dto.id())
                        .orElse(new Zona());

                zona.setId(dto.id());
                zona.setNombre(dto.nombre());
                zona.setMacroZona(MacroZona.valueOf(dto.macroZona().toUpperCase().trim()));
                zona.setTipoRestriccion(TipoRestriccion.valueOf(dto.tipoRestriccion().toUpperCase().trim()));
                zona.setCategoriaHidrica(CategoriaHidrica.valueOf(dto.categoriaHidrica().toUpperCase().trim()));
                zona.setEsAnp(dto.esAnp());
                zona.setMunicipioSede(dto.municipioSede());
                zona.setNotasEspecificas(dto.notasEspecificas());

                zonaRepository.save(zona);
                contador++;

            } catch (Exception e) {
                log.warn("⚠️  Error procesando zona ID {}: {}",
                        dto.id(), e.getMessage());
            }
        }

        return contador;
    }

    // ============================================================================
    // UPSERT DE REGULACIONES
    // ============================================================================

    /**
     * Inserta o actualiza regulaciones en la base de datos.
     *
     * @param regulaciones Lista de DTOs parseados del CSV
     * @return Cantidad de registros afectados
     */
    @Transactional
    protected int upsertRegulaciones(List<RegulacionCsvRow> regulaciones) {
        int contador = 0;

        for (RegulacionCsvRow dto : regulaciones) {
            try {
                Pez pez = pezRepository.findById(dto.pezId())
                        .orElse(null);
                if (pez == null) {
                    log.warn("⚠️  Pez ID {} no encontrado para regulación, skipping",
                            dto.pezId());
                    continue;
                }

                Zona zona = zonaRepository.findById(dto.zonaId())
                        .orElse(null);
                if (zona == null) {
                    log.warn("⚠️  Zona ID {} no encontrada para regulación, skipping",
                            dto.zonaId());
                    continue;
                }

                Regulacion regulacion = regulacionRepository.findById(dto.id())
                        .orElse(new Regulacion());

                regulacion.setId(dto.id());
                regulacion.setPez(pez);
                regulacion.setZona(zona);
                regulacion.setCategoriaPesca(CategoriaPesca.valueOf(dto.categoriaPesca().toUpperCase().trim()));
                regulacion.setTallaMinima(dto.tallaMinima());
                regulacion.setTallaMaxima(dto.tallaMaxima());
                if (dto.tipoMedicion() != null && !dto.tipoMedicion().isBlank()) {
                    regulacion.setTipoMedicion(TipoMedicion.valueOf(dto.tipoMedicion().toUpperCase().trim()));
                }
                regulacion.setCuotaDiaria(dto.cuotaDiaria());
                regulacion.setRequierePermiso(dto.requierePermiso());

                regulacionRepository.save(regulacion);
                contador++;

            } catch (Exception e) {
                log.warn("⚠️  Error procesando regulación ID {}: {}",
                        dto.id(), e.getMessage());
            }
        }

        return contador;
    }

    // ============================================================================
    // UPSERT DE PERIODO VEDAS
    // ============================================================================

    /**
     * Inserta o actualiza periodos de veda en la base de datos.
     *
     * @param periodoVedas Lista de DTOs parseados del CSV
     * @return Cantidad de registros afectados
     */
    @Transactional
    protected int upsertPeriodoVedas(List<PeriodoVedaCsvRow> periodoVedas) {
        int contador = 0;

        for (PeriodoVedaCsvRow dto : periodoVedas) {
            try {
                Regulacion regulacion = regulacionRepository.findById(dto.regulacionId())
                        .orElse(null);
                if (regulacion == null) {
                    log.warn("⚠️  Regulación ID {} no encontrada para periodo veda, skipping",
                            dto.regulacionId());
                    continue;
                }

                PeriodoVeda periodoVeda = periodoVedaRepository.findById(dto.id())
                        .orElse(new PeriodoVeda());

                periodoVeda.setId(dto.id());
                periodoVeda.setRegulacion(regulacion);
                periodoVeda.setTipoVeda(TipoVeda.valueOf(dto.tipoVeda().toUpperCase().trim()));
                periodoVeda.setMesInicio(dto.mesInicio());
                periodoVeda.setDiaInicio(dto.diaInicio());
                periodoVeda.setMesFin(dto.mesFin());
                periodoVeda.setDiaFin(dto.diaFin());
                periodoVeda.setFuenteDof(dto.fuenteDof());

                periodoVedaRepository.save(periodoVeda);
                contador++;

            } catch (Exception e) {
                log.warn("⚠️  Error procesando periodo veda ID {}: {}",
                        dto.id(), e.getMessage());
            }
        }

        return contador;
    }

    // ============================================================================
    // PROCESAMIENTO DE ARTE PESCA
    // ============================================================================

    private IngestionLog procesarArtePesca() {
        try {
            log.debug("Descargando CSV de artes pesca desde: {}", artePescaUrl);
            byte[] csvBytes = descargarCsv(artePescaUrl);

            String hash = calcularSha256(csvBytes);
            log.debug("Hash SHA-256 calculado: {}", hash);

            if (ingestionLogRepository.existsByHashSha256(hash)) {
                log.info("⏭️  CSV de artes pesca sin cambios (hash: {}...), SKIP",
                        hash.substring(0, 8));
                return null;
            }

            List<ArtePescaCsvRow> artesPesca = csvParser.parseArtePesca(csvBytes);
            log.info("✅ Parseados {} artes pesca", artesPesca.size());

            IngestionLog registro = crearIngestionLog("arte_pesca.csv", hash,
                    artesPesca.size(), EstadoIngestion.PROCESANDO);

            int actualizadas = upsertArtePesca(artesPesca);
            log.info("💾 {} artes pesca actualizadas/insertadas en BD", actualizadas);

            registro.setEstado(EstadoIngestion.COMPLETADO);
            return ingestionLogRepository.save(registro);

        } catch (Exception e) {
            log.error("❌ Error procesando artes pesca: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en procesamiento de artes pesca", e);
        }
    }

    @Transactional
    protected int upsertArtePesca(List<ArtePescaCsvRow> artesPesca) {
        int contador = 0;

        for (ArtePescaCsvRow dto : artesPesca) {
            try {
                Regulacion regulacion = regulacionRepository.findById(dto.regulacionId())
                        .orElse(null);
                if (regulacion == null) {
                    log.warn("⚠️  Regulación ID {} no encontrada para arte pesca, skipping",
                            dto.regulacionId());
                    continue;
                }

                ArtePesca artePesca = artePescaRepository.findById(dto.id())
                        .orElse(new ArtePesca());

                artePesca.setId(dto.id());
                artePesca.setRegulacion(regulacion);
                artePesca.setNombre(dto.nombre());
                artePesca.setEsProhibido(dto.esProhibido());

                artePescaRepository.save(artePesca);
                contador++;

            } catch (Exception e) {
                log.warn("⚠️  Error procesando arte pesca ID {}: {}",
                        dto.id(), e.getMessage());
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