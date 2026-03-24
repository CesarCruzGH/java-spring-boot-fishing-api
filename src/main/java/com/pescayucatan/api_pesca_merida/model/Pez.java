package com.pescayucatan.api_pesca_merida.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // Le dice a Spring que esto es una tabla de base de datos
@Table(name = "pez")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Pez {
    @Id // Define la llave primaria
    private Long id;

    @Column(name = "nombre_comun")
    private String nombreComun;

    private String especie;
    private String nombreMaya;

    @Column(name = "TALLA_MINIMA")
    private String tallaMinima;

    private String habitat;
    private String tecnicaRecomendada;
    private String zona;
}

