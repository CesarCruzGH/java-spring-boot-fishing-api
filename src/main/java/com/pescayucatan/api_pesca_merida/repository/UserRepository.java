package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
