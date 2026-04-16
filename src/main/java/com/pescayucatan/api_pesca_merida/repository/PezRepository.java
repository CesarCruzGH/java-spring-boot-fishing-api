package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.model.Pez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PezRepository extends JpaRepository<Pez, Long> {
    Optional<Pez> findByNombreComunIgnoreCase(String nombreComun);
}
