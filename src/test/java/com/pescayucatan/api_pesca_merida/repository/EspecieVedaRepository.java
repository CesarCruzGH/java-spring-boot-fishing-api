package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.model.EspecieVeda;
import com.pescayucatan.api_pesca_merida.model.Pez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecieVedaRepository extends JpaRepository<EspecieVeda, Long> {
    Optional<EspecieVeda> findByPezAndZonaAndTipoVeda(
            Pez pez, String zona, TipoVeda tipoVeda
    );
}