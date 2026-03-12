package com.pescayucatan.api_pesca_merida.model;

import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity // Le dice a Spring que esto es una tabla de base de datos
@Table(name = "peces")
public class Pez {
    @Id // Define la llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // El ID se genera solo (auto-increment)
    private Long id;

    private String nombre;
    private String nombreCientifico;
    @Enumerated(EnumType.STRING)
    private ZonaPesca zona;
    private String tipoVeda; // Permanente, Temporal Fija, Temporal Variable
    private LocalDate inicioVeda;
    private LocalDate finVeda;
    private boolean enVeda;

    public Pez() {}

    public Pez(Long id, String nombre, String nombreCientifico, ZonaPesca zona,
               String tipoVeda, LocalDate inicioVeda, LocalDate finVeda, boolean enVeda) {
        this.id = id;
        this.nombre = nombre;
        this.nombreCientifico = nombreCientifico;
        this.zona = zona;
        this.tipoVeda = tipoVeda;
        this.inicioVeda = inicioVeda;
        this.finVeda = finVeda;
        this.enVeda = enVeda;
    }

    @Transient
    public boolean isEnVedaActual() {
        if (inicioVeda == null || finVeda == null) return false;

        LocalDate hoy = LocalDate.now();

        if (inicioVeda.isBefore(finVeda)) {
            // Caso normal: Feb a Marzo
            return !hoy.isBefore(inicioVeda) && !hoy.isAfter(finVeda);
        } else {
            // Caso cruzado: Diciembre a Julio
            return !hoy.isBefore(inicioVeda) || !hoy.isAfter(finVeda);
        }
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public void setNombreCientifico(String nombreCientifico) {
        this.nombreCientifico = nombreCientifico;
    }

    public ZonaPesca getZona() {
        return zona;
    }

    public void setZona(ZonaPesca zona) {
        this.zona = zona;
    }

    public String getTipoVeda() {
        return tipoVeda;
    }

    public void setTipoVeda(String tipoVeda) {
        this.tipoVeda = tipoVeda;
    }

    public LocalDate getInicioVeda() {
        return inicioVeda;
    }

    public void setInicioVeda(LocalDate inicioVeda) {
        this.inicioVeda = inicioVeda;
    }

    public LocalDate getFinVeda() {
        return finVeda;
    }

    public void setFinVeda(LocalDate finVeda) {
        this.finVeda = finVeda;
    }

    public boolean isEnVeda() {
        return enVeda;
    }

    public void setEnVeda(boolean enVeda) {
        this.enVeda = enVeda;
    }
}

