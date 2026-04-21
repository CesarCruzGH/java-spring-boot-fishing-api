package com.pescayucatan.api_pesca_merida.dto;

import com.pescayucatan.api_pesca_merida.model.ArtePesca;

public record ArtePescaDto(
        Long id,
        String nombre,
        Boolean esProhibido
) {
    public static ArtePescaDto fromEntity(ArtePesca ap) {
        return new ArtePescaDto(
                ap.getId(),
                ap.getNombre(),
                ap.getEsProhibido()
        );
    }
}
