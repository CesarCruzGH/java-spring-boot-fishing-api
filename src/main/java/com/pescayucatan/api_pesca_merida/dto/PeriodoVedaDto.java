package com.pescayucatan.api_pesca_merida.dto;

import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.model.PeriodoVeda;

public record PeriodoVedaDto(
        Long id,
        Long regulacionId,
        Long pezId,
        String pezNombre,
        String pezNombreCientifico,
        String pezImagenUrl,
        TipoVeda tipoVeda,
        Integer mesInicio,
        Integer diaInicio,
        Integer mesFin,
        Integer diaFin,
        String fuenteDof
) {
    public static PeriodoVedaDto fromEntity(PeriodoVeda pv) {
        return new PeriodoVedaDto(
                pv.getId(),
                pv.getRegulacion().getId(),
                pv.getRegulacion().getPez().getId(),
                pv.getRegulacion().getPez().getNombreComun(),
                pv.getRegulacion().getPez().getNombreCientifico(),
                pv.getRegulacion().getPez().getImagenUrl(),
                pv.getTipoVeda(),
                pv.getMesInicio(),
                pv.getDiaInicio(),
                pv.getMesFin(),
                pv.getDiaFin(),
                pv.getFuenteDof()
        );
    }
}
