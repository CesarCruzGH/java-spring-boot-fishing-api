package com.pescayucatan.api_pesca_merida.repository;

import com.pescayucatan.api_pesca_merida.model.IngestionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngestionLogRepository extends JpaRepository<IngestionLog, Long> {
    boolean existsByHashSha256(String hashSha256);
}
