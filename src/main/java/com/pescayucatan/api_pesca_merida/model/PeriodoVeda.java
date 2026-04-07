package com.pescayucatan.api_pesca_merida.model;

import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "periodo_veda")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PeriodoVeda {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regulacion_id", nullable = false)
    private Regulacion regulacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_veda", nullable = false)
    private TipoVeda tipoVeda;

    @Column(name = "mes_inicio")
    private Integer mesInicio;

    @Column(name = "dia_inicio")
    private Integer diaInicio;

    @Column(name = "mes_fin")
    private Integer mesFin;

    @Column(name = "dia_fin")
    private Integer diaFin;

    @Column(name = "fuente_dof")
    private String fuenteDof;
}