package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.model.Pez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PezRepository extends JpaRepository<Pez, Long> {
    // CAMBIO AQUÍ: Debe decir NombreComun (que es el nombre de tu variable en la clase Pez)
    Optional<Pez> findByNombreComunIgnoreCase(String nombreComun);

}
