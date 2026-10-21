package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "nivel_dificultad", uniqueConstraints = @UniqueConstraint(name = "uq_nivel_dificultad_numero", columnNames = "numero_nivel"))
@Getter @Setter @NoArgsConstructor
public class NivelDificultad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nivel_dificultad_id", nullable = false, updatable = false)
    private Long nivelDificultadId;

    @Column(name = "numero_nivel", nullable = false)
    private Integer numeroNivel;

    @Column(nullable = false, length = 30)
    private String etiqueta;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime fechaActualizacion;
}
