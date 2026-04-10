package com.pescayucatan.api_pesca_merida.controller;

import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.service.PezService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/peces")
@RequiredArgsConstructor
public class PezController {

    private final PezService pezService;

    @GetMapping
    public List<Pez> obtenerTemporadaPesca() {
        return pezService.listarTodosLosPeces();
    }

    @GetMapping("/{id}")
    public Pez obtenerUno(@PathVariable Long id) {
        return pezService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Devuelve código 201 sitodo sale bien
    public Pez registrarPez(@RequestBody Pez nuevoPez) {
        // Aquí el JSON que envíe la App se convierte automáticamente en un objeto Java
        return pezService.guardarPez(nuevoPez);
    }
}
