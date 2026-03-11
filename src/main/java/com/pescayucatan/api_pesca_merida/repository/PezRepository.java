package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import com.pescayucatan.api_pesca_merida.model.Pez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PezRepository extends JpaRepository<Pez, Long> {
    // Aquí ya tienes métodos como save(), findAll(), findById(), etc.
    List<Pez> findByZona(ZonaPesca zona);
}