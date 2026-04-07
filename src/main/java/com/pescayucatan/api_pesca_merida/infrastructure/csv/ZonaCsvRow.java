package com.pescayucatan.api_pesca_merida.infrastructure.csv;

public record ZonaCsvRow(
        Long id,
        String nombre,
        String macroZona,
        String tipoRestriccion,
        String categoriaHidrica,
        Boolean esAnp,
        String municipioSede,
        String notasEspecificas
) {
    public static ZonaCsvRow fromCsvLine(String[] cells) {
        return new ZonaCsvRow(
                Long.parseLong(cells[0]),
                cells[1],
                cells[2],
                cells[3],
                cells[4],
                parseBoolOrNull(cells[5]),
                cells[6],
                cells[7]
        );
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
