package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import com.pescayucatan.api_pesca_merida.model.EspecieVeda;
import com.pescayucatan.api_pesca_merida.repository.EspecieVedaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
public class VedaCalendarioService {
/*
    private final EspecieVedaRepository vedaRepo;

    public VedaCalendarioService(EspecieVedaRepository vedaRepo) {
        this.vedaRepo = vedaRepo;
    }

    /**
     * Retorna todas las vedas activas para una fecha dada.
     * Maneja automáticamente fijas y cíclicas.

    public List<VedaActivaDTO> getVedasActivas(LocalDate fecha, ZonaPesca zona) {
        List<EspecieVeda> fijas = vedaRepo.findVedasFijaActivas(fecha, zona);

        List<EspecieVeda> ciclicas = vedaRepo.findVedasCiclicaActivas(
                fecha.getMonthValue(), fecha.getDayOfMonth(), zona
        );

        // Combinar y mapear a DTO
        return Stream.concat(fijas.stream(), ciclicas.stream())
                .map(v -> toDTO(v, fecha.getYear()))
                .toList();
    }

    private VedaActivaDTO toDTO(EspecieVeda veda, int anio) {
        LocalDate inicio, fin;

        if (veda.getTipoVeda() == TipoVeda.CICLICA) {
            // Inyectar el año corriente
            inicio = LocalDate.of(anio, veda.getInicioMes(), veda.getInicioDia());
            fin    = LocalDate.of(anio, veda.getFinMes(),    veda.getFinDia());

            // Manejar vedas que cruzan año (e.g. nov-dic → ene-feb del año siguiente)
            if (fin.isBefore(inicio)) {
                fin = fin.plusYears(1);
            }
        } else {
            inicio = veda.getInicioFijo();
            fin    = veda.getFinFijo();
        }

        return new VedaActivaDTO(
                veda.getPez().getNombre(),
                veda.getZona(),
                inicio, fin,
                veda.getTipoVeda()
        );
    }

 */
}