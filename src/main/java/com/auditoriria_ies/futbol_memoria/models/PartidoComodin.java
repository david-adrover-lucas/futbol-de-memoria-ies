package com.auditoriria_ies.futbol_memoria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "partido_comodin", uniqueConstraints = {
        @UniqueConstraint(name = "uq_partido_comodin_tipo", columnNames = {"partido_id", "tipo_comodin"}),
        @UniqueConstraint(name = "uq_partido_comodin_por_pregunta", columnNames = {"partido_id", "pregunta_id"})})
@Getter @Setter @NoArgsConstructor
public class PartidoComodin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partido_comodin_id", nullable = false, updatable = false)
    private Long partidoComodinId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partido_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partido_comodin_partido"))
    private Partido partido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partido_comodin_pregunta"))
    private Pregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_distractor_id", foreignKey = @ForeignKey(name = "fk_partido_comodin_distractor"))
    private OpcionRespuesta opcionDistractor;

    @Column(name = "tipo_comodin", nullable = false, columnDefinition = "ENUM('doble','penal','cincuenta')")
    private String tipoComodin;

    @CreationTimestamp
    @Column(name = "fecha_uso", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaUso;
}
