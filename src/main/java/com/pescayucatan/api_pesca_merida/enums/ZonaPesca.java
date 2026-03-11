package com.pescayucatan.api_pesca_merida.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ZonaPesca {
    YUCATAN("Yucatán"),
    CAMPECHE("Campeche"),
    QUINTANA_ROO("Quintana Roo"),
    GOLFO_DE_MEXICO("Golfo de México");

    private final String nombre;

    ZonaPesca(String nombre) {
        this.nombre = nombre;
    }

    @JsonValue // Spring usará este texto al enviar datos al navegador
    public String getNombre() {
        return nombre;
    }

    @JsonCreator // Spring usará esto para entender lo que reciba por POST
    public static ZonaPesca forValue(String value) {
        for (ZonaPesca zona : ZonaPesca.values()) {
            if (zona.nombre.equalsIgnoreCase(value) || zona.name().equalsIgnoreCase(value)) {
                return zona;
            }
        }
        return null;
    }
}