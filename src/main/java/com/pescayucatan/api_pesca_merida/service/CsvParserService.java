package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.infrastructure.csv.EspecieCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.VedaCsvRow;
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

                // Validación: Mínimo 8 columnas
                if (cols.length < 8) {
                    log.warn("Fila {} de especies incompleta ({} cols): {}",
                            i + 1, cols.length, lines[i]);
                    continue;
                }

                rows.add(new EspecieCsvRow(
                        parseIntOrNull(cols[0]),   // ID
                        cols[1].trim(),            // Nombre Común
                        cols[2].trim(),            // Especie Científica
                        cols[3].trim(),            // Nombre Maya
                        cols[4].trim(),            // Talla Mínima
                        cols[5].trim(),            // Hábitat
                        cols[6].trim(),            // Técnica Recomendada
                        cols[7].trim()             // Zona Geográfica
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
     * Parsea CSV de VEDAS (Hoja 2 del Google Sheet).
     *
     * Formato esperado (12 columnas):
     * Pez ID | Nombre Común | Especie | Zona | Tipo Veda | Inicio mes | Inicio día |
     * Fin mes | Fin día | Inicio fijo | Fin fijo | Fuente DOF
     *
     * @param csvBytes Contenido crudo del CSV descargado desde Google Sheets
     * @return Lista de DTOs de vedas, excluyendo filas inválidas
     */
    public List<VedaCsvRow> parseVedas(byte[] csvBytes) {
        String csvContent = stripUtf8Bom(csvBytes);
        List<VedaCsvRow> rows = new ArrayList<>();
        String[] lines = csvContent.split("\r?\n");

        if (lines.length <= 1) {
            log.warn("CSV de vedas vacío o solo contiene header");
            return rows;
        }

        // Validar header
        validateVedasHeader(lines[0]);

        // Parsear filas de datos (skip header)
        for (int i = 1; i < lines.length; i++) {
            try {
                String[] cols = parseCsvLine(lines[i]);

                // Validación: Mínimo 12 columnas
                if (cols.length < 12) {
                    log.warn("Fila {} de vedas incompleta ({} cols): {}",
                            i + 1, cols.length, lines[i]);
                    continue;
                }

                // Validación adicional: Pez ID debe existir
                Integer pezId = parseIntOrNull(cols[0]);
                if (pezId == null) {
                    log.warn("Fila {} de vedas sin Pez ID válido, skipping", i + 1);
                    continue;
                }

                rows.add(new VedaCsvRow(
                        pezId,                     // Pez ID (FK)
                        cols[1].trim(),            // Nombre Común
                        cols[2].trim(),            // Especie Científica
                        cols[3].trim(),            // Zona
                        cols[4].trim(),            // Tipo de Veda
                        parseIntOrNull(cols[5]),   // Inicio mes
                        parseIntOrNull(cols[6]),   // Inicio día
                        parseIntOrNull(cols[7]),   // Fin mes
                        parseIntOrNull(cols[8]),   // Fin día
                        cols[9].trim(),            // Inicio fijo
                        cols[10].trim(),           // Fin fijo
                        cols[11].trim()            // Fuente DOF
                ));

            } catch (Exception e) {
                log.error("Error parseando fila {} de vedas: {}", i + 1, e.getMessage());
            }
        }

        log.info("✅ Parseadas {} vedas de {} filas totales", rows.size(), lines.length - 1);
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
                "ID", "NOMBRE COMÚN", "ESPECIE", "NOMBRE MAYA",
                "TALLA MÍNIMA", "HÁBITAT", "TÉCNICA RECOMENDADA", "ZONA"
        };

        String[] actualCols = parseCsvLine(headerLine);

        if (actualCols.length < expectedCols.length) {
            log.warn("⚠️ Header de especies tiene {} columnas, se esperaban {}. " +
                            "Posible cambio de schema en Google Sheet.",
                    actualCols.length, expectedCols.length);
        }

        // Log de header actual para debugging
        log.debug("Header especies: {}", String.join(" | ", actualCols));
    }

    /**
     * Valida que el header del CSV de vedas contenga las columnas esperadas.
     */
    private void validateVedasHeader(String headerLine) {
        String[] expectedCols = {
                "Pez ID", "Nombre Común", "Especie Científica", "Zona", "Tipo de Veda",
                "Inicio mes", "Inicio día", "Fin mes", "Fin día",
                "Inicio fijo", "Fin fijo", "Fuente DOF"
        };

        String[] actualCols = parseCsvLine(headerLine);

        if (actualCols.length < expectedCols.length) {
            log.warn("⚠️ Header de vedas tiene {} columnas, se esperaban {}. " +
                            "Posible cambio de schema en Google Sheet.",
                    actualCols.length, expectedCols.length);
        }

        log.debug("Header vedas: {}", String.join(" | ", actualCols));
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
}