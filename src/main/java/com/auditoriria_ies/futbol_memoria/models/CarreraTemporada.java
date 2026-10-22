package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "carrera_temporada", uniqueConstraints = @UniqueConstraint(name = "uq_temporada_carrera_numero", columnNames = {"carrera_id", "numero_temporada"}))
@Getter @Setter @NoArgsConstructor
public class CarreraTemporada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrera_temporada_id", nullable = false, updatable = false)
    private Long carreraTemporadaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;
    @Column(name = "numero_temporada", nullable = false)
    private Integer numeroTemporada;
    @Column(name = "jornada_actual", nullable = false)
    private Integer jornadaActual = 1;
    @Column(nullable = false, length = 20)
    private String estado = "activa";
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
