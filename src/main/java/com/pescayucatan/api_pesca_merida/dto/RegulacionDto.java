package com.pescayucatan.api_pesca_merida.dto;

import com.pescayucatan.api_pesca_merida.enums.CategoriaPesca;
import com.pescayucatan.api_pesca_merida.enums.TipoMedicion;
import com.pescayucatan.api_pesca_merida.model.Regulacion;

import java.math.BigDecimal;

public record RegulacionDto(
        Long id,
        Long pezId,
        String pezNombre,
        Long zonaId,
        String zonaNombre,
        CategoriaPesca categoriaPesca,
        BigDecimal tallaMinima,
        BigDecimal tallaMaxima,
        TipoMedicion tipoMedicion,
        Integer cuotaDiaria,
        Boolean requierePermiso
) {
    public static RegulacionDto fromEntity(Regulacion r) {
        return new RegulacionDto(
                r.getId(),
                r.getPez().getId(),
                r.getPez().getNombreComun(),
                r.getZona().getId(),
                r.getZona().getNombre(),
                r.getCategoriaPesca(),
                r.getTallaMinima(),
                r.getTallaMaxima(),
                r.getTipoMedicion(),
                r.getCuotaDiaria(),
                r.getRequierePermiso()
        );
    }
}
