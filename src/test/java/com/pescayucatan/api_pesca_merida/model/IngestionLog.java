package com.pescayucatan.api_pesca_merida.model;

import com.pescayucatan.api_pesca_merida.enums.EstadoIngestion;
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

    //Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public void setHashSha256(String hashSha256) {
        this.hashSha256 = hashSha256;
    }

    public Integer getTotalFilas() {
        return totalFilas;
    }

    public void setTotalFilas(Integer totalFilas) {
        this.totalFilas = totalFilas;
    }

    public Integer getFilasExitosas() {
        return filasExitosas;
    }

    public void setFilasExitosas(Integer filasExitosas) {
        this.filasExitosas = filasExitosas;
    }

    public Integer getFilasError() {
        return filasError;
    }

    public void setFilasError(Integer filasError) {
        this.filasError = filasError;
    }

    public EstadoIngestion getEstado() {
        return estado;
    }

    public void setEstado(EstadoIngestion estado) {
        this.estado = estado;
    }

    public LocalDateTime getProcesadoEn() {
        return procesadoEn;
    }

    public void setProcesadoEn(LocalDateTime procesadoEn) {
        this.procesadoEn = procesadoEn;
    }

    public String getDetalleError() {
        return detalleError;
    }

    public void setDetalleError(String detalleError) {
        this.detalleError = detalleError;
    }
}
