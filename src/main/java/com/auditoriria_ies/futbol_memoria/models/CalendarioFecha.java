package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendario_fecha", uniqueConstraints = {
        @UniqueConstraint(name = "uq_calendario_fecha_numero", columnNames = {"carrera_temporada_id", "numero_fecha"}),
        @UniqueConstraint(name = "uq_calendario_fecha_evento", columnNames = {"carrera_temporada_id", "codigo_competicion", "numero_ronda"})
})
@Getter @Setter @NoArgsConstructor
public class CalendarioFecha {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendario_fecha_id", nullable = false, updatable = false)
    private Long calendarioFechaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_temporada_id", nullable = false)
    private CarreraTemporada temporada;
    @Column(name = "numero_fecha", nullable = false)
    private Integer numeroFecha;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_competicion", referencedColumnName = "codigo", nullable = false)
    private Competicion competicion;
    @Column(name = "numero_ronda", nullable = false)
    private Integer numeroRonda;
    @Column(nullable = false, length = 20)
    private String estado = "pendiente";
    @Column(name = "completada_en")
    private LocalDateTime completadaEn;
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
