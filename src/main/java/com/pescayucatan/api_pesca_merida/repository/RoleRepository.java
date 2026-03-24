package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
