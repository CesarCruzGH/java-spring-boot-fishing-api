package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.dto.RegulacionConArtesDto;
import com.pescayucatan.api_pesca_merida.dto.RegulacionDto;
import com.pescayucatan.api_pesca_merida.repository.RegulacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegulacionService {

    private final RegulacionRepository regulacionRepository;

    public List<RegulacionDto> obtenerRegulacionesPorPez(Long pezId) {
        return regulacionRepository.findByPezId(pezId).stream()
                .map(RegulacionDto::fromEntity)
                .toList();
    }

    public List<RegulacionConArtesDto> obtenerRegulacionesPorPezConArtes(Long pezId) {
        return regulacionRepository.findByPezIdWithArtes(pezId).stream()
                .map(RegulacionConArtesDto::fromEntity)
                .toList();
    }

    public List<RegulacionDto> obtenerRegulacionesPorZona(Long zonaId) {
        return regulacionRepository.findByZonaId(zonaId).stream()
                .map(RegulacionDto::fromEntity)
                .toList();
    }
}
