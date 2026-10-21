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
@Table(name = "equipo",
        uniqueConstraints = @UniqueConstraint(name = "uq_equipo_codigo_externo", columnNames = "codigo_externo"),
        indexes = @Index(name = "idx_equipo_pais_prestigio", columnList = "pais, prestigio"))
@Getter @Setter @NoArgsConstructor
public class Equipo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipo_id", nullable = false, updatable = false)
    private Long equipoId;

    @Column(name = "codigo_externo", nullable = false, length = 40)
    private String codigoExterno;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apodo;

    @Column(nullable = false, length = 80, columnDefinition = "VARCHAR(80) DEFAULT 'Argentina'")
    private String pais = "Argentina";

    @Column(name = "ruta_escudo", length = 255)
    private String rutaEscudo;

    @Column(nullable = false)
    private Integer prestigio;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal sueldo = BigDecimal.ZERO;

    @Column(name = "es_internacional", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean esInternacional = false;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime fechaActualizacion;
}
