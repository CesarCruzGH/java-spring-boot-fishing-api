package com.pescayucatan.api_pesca_merida.infrastructure.csv;

public record PeriodoVedaCsvRow(
        Long id,
        Long regulacionId,
        String tipoVeda,
        Integer mesInicio,
        Integer diaInicio,
        Integer mesFin,
        Integer diaFin,
        String fuenteDof
) {
    public static PeriodoVedaCsvRow fromCsvLine(String[] cells) {
        return new PeriodoVedaCsvRow(
                Long.parseLong(cells[0].trim()),
                Long.parseLong(cells[1].trim()),
                cells[2].trim(),
                parseIntOrNull(cells[3]),
                parseIntOrNull(cells[4]),
                parseIntOrNull(cells[5]),
                parseIntOrNull(cells[6]),
                cells[7].trim()
        );
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
