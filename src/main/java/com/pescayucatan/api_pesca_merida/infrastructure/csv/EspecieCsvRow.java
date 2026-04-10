package com.pescayucatan.api_pesca_merida.infrastructure.csv;

public record EspecieCsvRow(
        Integer id,
        String nombreComun,
        String nombreCientifico,
        String nombreMaya,
        String descripcion,
        String riesgoCiguatera,
        Boolean esInvasiva,
        Boolean esProtegida,
        String tipoAgua,
        Boolean migratorio,
        String imagenUrl
) {
    public static EspecieCsvRow fromCsvLine(String[] cells) {
        return new EspecieCsvRow(
                Integer.parseInt(cells[0]),
                cells[1],
                cells[2],
                cells[3],
                cells[4],
                cells[5],
                parseBoolOrNull(cells[6]),
                parseBoolOrNull(cells[7]),
                cells[8],
                parseBoolOrNull(cells[9]),
                cells.length > 10 ? cells[10].trim() : null
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
