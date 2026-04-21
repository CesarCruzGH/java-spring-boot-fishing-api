package com.pescayucatan.api_pesca_merida.controller;

import com.pescayucatan.api_pesca_merida.dto.PezBasicoDto;
import com.pescayucatan.api_pesca_merida.dto.PeriodoVedaDto;
import com.pescayucatan.api_pesca_merida.dto.VedaAgrupadaDto;
import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.model.PeriodoVeda;
import com.pescayucatan.api_pesca_merida.repository.PeriodoVedaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/periodos-veda")
@RequiredArgsConstructor
public class PeriodoVedaController {

    private final PeriodoVedaRepository periodoVedaRepository;

    @GetMapping("/actuales")
    public ResponseEntity<List<PeriodoVedaDto>> obtenerVedasActuales() {
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int diaActual = hoy.getDayOfMonth();

        List<PeriodoVeda> todasVedas = periodoVedaRepository.findAllWithRelations();

        List<PeriodoVedaDto> vedasActuales = todasVedas.stream()
                .filter(v -> esVedaActiva(v, mesActual, diaActual))
                .map(PeriodoVedaDto::fromEntity)
                .toList();

        return ResponseEntity.ok(vedasActuales);
    }

    @GetMapping("/pez/{pezId}")
    public ResponseEntity<List<PeriodoVedaDto>> obtenerVedasPorPez(@PathVariable Long pezId) {
        List<PeriodoVedaDto> dtos = periodoVedaRepository.findByRegulacionPezId(pezId).stream()
                .map(PeriodoVedaDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/agrupados")
    public ResponseEntity<List<VedaAgrupadaDto>> obtenerVedasAgrupadas() {
        List<PeriodoVeda> todasVedas = periodoVedaRepository.findAllWithRelations();

        Map<TipoVeda, List<PeriodoVeda>> vedasPorTipo = todasVedas.stream()
                .collect(Collectors.groupingBy(PeriodoVeda::getTipoVeda));

        List<VedaAgrupadaDto> resultado = vedasPorTipo.entrySet().stream()
                .map(entry -> {
                    TipoVeda tipoVeda = entry.getKey();
                    List<PeriodoVeda> vedasDelTipo = entry.getValue();

                    List<PezBasicoDto> pecesUnicos = vedasDelTipo.stream()
                            .map(v -> v.getRegulacion().getPez())
                            .distinct()
                            .map(pez -> new PezBasicoDto(pez.getId(), pez.getNombreComun(), pez.getImagenUrl()))
                            .toList();

                    return new VedaAgrupadaDto(
                            tipoVeda,
                            obtenerLabelParaTipoVeda(tipoVeda),
                            pecesUnicos
                    );
                })
                .toList();

        return ResponseEntity.ok(resultado);
    }

    private String obtenerLabelParaTipoVeda(TipoVeda tipo) {
        return switch (tipo) {
            case FIJA -> "Vedas Cíclicas (Anuales)";
            case VARIABLE -> "Vedas Temporales";
            case PERMANENTE -> "Vedas Permanentes";
            case PLURIANUAL -> "Vedas Plurianuales";
        };
    }

    private boolean esVedaActiva(PeriodoVeda veda, int mesActual, int diaActual) {
        TipoVeda tipo = veda.getTipoVeda();

        if (tipo == TipoVeda.PERMANENTE) {
            return true;
        }

        if (veda.getMesInicio() == null || veda.getMesFin() == null) {
            return false;
        }

        int mesInicio = veda.getMesInicio();
        int diaInicio = veda.getDiaInicio() != null ? veda.getDiaInicio() : 1;
        int mesFin = veda.getMesFin();
        int diaFin = veda.getDiaFin() != null ? veda.getDiaFin() : 28;

        if (mesInicio <= mesFin) {
            return (mesActual > mesInicio || (mesActual == mesInicio && diaActual >= diaInicio))
                    && (mesActual < mesFin || (mesActual == mesFin && diaActual <= diaFin));
        } else {
            return (mesActual > mesInicio || (mesActual == mesInicio && diaActual >= diaInicio))
                    || (mesActual < mesFin || (mesActual == mesFin && diaActual <= diaFin));
        }
    }
}
