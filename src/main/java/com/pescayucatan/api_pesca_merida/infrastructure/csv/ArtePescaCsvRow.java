package com.pescayucatan.api_pesca_merida.infrastructure.csv;

public record ArtePescaCsvRow(
        Long id,
        Long regulacionId,
        String nombre,
        Boolean esProhibido
) {
    public static ArtePescaCsvRow fromCsvLine(String[] cells) {
        return new ArtePescaCsvRow(
                Long.parseLong(cells[0].trim()),
                Long.parseLong(cells[1].trim()),
                cells[2].trim(),
                parseBoolOrNull(cells[3])
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