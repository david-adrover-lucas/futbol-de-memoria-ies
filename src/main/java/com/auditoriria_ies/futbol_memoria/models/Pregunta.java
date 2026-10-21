package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pregunta", indexes = @Index(name = "idx_pregunta_nivel_activa", columnList = "nivel_dificultad_id, esta_activa"))
@Getter @Setter @NoArgsConstructor
public class Pregunta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pregunta_id", nullable = false, updatable = false)
    private Long preguntaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nivel_dificultad_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pregunta_nivel_dificultad"))
    private NivelDificultad nivelDificultad;

    @Column(name = "texto_pregunta", nullable = false, length = 500)
    private String textoPregunta;

    @Column(name = "tipo_pregunta", nullable = false, columnDefinition = "ENUM('opcion_multiple','penal_numerico') DEFAULT 'opcion_multiple'")
    private String tipoPregunta = "opcion_multiple";

    @Column(name = "respuesta_numerica", precision = 10, scale = 2)
    private BigDecimal respuestaNumerica;

    @Column(name = "tolerancia_numerica", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal toleranciaNumerica = BigDecimal.ZERO;

    @Column(name = "esta_activa", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean estaActiva = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime fechaActualizacion;
}
