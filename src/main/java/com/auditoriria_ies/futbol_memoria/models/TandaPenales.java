package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tanda_penales")
@Getter @Setter @NoArgsConstructor
public class TandaPenales {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tanda_penales_id") private Long tandaPenalesId;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partido_id", nullable = false, unique = true)
    private Partido partido;
    @Column(nullable = false, length = 20) private String estado = "en_curso";
    @Column(name = "fase_actual", nullable = false, length = 20) private String faseActual = "regular";
    @Column(name = "numero_turno_actual", nullable = false) private Integer numeroTurnoActual = 1;
    @Column(name = "goles_jugador", nullable = false) private Integer golesJugador = 0;
    @Column(name = "goles_rival", nullable = false) private Integer golesRival = 0;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_ganador_id") private Equipo equipoGanador;
    @Column(name = "finalizada_en") private LocalDateTime finalizadaEn;
    @CreationTimestamp @Column(name = "fecha_creacion", nullable = false, updatable = false) private LocalDateTime fechaCreacion;
    @UpdateTimestamp @Column(name = "fecha_actualizacion", nullable = false) private LocalDateTime fechaActualizacion;
}
