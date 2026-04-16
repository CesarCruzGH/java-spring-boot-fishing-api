package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.exception.PezNotFoundException;
import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.model.PeriodoVeda;
import com.pescayucatan.api_pesca_merida.repository.PeriodoVedaRepository;
import com.pescayucatan.api_pesca_merida.repository.PezRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PezService {

    private final PezRepository pezRepository;
    private final PeriodoVedaRepository periodoVedaRepository;

    @Autowired
    public PezService(PezRepository pezRepository, PeriodoVedaRepository periodoVedaRepository) {
        this.pezRepository = pezRepository;
        this.periodoVedaRepository = periodoVedaRepository;
    }

    public List<Pez> listarTodosLosPeces() {
        return pezRepository.findAll();
    }

    public List<Pez> listarPecesAbiertos() {
        List<Pez> todosLosPeces = pezRepository.findAll();
        List<PeriodoVeda> todasVedas = periodoVedaRepository.findAllWithRelations();
        
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int diaActual = hoy.getDayOfMonth();

        List<Long> idsConVedaActiva = todasVedas.stream()
                .filter(v -> esVedaActiva(v, mesActual, diaActual))
                .map(v -> v.getRegulacion().getPez().getId())
                .distinct()
                .toList();

        return todosLosPeces.stream()
                .filter(pez -> !idsConVedaActiva.contains(pez.getId()))
                .toList();
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

    public Pez guardarPez(Pez pez) {
        return pezRepository.save(pez);
    }

    public Pez obtenerPorId(Long id) {
        return pezRepository.findById(id)
                .orElseThrow(() -> new PezNotFoundException(id));
    }
}