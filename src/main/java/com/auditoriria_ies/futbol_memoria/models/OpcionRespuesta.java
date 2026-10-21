package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "opcion_respuesta",
        uniqueConstraints = @UniqueConstraint(name = "uq_opcion_pregunta_orden", columnNames = {"pregunta_id", "orden_opcion"}),
        indexes = @Index(name = "idx_opcion_pregunta_correcta", columnList = "pregunta_id, es_correcta"))
@Getter @Setter @NoArgsConstructor
public class OpcionRespuesta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opcion_respuesta_id", nullable = false, updatable = false)
    private Long opcionRespuestaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_opcion_pregunta"))
    private Pregunta pregunta;

    @Column(name = "orden_opcion", nullable = false)
    private Integer ordenOpcion;

    @Column(name = "texto_opcion", nullable = false, length = 255)
    private String textoOpcion;

    @Column(name = "es_correcta", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean esCorrecta = false;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime fechaActualizacion;
}
