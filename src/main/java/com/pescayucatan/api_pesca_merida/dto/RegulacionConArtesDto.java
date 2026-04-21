package com.pescayucatan.api_pesca_merida.dto;

import com.pescayucatan.api_pesca_merida.enums.CategoriaPesca;
import com.pescayucatan.api_pesca_merida.enums.TipoMedicion;
import com.pescayucatan.api_pesca_merida.model.Regulacion;

import java.math.BigDecimal;
import java.util.List;

public record RegulacionConArtesDto(
        Long id,
        Long zonaId,
        String zonaNombre,
        CategoriaPesca categoriaPesca,
        BigDecimal tallaMinima,
        BigDecimal tallaMaxima,
        TipoMedicion tipoMedicion,
        Integer cuotaDiaria,
        Boolean requierePermiso,
        List<ArtePescaDto> artesPesca
) {
    public static RegulacionConArtesDto fromEntity(Regulacion r) {
        List<ArtePescaDto> artes = r.getArtePesca() != null
                ? r.getArtePesca().stream()
                        .map(ArtePescaDto::fromEntity)
                        .toList()
                : List.of();

        return new RegulacionConArtesDto(
                r.getId(),
                r.getZona().getId(),
                r.getZona().getNombre(),
                r.getCategoriaPesca(),
                r.getTallaMinima(),
                r.getTallaMaxima(),
                r.getTipoMedicion(),
                r.getCuotaDiaria(),
                r.getRequierePermiso(),
                artes
        );
    }
}
