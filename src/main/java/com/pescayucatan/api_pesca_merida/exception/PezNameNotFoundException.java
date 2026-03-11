package com.pescayucatan.api_pesca_merida.exception;

public class PezNameNotFoundException extends RuntimeException {
    public PezNameNotFoundException(String name) {
        super("No se encontró ningun pez con el nombre: " + name);
    }
}
