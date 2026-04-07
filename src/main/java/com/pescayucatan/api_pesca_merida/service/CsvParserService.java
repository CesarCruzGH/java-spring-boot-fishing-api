package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.infrastructure.csv.ArtePescaCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.EspecieCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.ZonaCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.RegulacionCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.PeriodoVedaCsvRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para parsear archivos CSV provenientes de Google Sheets.
 * Soporta dos formatos:
 * - Especies: 8 columnas (ID, Nombre Común, Especie, etc.)
 * - Vedas: 12 columnas (Pez ID, Zona, Tipo de Veda, etc.)
 *
 * CARACTERÍSTICAS:
 * - Zero dependencias externas (usa java.io nativo)
 * - Manejo de UTF-8 BOM (Byte Order Mark)
 * - Parser RFC 4180 compliant (comillas, escapes)
 * - Validación de filas incompletas
 * - Logging de errores detallado
 *
 * @author Sistema de Ingesta CONAPESCA
 * @since 2026-03-17
 */
@Service
@Slf4j
public class CsvParserService {

    /**
     * Parsea CSV de ESPECIES (Hoja 1 del Google Sheet).
     *
     * Formato esperado (8 columnas):
     * ID | NOMBRE COMÚN | ESPECIE | NOMBRE MAYA | TALLA MÍNIMA | HÁBITAT | TÉCNICA | ZONA
     *
     * @param csvBytes Contenido crudo del CSV descargado desde Google Sheets
     * @return Lista de DTOs de especies, excluyendo filas inválidas
     * @throws IllegalArgumentException Si el CSV está completamente vacío o corrupto
     */
    public List<EspecieCsvRow> parsePeces(byte[] csvBytes) {
        String csvContent = stripUtf8Bom(csvBytes);
        List<EspecieCsvRow> rows = new ArrayList<>();
        String[] lines = csvContent.split("\r?\n");

        if (lines.length <= 1) {
            log.warn("CSV de peces vacío o solo contiene header");
            return rows;
        }

        // Validar header (primera línea)
        validateEspeciesHeader(lines[0]);

        // Parsear filas de datos (skip header)
        for (int i = 1; i < lines.length; i++) {
            try {
                String[] cols = parseCsvLine(lines[i]);

                // Validación: Mínimo 10 columnas
                if (cols.length < 10) {
                    log.warn("Fila {} de especies incompleta ({} cols): {}",
                            i + 1, cols.length, lines[i]);
                    continue;
                }

                rows.add(new EspecieCsvRow(
                        parseIntOrNull(cols[0]),   // ID
                        cols[1].trim(),            // Nombre Común
                        cols[2].trim(),            // Especie Científica
                        cols[3].trim(),            // Nombre Maya
                        cols[4].trim(),            // Descripcion
                        cols[5].trim(),            // Ciguatera
                        Boolean.parseBoolean(cols[6]),  //Invasiva
                        Boolean.parseBoolean(cols[7]),  //Protegida
                        cols[8].trim(),             //Tipo Agua
                        Boolean.parseBoolean(cols[9])   //Migratorio
                ));

            } catch (Exception e) {
                log.error("Error parseando fila {} de especies: {}", i + 1, e.getMessage());
                // Continúa con siguiente fila (no falla todo el proceso)
            }
        }

        log.info("✅ Parseadas {} especies de {} filas totales", rows.size(), lines.length - 1);
        return rows;
    }

