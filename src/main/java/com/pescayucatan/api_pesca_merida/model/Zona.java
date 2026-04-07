package com.pescayucatan.api_pesca_merida.model;

import com.pescayucatan.api_pesca_merida.enums.CategoriaHidrica;
import com.pescayucatan.api_pesca_merida.enums.MacroZona;
import com.pescayucatan.api_pesca_merida.enums.TipoRestriccion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "zona")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Zona {

    @Id
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "macro_zona", nullable = false)
    private MacroZona macroZona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_restriccion", nullable = false)
    private TipoRestriccion tipoRestriccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_hidrica", nullable = false)
    private CategoriaHidrica categoriaHidrica;

    @Column(name = "es_anp")
    private Boolean esAnp;

    @Column(name = "municipio_sede")
    private String municipioSede;

    @Column(name = "notas_especificas", columnDefinition = "TEXT")
    private String notasEspecificas;
}