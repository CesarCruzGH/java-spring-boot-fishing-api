package com.pescayucatan.api_pesca_merida.model;

import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "especies_vedas")
public class EspecieVeda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la entidad Pez existente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pez_id", nullable = false)
    private Pez pez;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ZonaPesca zona;              // enum ya existente

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_veda", nullable = false)
    private TipoVeda tipoVeda;

    // --- Fechas para veda FIJA y PLURIANUAL ---
    @Column(name = "inicio_fijo")
    private LocalDate inicioFijo;        // e.g. 2025-03-01

    @Column(name = "fin_fijo")
    private LocalDate finFijo;           // e.g. 2025-05-31

    // --- Campos para veda CÍCLICA (recurrente anual) ---
    // Almacenar solo mes-día; el año se inyecta en runtime
    @Column(name = "inicio_mes")
    private Integer inicioMes;           // 1-12

    @Column(name = "inicio_dia")
    private Integer inicioDia;           // 1-31

    @Column(name = "fin_mes")
    private Integer finMes;

    @Column(name = "fin_dia")
    private Integer finDia;

    // --- Control de overrides manuales ---
    @Column(name = "cancelada", nullable = false)
    private Boolean cancelada = false;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;    // Texto libre, e.g. "DOF 2025-11-15"

    @Column(name = "cancelada_en")
    private LocalDateTime canceladaEn;

    // --- Auditoría ---
    @Column(name = "fuente_pdf")
    private String fuentePdf;            // Nombre/hash del PDF origen

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PreUpdate
    void onUpdate() { this.actualizadoEn = LocalDateTime.now(); }

    // Getters y setters omitidos (usar Lombok @Data o generarlos)
}