    /**
     * Parsea CSV de ZONAS (del documento guía).
     *
     * Formato esperado (8 columnas):
     * ID | Nombre | Macro Zona | Tipo Restricción | Categoría Hídrica | Es ANP | Municipio Sede | Notas Específicas
     *
     * @param csvBytes Contenido crudo del CSV
     * @return Lista de DTOs de zonas, excluyendo filas inválidas
     */
    public List<ZonaCsvRow> parseZonas(byte[] csvBytes) {
        String csvContent = stripUtf8Bom(csvBytes);
        List<ZonaCsvRow> rows = new ArrayList<>();
        String[] lines = csvContent.split("\r?\n");

        if (lines.length <= 1) {
            log.warn("CSV de zonas vacío o solo contiene header");
            return rows;
        }

        validateZonasHeader(lines[0]);

        for (int i = 1; i < lines.length; i++) {
            try {
                String[] cols = parseCsvLine(lines[i]);

                if (cols.length < 8) {
                    log.warn("Fila {} de zonas incompleta ({} cols): {}",
                            i + 1, cols.length, lines[i]);
                    continue;
                }

                Long zonaId = parseLongOrNull(cols[0]);
                if (zonaId == null) {
                    log.warn("Fila {} de zonas sin ID válido, skipping", i + 1);
                    continue;
                }

                rows.add(new ZonaCsvRow(
                        zonaId,
                        cols[1].trim(),
                        cols[2].trim(),
                        cols[3].trim(),
                        cols[4].trim(),
                        parseBoolOrNull(cols[5]),
                        cols[6].trim(),
                        cols[7].trim()
                ));

            } catch (Exception e) {
                log.error("Error parseando fila {} de zonas: {}", i + 1, e.getMessage());
            }
        }

        log.info("✅ Parseadas {} zonas de {} filas totales", rows.size(), lines.length - 1);
        return rows;
    }

    /**
     * Parsea CSV de REGULACIONES.
     *
     * Formato esperado (9 columnas):
     * ID | Pez ID | Zona ID | Categoría Pesca | Talla Mínima | Talla Máxima | Tipo Medición | Cuota Diaria | Requiere Permiso
     *
     * @param csvBytes Contenido crudo del CSV
     * @return Lista de DTOs de regulaciones, excluyendo filas inválidas
     */
    public List<RegulacionCsvRow> parseRegulaciones(byte[] csvBytes) {
        String csvContent = stripUtf8Bom(csvBytes);
        List<RegulacionCsvRow> rows = new ArrayList<>();
        String[] lines = csvContent.split("\r?\n");

        if (lines.length <= 1) {
            log.warn("CSV de regulaciones vacío o solo contiene header");
            return rows;
        }

        validateRegulacionesHeader(lines[0]);

        for (int i = 1; i < lines.length; i++) {
            try {
                String[] cols = parseCsvLine(lines[i]);

                if (cols.length < 9) {
                    log.warn("Fila {} de regulaciones incompleta ({} cols): {}",
                            i + 1, cols.length, lines[i]);
                    continue;
                }

                Long regulacionId = parseLongOrNull(cols[0]);
                if (regulacionId == null) {
                    log.warn("Fila {} de regulaciones sin ID válido, skipping", i + 1);
                    continue;
                }

                rows.add(RegulacionCsvRow.fromCsvLine(cols));

            } catch (Exception e) {
                log.error("Error parseando fila {} de regulaciones: {}", i + 1, e.getMessage());
            }
        }

        log.info("✅ Parseadas {} regulaciones de {} filas totales", rows.size(), lines.length - 1);
        return rows;
    }

