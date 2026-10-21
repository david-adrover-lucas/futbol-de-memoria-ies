package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "competicion", uniqueConstraints = @UniqueConstraint(name = "uq_competicion_codigo", columnNames = "codigo"))
@Getter @Setter @NoArgsConstructor
public class Competicion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "competicion_id", nullable = false, updatable = false)
    private Long competicionId;

    @Column(nullable = false, length = 24)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "modificador_dificultad", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer modificadorDificultad = 0;

    @Column(name = "tipo_competicion", nullable = false, columnDefinition = "ENUM('liga','copa')")
    private String tipoCompeticion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime fechaActualizacion;
}
