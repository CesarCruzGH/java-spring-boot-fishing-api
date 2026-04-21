package com.pescayucatan.api_pesca_merida.model;

import com.pescayucatan.api_pesca_merida.enums.CategoriaPesca;
import com.pescayucatan.api_pesca_merida.enums.TipoMedicion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regulacion")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Regulacion {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pez_id", nullable = false)
    private Pez pez;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_pesca", nullable = false)
    private CategoriaPesca categoriaPesca;

    @Column(name = "talla_minima", precision = 5, scale = 2)
    private BigDecimal tallaMinima;

    @Column(name = "talla_maxima", precision = 5, scale = 2)
    private BigDecimal tallaMaxima;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_medicion")
    private TipoMedicion tipoMedicion;

    @Column(name = "cuota_diaria")
    private Integer cuotaDiaria;

    @Column(name = "requiere_permiso")
    private Boolean requierePermiso;

    @OneToMany(mappedBy = "regulacion", fetch = FetchType.LAZY)
    private List<ArtePesca> artePesca = new ArrayList<>();
}