package com.auditoriria_ies.futbol_memoria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "partido_pregunta",
        uniqueConstraints = @UniqueConstraint(name = "uq_partido_pregunta_partido_pregunta", columnNames = {"partido_id", "pregunta_id"}),
        indexes = @Index(name = "idx_partido_pregunta_estado", columnList = "partido_id, estado, numero_pregunta"))
@Getter @Setter @NoArgsConstructor
public class PartidoPregunta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partido_pregunta_id", nullable = false, updatable = false)
    private Long partidoPreguntaId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partido_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partido_pregunta_partido"))
    private Partido partido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partido_pregunta_pregunta"))
    private Pregunta pregunta;

    @Column(name = "numero_pregunta", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer numeroPregunta;

    @Column(nullable = false, columnDefinition = "ENUM('pendiente','contestada','sustituida') DEFAULT 'pendiente'")
    private String estado = "pendiente";

    @Column(name = "pregunta_iniciada_en")
    private LocalDateTime preguntaIniciadaEn;

    @Column(name = "respondida_en")
    private LocalDateTime respondidaEn;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaAsignacion;
}
