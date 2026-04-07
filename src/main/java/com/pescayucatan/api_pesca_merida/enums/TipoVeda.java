package com.pescayucatan.api_pesca_merida.enums;

public enum TipoVeda {
    FIJA,       // Fechas cíclicas anuales fijas
    VARIABLE,   // Fechas cíclicas que cambian por DOF
    PERMANENTE,          // Sin fechas, prohibición total
    PLURIANUAL;          // Abarca múltiples años

    /**
     * Mapea el texto del CSV de Google Sheets al enum.
     * Ej: "TEMPORAL FIJA" → TEMPORAL_FIJA
     */
    public static TipoVeda fromCsvValue(String raw) {
        if (raw == null) throw new IllegalArgumentException("TipoVeda null");
        return switch (raw.trim().toUpperCase()) {
            case "TEMPORAL FIJA"     -> FIJA;
            case "TEMPORAL VARIABLE" -> VARIABLE;
            case "PERMANENTE"        -> PERMANENTE;
            case "PLURIANUAL"        -> PLURIANUAL;
            default -> throw new IllegalArgumentException(
                    "TipoVeda desconocido: " + raw
            );
        };
    }
}
