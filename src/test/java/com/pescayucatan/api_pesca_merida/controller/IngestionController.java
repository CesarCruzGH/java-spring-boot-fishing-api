package com.pescayucatan.api_pesca_merida.controller;

import com.pescayucatan.api_pesca_merida.service.IngestionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// IngestionController.java
@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }
/*
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerManual() {
        ingestionService.ejecutarIngestion();
        return ResponseEntity.ok("Ingestión ejecutada");
    }

    @GetMapping("/status")
    public ResponseEntity<List<IngestionLog>> status() {
        return ResponseEntity.ok(ingestionLogRepo.findAll());
    }

 */
}

