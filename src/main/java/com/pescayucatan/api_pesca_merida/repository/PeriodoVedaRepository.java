package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.model.PeriodoVeda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodoVedaRepository extends JpaRepository<PeriodoVeda, Long> {
    @Query("SELECT pv FROM PeriodoVeda pv JOIN FETCH pv.regulacion r JOIN FETCH r.pez JOIN FETCH r.zona WHERE r.pez.id = :pezId")
    List<PeriodoVeda> findByRegulacionPezId(@Param("pezId") Long pezId);

    @Query("SELECT pv FROM PeriodoVeda pv JOIN FETCH pv.regulacion r JOIN FETCH r.pez JOIN FETCH r.zona")
    List<PeriodoVeda> findAllWithRelations();
}