package com.pescayucatan.api_pesca_merida.exception;

public class PezNotFoundException extends RuntimeException{
    public PezNotFoundException (Long id){
        super("El pez con el ID " + id + " no existe en nuestras costas.");
    }

    public PezNotFoundException (String zonaPesca){
        super("El pez de la zona: " + zonaPesca + " no nada por nuestros mares.");
    }
}