    /**
     * Parsea CSV de PERIODOS DE VEDA.
     *
     * Formato esperado (8 columnas):
     * ID | Regulacion ID | Tipo Veda | Mes Inicio | Día Inicio | Mes Fin | Día Fin | Fuente DOF
     *
     * @param csvBytes Contenido crudo del CSV
     * @return Lista de DTOs de periodo veda, excluyendo filas inválidas
     */
    public List<PeriodoVedaCsvRow> parsePeriodoVedas(byte[] csvBytes) {
        String csvContent = stripUtf8Bom(csvBytes);
        List<PeriodoVedaCsvRow> rows = new ArrayList<>();
        String[] lines = csvContent.split("\r?\n");

        if (lines.length <= 1) {
            log.warn("CSV de periodo veda vacío o solo contiene header");
            return rows;
        }

        validatePeriodoVedasHeader(lines[0]);

        for (int i = 1; i < lines.length; i++) {
            try {
                String[] cols = parseCsvLine(lines[i]);

                if (cols.length < 8) {
                    log.warn("Fila {} de periodo veda incompleta ({} cols): {}",
                            i + 1, cols.length, lines[i]);
                    continue;
                }

                Long periodoId = parseLongOrNull(cols[0]);
                if (periodoId == null) {
                    log.warn("Fila {} de periodo veda sin ID válido, skipping", i + 1);
                    continue;
                }

                rows.add(PeriodoVedaCsvRow.fromCsvLine(cols));

            } catch (Exception e) {
                log.error("Error parseando fila {} de periodo veda: {}", i + 1, e.getMessage());
            }
        }

        log.info("✅ Parseados {} periodos veda de {} filas totales", rows.size(), lines.length - 1);
        return rows;
    }

    public List<ArtePescaCsvRow> parseArtePesca(byte[] csvBytes) {
        String csvContent = stripUtf8Bom(csvBytes);
        List<ArtePescaCsvRow> rows = new ArrayList<>();
        String[] lines = csvContent.split("\r?\n");

        if (lines.length <= 1) {
            log.warn("CSV de arte pesca vacío o solo contiene header");
            return rows;
        }

        validateArtePescaHeader(lines[0]);

        for (int i = 1; i < lines.length; i++) {
            try {
                String[] cols = parseCsvLine(lines[i]);

                if (cols.length < 4) {
                    log.warn("Fila {} de arte pesca incompleta ({} cols): {}",
                            i + 1, cols.length, lines[i]);
                    continue;
                }

                Long artePescaId = parseLongOrNull(cols[0]);
                if (artePescaId == null) {
                    log.warn("Fila {} de arte pesca sin ID válido, skipping", i + 1);
                    continue;
                }

                rows.add(ArtePescaCsvRow.fromCsvLine(cols));

            } catch (Exception e) {
                log.error("Error parseando fila {} de arte pesca: {}", i + 1, e.getMessage());
            }
        }

        log.info("✅ Parseados {} artes pesca de {} filas totales", rows.size(), lines.length - 1);
        return rows;
    }

    /**
     * Remueve UTF-8 BOM (Byte Order Mark) si existe.
     * Google Sheets a veces exporta CSVs con BOM (bytes EF BB BF).
     *
     * @param csvBytes Bytes crudos del CSV
     * @return String UTF-8 sin BOM
     */
    private String stripUtf8Bom(byte[] csvBytes) {
        String content = new String(csvBytes, StandardCharsets.UTF_8);

        // BOM UTF-8 = U+FEFF
        if (content.startsWith("\uFEFF")) {
            log.debug("Detectado UTF-8 BOM, removiendo...");
            return content.substring(1);
        }

        return content;
    }

    /**
     * Valida que el header del CSV de especies contenga las columnas esperadas.
     * No falla el proceso, solo registra un warning si difiere.
     */
    private void validateEspeciesHeader(String headerLine) {
        String[] expectedCols = {
                "ID", "NOMBRE COMÚN", "NOMBRE CIENTÍFICO", "NOMBRE MAYA",
                "DESCRIPCIÓN", "RIESGO CIGUATERA", "ES INVASIVA", "ES PROTEGIDA",
                "TIPO AGUA", "MIGRATORIO"
        };

        String[] actualCols = parseCsvLine(headerLine);

        if (actualCols.length < expectedCols.length) {
            log.warn("⚠️ Header de especies tiene {} columnas, se esperaban {}. " +
                            "Posible cambio de schema en Google Sheet.",
                    actualCols.length, expectedCols.length);
        }

        log.debug("Header especies: {}", String.join(" | ", actualCols));
    }

