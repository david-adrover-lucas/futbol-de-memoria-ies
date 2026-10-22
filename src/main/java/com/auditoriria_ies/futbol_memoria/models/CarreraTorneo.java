package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "carrera_torneo", uniqueConstraints = @UniqueConstraint(name = "uq_carrera_torneo_temporada_codigo", columnNames = {"carrera_id", "numero_temporada", "codigo_competicion"}))
@Getter @Setter @NoArgsConstructor
public class CarreraTorneo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrera_torneo_id", nullable = false, updatable = false)
    private Long carreraTorneoId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_participante_id")
    private Equipo equipoParticipante;
    @Column(name = "codigo_competicion", nullable = false, length = 24)
    private String codigoCompeticion;
    @Column(name = "numero_temporada", nullable = false)
    private Integer numeroTemporada;
    @Column(name = "ronda_actual", nullable = false)
    private Integer rondaActual = 1;
    @Column(nullable = false, length = 20)
    private String estado = "en_curso";
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
