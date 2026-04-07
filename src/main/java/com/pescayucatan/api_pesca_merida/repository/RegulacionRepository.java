package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.model.Regulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegulacionRepository extends JpaRepository<Regulacion, Long> {
    @Query("SELECT r FROM Regulacion r JOIN FETCH r.pez JOIN FETCH r.zona WHERE r.pez.id = :pezId")
    List<Regulacion> findByPezId(@Param("pezId") Long pezId);

    @Query("SELECT r FROM Regulacion r JOIN FETCH r.pez JOIN FETCH r.zona WHERE r.zona.id = :zonaId")
    List<Regulacion> findByZonaId(@Param("zonaId") Long zonaId);
}