package com.pescayucatan.api_pesca_merida.dto;

import com.pescayucatan.api_pesca_merida.enums.TipoVeda;

import java.util.List;

public record VedaAgrupadaDto(
        TipoVeda tipoVeda,
        String tipoVedaLabel,
        List<PezBasicoDto> peces
) {
}
