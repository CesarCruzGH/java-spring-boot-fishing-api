package com.pescayucatan.api_pesca_merida.controller;

import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.service.PezService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/abiertos")
    public List<Pez> obtenerPecesAbiertos() {
        return pezService.listarPecesAbiertos();
    }

    @GetMapping("/{id}")
    public Pez obtenerUno(@PathVariable Long id) {
        return pezService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pez registrarPez(@RequestBody Pez nuevoPez) {
        return pezService.guardarPez(nuevoPez);
    }
}
