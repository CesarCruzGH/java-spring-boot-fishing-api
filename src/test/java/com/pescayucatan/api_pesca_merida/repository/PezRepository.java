package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.model.Pez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PezRepository extends JpaRepository<Pez, Long> {
    // Aquí ya tienes métodos como save(), findAll(), findById(), etc.
    List<Pez> findByZona(String zona);
    Optional<Pez> findByNombreComunIgnoreCase(String nombreComun);

}