package com.pescayucatan.api_pesca_merida.model;

import com.pescayucatan.api_pesca_merida.enums.RiesgoCiguatera;
import com.pescayucatan.api_pesca_merida.enums.TipoAgua;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pez")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Pez {
    @Id
    private Long id;

    @Column(name = "nombre_comun")
    private String nombreComun;

    @Column(name = "nombre_cientifico")
    private String nombreCientifico;

    @Column(name = "nombre_maya")
    private String nombreMaya;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "riesgo_ciguatera")
    private RiesgoCiguatera riesgoCiguatera;

    @Column(name = "es_invasiva")
    private Boolean esInvasiva;

    @Column(name = "es_protegida")
    private Boolean esProtegida;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_agua")
    private TipoAgua tipoAgua;

    @Column(name = "migratorio")
    private Boolean migratorio;
}
