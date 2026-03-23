package com.pescayucatan.api_pesca_merida.infrastructure.csv;

// EspecieCsvRow.java
public record EspecieCsvRow(
        Integer id,
        String nombreComun,
        String especieCientifica,
        String nombreMaya,
        String tallaMinima,
        String habitat,
        String tecnicaRecomendada,
        String zona
) {
    public static EspecieCsvRow fromCsvLine(String[] cells) {
        return new EspecieCsvRow(
                Integer.parseInt(cells[0]),
                cells[1], cells[2], cells[3],
                cells[4], cells[5], cells[6], cells[7]
        );
    }
}
