package com.pescayucatan.api_pesca_merida.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PezNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Devuelve error 404
    public String manejarPezNoEncontrado(PezNotFoundException ex) {
        return ex.getMessage();
    }

    // Antena 2: Captura errores de Nombre
    @ExceptionHandler(PezNameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarPezNombreNoEncontrado(PezNameNotFoundException ex) {
        // Aquí podrías incluso añadir lógica extra, como un log de qué nombres busca la gente
        return "Aviso: " + ex.getMessage();
    }
}

