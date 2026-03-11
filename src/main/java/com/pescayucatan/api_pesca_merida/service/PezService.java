package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import com.pescayucatan.api_pesca_merida.exception.PezNotFoundException;
import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.repository.PezRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Le dice a Spring que esta es la capa de lógica de negocio
public class PezService {

    private final PezRepository pezRepository;

    // Inyección de dependencias por constructor (Práctica recomendada en 2026)
    @Autowired
    public PezService(PezRepository pezRepository) {
        this.pezRepository = pezRepository;
    }

    public List<Pez> listarTodosLosPeces() {
        return pezRepository.findAll();
    }

    public Pez guardarPez(Pez pez) {
        return pezRepository.save(pez);
    }

    public List<Pez> buscarPeces(String zona) {
        if (zona == null || zona.isEmpty()) {
            return pezRepository.findAll();
        }

        ZonaPesca zonaEnum = ZonaPesca.forValue(zona);

        // 1. Obtenemos la lista primero
        List<Pez> resultados = pezRepository.findByZona(zonaEnum);

        // 2. Verificamos si la lista no tiene nada
        if (resultados.isEmpty()) {
            throw new PezNotFoundException(zona);
        }

        return resultados;
    }
    // En PezService.java
    public Pez obtenerPorId(Long id) {
        return pezRepository.findById(id)
                .orElseThrow(() -> new PezNotFoundException(id));
    }
}