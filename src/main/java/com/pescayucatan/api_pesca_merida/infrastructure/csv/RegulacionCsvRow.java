package com.pescayucatan.api_pesca_merida.infrastructure.csv;

import java.math.BigDecimal;

public record RegulacionCsvRow(
        Long id,
        Long pezId,
        Long zonaId,
        String categoriaPesca,
        BigDecimal tallaMinima,
        BigDecimal tallaMaxima,
        String tipoMedicion,
        Integer cuotaDiaria,
        Boolean requierePermiso
) {
    public static RegulacionCsvRow fromCsvLine(String[] cells) {
        return new RegulacionCsvRow(
                Long.parseLong(cells[0].trim()),
                Long.parseLong(cells[1].trim()),
                Long.parseLong(cells[2].trim()),
                cells[3].trim(),
                parseDecimalOrNull(cells[4]),
                parseDecimalOrNull(cells[5]),
                cells[6].trim(),
                parseIntOrNull(cells[7]),
                parseBoolOrNull(cells[8])
        );
    }

    private static BigDecimal parseDecimalOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean parseBoolOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        return switch (s.trim().toUpperCase()) {
            case "TRUE", "1", "SI", "S" -> true;
            case "FALSE", "0", "NO", "N" -> false;
            default -> null;
        };
    }
}
