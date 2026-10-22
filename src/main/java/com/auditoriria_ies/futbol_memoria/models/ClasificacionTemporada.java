package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clasificacion_temporada", uniqueConstraints = @UniqueConstraint(
        name = "uq_clasificacion_carrera_temporada", columnNames = {"carrera_id", "numero_temporada"}))
@Getter @Setter @NoArgsConstructor
public class ClasificacionTemporada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clasificacion_temporada_id", nullable = false, updatable = false)
    private Long clasificacionTemporadaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false, foreignKey = @ForeignKey(name = "fk_clasificacion_carrera"))
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_clasificacion_equipo"))
    private Equipo equipo;

    @Column(name = "numero_temporada", nullable = false)
    private Integer numeroTemporada;

    @Column(name = "prestigio_equipo", nullable = false)
    private Integer prestigioEquipo;

    @Column(name = "posicion_final", nullable = false)
    private Integer posicionFinal;

    @Column(name = "codigo_competicion", length = 24)
    private String codigoCompeticion;

    @Column(nullable = false, length = 20)
    private String metodo;

    @Column(name = "tirada_libertadores")
    private Integer tiradaLibertadores;

    @Column(name = "tirada_sudamericana")
    private Integer tiradaSudamericana;

    @Column(name = "probabilidad_libertadores")
    private Integer probabilidadLibertadores;

    @Column(name = "probabilidad_sudamericana")
    private Integer probabilidadSudamericana;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
