package com.pescayucatan.api_pesca_merida.controller;

import com.pescayucatan.api_pesca_merida.dto.RegulacionDto;
import com.pescayucatan.api_pesca_merida.repository.RegulacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regulaciones")
@RequiredArgsConstructor
public class RegulacionController {

    private final RegulacionRepository regulacionRepository;

    @GetMapping("/pez/{pezId}")
    public ResponseEntity<List<RegulacionDto>> obtenerRegulacionesPorPez(@PathVariable Long pezId) {
        List<RegulacionDto> dtos = regulacionRepository.findByPezId(pezId).stream()
                .map(RegulacionDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/zona/{zonaId}")
    public ResponseEntity<List<RegulacionDto>> obtenerRegulacionesPorZona(@PathVariable Long zonaId) {
        List<RegulacionDto> dtos = regulacionRepository.findByZonaId(zonaId).stream()
                .map(RegulacionDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
