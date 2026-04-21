package com.pescayucatan.api_pesca_merida.controller;

import com.pescayucatan.api_pesca_merida.dto.RegulacionConArtesDto;
import com.pescayucatan.api_pesca_merida.dto.RegulacionDto;
import com.pescayucatan.api_pesca_merida.service.RegulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regulaciones")
@RequiredArgsConstructor
public class RegulacionController {

    private final RegulacionService regulacionService;

    @GetMapping("/pez/{pezId}")
    public ResponseEntity<List<RegulacionDto>> obtenerRegulacionesPorPez(@PathVariable Long pezId) {
        List<RegulacionDto> dtos = regulacionService.obtenerRegulacionesPorPez(pezId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/pez/{pezId}/detalle")
    public ResponseEntity<List<RegulacionConArtesDto>> obtenerRegulacionesPorPezDetalle(@PathVariable Long pezId) {
        List<RegulacionConArtesDto> dtos = regulacionService.obtenerRegulacionesPorPezConArtes(pezId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/zona/{zonaId}")
    public ResponseEntity<List<RegulacionDto>> obtenerRegulacionesPorZona(@PathVariable Long zonaId) {
        List<RegulacionDto> dtos = regulacionService.obtenerRegulacionesPorZona(zonaId);
        return ResponseEntity.ok(dtos);
    }
}
