package com.pescayucatan.api_pesca_merida.infrastructure.csv;
// VedaCsvRow.java
public record VedaCsvRow(
        Integer pezId,
        String nombreComun,
        String especieCientifica,
        String zona,
        String tipoVeda,
        Integer inicioMes,
        Integer inicioDia,
        Integer finMes,
        Integer finDia,
        String inicioFijo,
        String finFijo,
        String fuenteDof
) {
    public static VedaCsvRow fromCsvLine(String[] cells) {
        return new VedaCsvRow(
                Integer.parseInt(cells[0]),
                cells[1], cells[2], cells[3], cells[4],
                parseIntOrNull(cells[5]),
                parseIntOrNull(cells[6]),
                parseIntOrNull(cells[7]),
                parseIntOrNull(cells[8]),
                cells[9], cells[10], cells[11]
        );
    }

    private static Integer parseIntOrNull(String s) {
        return (s == null || s.isBlank()) ? null : Integer.parseInt(s);
    }
}