    /**
     * Parsea una línea CSV siguiendo RFC 4180.
     * Maneja correctamente:
     * - Campos entre comillas: "Golfo de México, Zona Norte"
     * - Comillas escapadas: "Técnica ""avanzada"""
     * - Campos vacíos: ,,valor,,
     *
     * @param line Línea completa del CSV
     * @return Array de valores (strings), nunca null
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check for escaped quote ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes; // Toggle quote mode
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                fields.add(currentField.toString());
                currentField.setLength(0); // Reset
            } else {
                currentField.append(c);
            }
        }

        // Add last field
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    /**
     * Parsea un string a Integer, retornando null si está vacío o no es numérico.
     * Usado para campos opcionales como fechas en vedas PERMANENTES.
     *
     * @param val String a parsear (puede ser null, vacío, o texto)
     * @return Integer parseado o null si inválido
     */
    private Integer parseIntOrNull(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            log.debug("Valor no numérico encontrado: '{}', retornando null", val);
            return null;
        }
    }

    private Long parseLongOrNull(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            log.debug("Valor no numérico (Long) encontrado: '{}', retornando null", val);
            return null;
        }
    }

    private Boolean parseBoolOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        return switch (s.trim().toUpperCase()) {
            case "TRUE", "1", "SI", "S" -> true;
            case "FALSE", "0", "NO", "N" -> false;
            default -> null;
        };
    }

    private void validateZonasHeader(String headerLine) {
        String[] expectedCols = {
                "ID", "NOMBRE", "MACROO ZONA", "TIPO RESTRICCION",
                "CATEGORIA HIDRICA", "ES ANP", "MUNICIPIO SEDE", "NOTAS ESPECIFICAS"
        };

        String[] actualCols = parseCsvLine(headerLine);

        if (actualCols.length < expectedCols.length) {
            log.warn("⚠️ Header de zonas tiene {} columnas, se esperaban {}. " +
                            "Posible cambio de schema.",
                    actualCols.length, expectedCols.length);
        }

        log.debug("Header zonas: {}", String.join(" | ", actualCols));
    }

    private void validateRegulacionesHeader(String headerLine) {
        String[] expectedCols = {
                "ID", "Pez ID", "Zona ID", "Categoría Pesca",
                "Talla Mínima", "Talla Máxima", "Tipo Medición",
                "Cuota Diaria", "Requiere Permiso"
        };

        String[] actualCols = parseCsvLine(headerLine);

        if (actualCols.length < expectedCols.length) {
            log.warn("⚠️ Header de regulaciones tiene {} columnas, se esperaban {}. " +
                            "Posible cambio de schema.",
                    actualCols.length, expectedCols.length);
        }

        log.debug("Header regulaciones: {}", String.join(" | ", actualCols));
    }

    private void validatePeriodoVedasHeader(String headerLine) {
        String[] expectedCols = {
                "ID", "Regulación ID", "Tipo Veda", "Mes Inicio", "Día Inicio",
                "Mes Fin", "Día Fin", "Fuente DOF"
        };

        String[] actualCols = parseCsvLine(headerLine);

        if (actualCols.length < expectedCols.length) {
            log.warn("⚠️ Header de periodo veda tiene {} columnas, se esperaban {}. " +
                            "Posible cambio de schema.",
                    actualCols.length, expectedCols.length);
        }

        log.debug("Header periodo veda: {}", String.join(" | ", actualCols));
    }

    private void validateArtePescaHeader(String headerLine) {
        String[] expectedCols = {
                "ID", "Regulación ID", "Nombre", "Es Prohibido"
        };

        String[] actualCols = parseCsvLine(headerLine);

        if (actualCols.length < expectedCols.length) {
            log.warn("⚠️ Header de arte pesca tiene {} columnas, se esperaban {}. " +
                            "Posible cambio de schema.",
                    actualCols.length, expectedCols.length);
        }

        log.debug("Header arte pesca: {}", String.join(" | ", actualCols));
    }
}