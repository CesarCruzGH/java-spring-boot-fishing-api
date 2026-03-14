package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import com.pescayucatan.api_pesca_merida.model.EspecieVeda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
@Repository
public interface EspecieVedaRepository extends JpaRepository <EspecieVeda, Long>{

    // Vedas fijas activas en una fecha concreta (para consultas del calendario)
    @Query("""
        SELECT v FROM EspecieVeda v
        WHERE v.cancelada = false
          AND v.tipoVeda = 'FIJA'
          AND v.inicioFijo <= :fecha
          AND v.finFijo >= :fecha
          AND (:zona IS NULL OR v.zona = :zona)
        """)
    List<EspecieVeda> findVedasFijaActivas(
            @Param("fecha") LocalDate fecha,
            @Param("zona") ZonaPesca zona
    );

    // Vedas cíclicas activas: se compara mes-día ignorando el año
    @Query("""
        SELECT v FROM EspecieVeda v
        WHERE v.cancelada = false
          AND v.tipoVeda = 'CICLICA'
          AND (:mes > v.inicioMes OR (:mes = v.inicioMes AND :dia >= v.inicioDia))
          AND (:mes < v.finMes    OR (:mes = v.finMes    AND :dia <= v.finDia))
          AND (:zona IS NULL OR v.zona = :zona)
        """)
    List<EspecieVeda> findVedasCiclicaActivas(
            @Param("mes") int mes,
            @Param("dia") int dia,
            @Param("zona") ZonaPesca zona
    );

    // Caso: veda cruza fin de año (e.g. 01-nov → 28-feb)
    // Si finMes < inicioMes → el fin pertenece al año siguiente.
    // La lógica `if (fin.isBefore(inicio)) fin = fin.plusYears(1)` lo cubre.

        // Para consultar si hoy cae en una veda trans-año en la DB:
    // Estrategia: verificar con año actual Y año anterior
    // Añadir al repositorio:
    @Query("""
    SELECT v FROM EspecieVeda v
    WHERE v.cancelada = false
      AND v.tipoVeda = 'CICLICA'
      AND v.finMes < v.inicioMes
      AND (:mes < v.finMes OR (:mes = v.finMes AND :dia <= v.finDia))
    """)
    List<EspecieVeda> findVedasCiclicaTransAnio(
            @Param("mes") int mes,
            @Param("dia") int dia,
            @Param("zona") ZonaPesca zona
    );
}