package com.pescayucatan.api_pesca_merida.model;

import jakarta.persistence.*;

@Entity // Le dice a Spring que esto es una tabla de base de datos
@Table(name = "pez")
public class Pez {
    @Id // Define la llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // El ID se genera solo (auto-increment)
    private Long id;
    private String nombreComun;
    private String especie;
    private String nombreMaya;
    private String tallaMinima;
    private String habitad;
    private String tecnicaRecomendada;
    private String zona;

    public Pez() {}

    public Pez(String zona, String tecnicaRecomendada, String habitad, String tallaMinima, String nombreMaya, String especie, String nombreComun, Long id) {
        this.zona = zona;
        this.tecnicaRecomendada = tecnicaRecomendada;
        this.habitad = habitad;
        this.tallaMinima = tallaMinima;
        this.nombreMaya = nombreMaya;
        this.especie = especie;
        this.nombreComun = nombreComun;
        this.id = id;
    }

    //getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreComun() {
        return nombreComun;
    }

    public void setNombreComun(String nombreComun) {
        this.nombreComun = nombreComun;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getNombreMaya() {
        return nombreMaya;
    }

    public void setNombreMaya(String nombreMaya) {
        this.nombreMaya = nombreMaya;
    }

    public String getTallaMinima() {
        return tallaMinima;
    }

    public void setTallaMinima(String tallaMinima) {
        this.tallaMinima = tallaMinima;
    }

    public String getHabitad() {
        return habitad;
    }

    public void setHabitad(String habitad) {
        this.habitad = habitad;
    }

    public String getTecnicaRecomendada() {
        return tecnicaRecomendada;
    }

    public void setTecnicaRecomendada(String tecnicaRecomendada) {
        this.tecnicaRecomendada = tecnicaRecomendada;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }
}

