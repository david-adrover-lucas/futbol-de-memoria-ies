package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "partido", indexes = {
        @Index(name = "idx_partido_carrera_fecha", columnList = "carrera_id, iniciado_en DESC"),
        @Index(name = "idx_partido_estado_fecha_final", columnList = "estado, finalizado_en DESC"),
        @Index(name = "idx_partido_competicion_fecha", columnList = "competicion_id, iniciado_en DESC"),
        @Index(name = "idx_partido_equipo_local_fecha", columnList = "equipo_local_id, iniciado_en DESC")})
@Getter @Setter @NoArgsConstructor
public class Partido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partido_id", nullable = false, updatable = false) private Long partidoId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partido_carrera")) private Carrera carrera;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competicion_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partido_competicion")) private Competicion competicion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_local_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partido_equipo_local")) private Equipo equipoLocal;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_visitante_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partido_equipo_visitante")) private Equipo equipoVisitante;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToOne(mappedBy = "partido", fetch = FetchType.LAZY)
    private TemporadaFixture temporadaFixture;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToOne(mappedBy = "partido", fetch = FetchType.LAZY)
    private TorneoFixture torneoFixture;
    @Column(nullable = false, columnDefinition = "ENUM('en_curso','finalizado','abandonado') DEFAULT 'en_curso'") private String estado = "en_curso";
    @Column(name = "goles_local", nullable = false, columnDefinition = "SMALLINT UNSIGNED DEFAULT 0") private Integer golesLocal = 0;
    @Column(name = "goles_visitante", nullable = false, columnDefinition = "SMALLINT UNSIGNED DEFAULT 0") private Integer golesVisitante = 0;
    @Column(name = "respuestas_correctas", nullable = false, columnDefinition = "SMALLINT UNSIGNED DEFAULT 0") private Integer respuestasCorrectas = 0;
    @Column(name = "respuestas_incorrectas", nullable = false, columnDefinition = "SMALLINT UNSIGNED DEFAULT 0") private Integer respuestasIncorrectas = 0;
    @Column(name = "variacion_experiencia", nullable = false, columnDefinition = "SMALLINT DEFAULT 0") private Integer variacionExperiencia = 0;
    @CreationTimestamp
    @Column(name = "iniciado_en", nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP") private LocalDateTime iniciadoEn;
    @Column(name = "finalizado_en") private LocalDateTime finalizadoEn;
    @Column(name = "duracion_segundos", columnDefinition = "INT UNSIGNED") private Integer duracionSegundos;
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP") private LocalDateTime fechaCreacion;
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP") private LocalDateTime fechaActualizacion;

}
