package com.pescayucatan.api_pesca_merida.controller;

import com.pescayucatan.api_pesca_merida.model.Zona;
import com.pescayucatan.api_pesca_merida.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaRepository zonaRepository;

    @GetMapping
    public ResponseEntity<List<Zona>> listarZonas() {
        return ResponseEntity.ok(zonaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Zona> obtenerZona(@PathVariable Long id) {
        return zonaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
