package com.pescayucatan.api_pesca_merida.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "arte_pesca")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ArtePesca {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regulacion_id", nullable = false)
    private Regulacion regulacion;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "es_prohibido")
    private Boolean esProhibido;
}