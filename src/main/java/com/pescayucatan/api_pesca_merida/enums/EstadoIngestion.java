package com.pescayucatan.api_pesca_merida.enums;

public enum EstadoIngestion {
    PROCESANDO,
    COMPLETADO,
    ERROR;

    public static EstadoIngestion fromCsvValue(String raw) {
        if (raw == null) throw new IllegalArgumentException("EstadoIngestion null");
        return switch (raw.trim().toUpperCase()) {
            case "PROCESANDO"     -> PROCESANDO;
            case "COMPLETADO" -> COMPLETADO;
            case "ERROR"        -> ERROR;
            default -> throw new IllegalArgumentException(
                    "EstadoIngestion desconocido: " + raw
            );
        };
    }
}
