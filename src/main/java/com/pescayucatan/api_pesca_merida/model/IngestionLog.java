package com.pescayucatan.api_pesca_merida.model;

import com.pescayucatan.api_pesca_merida.enums.EstadoIngestion;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingestions_logs")
public class IngestionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "hash_sha256", nullable = false, unique = true)
    private String hashSha256;           // Evita re-ingestión del mismo PDF

    @Column(name = "total_filas")
    private Integer totalFilas;

    @Column(name = "filas_exitosas")
    private Integer filasExitosas;

    @Column(name = "filas_error")
    private Integer filasError;

    @Enumerated(EnumType.STRING)
    private EstadoIngestion estado;      // PROCESANDO / COMPLETADO / ERROR

    @Column(name = "procesado_en")
    private LocalDateTime procesadoEn = LocalDateTime.now();

    @Column(name = "detalle_error", columnDefinition = "TEXT")
    private String detalleError;
}
