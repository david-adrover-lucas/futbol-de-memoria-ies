package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tanda_penal_turno", uniqueConstraints = {
        @UniqueConstraint(name = "uq_tanda_turno_numero", columnNames = {"tanda_penales_id", "numero_turno"}),
        @UniqueConstraint(name = "uq_tanda_turno_pregunta", columnNames = {"tanda_penales_id", "pregunta_id"})})
@Getter @Setter @NoArgsConstructor
public class TandaPenalTurno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tanda_penal_turno_id") private Long tandaPenalTurnoId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tanda_penales_id", nullable = false) private TandaPenales tanda;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false) private Pregunta pregunta;
    @Column(name = "numero_turno", nullable = false) private Integer numeroTurno;
    @Column(name = "numero_ronda", nullable = false) private Integer numeroRonda;
    @Column(name = "fase", nullable = false, length = 20) private String fase;
    @Column(name = "rol_jugador", nullable = false, length = 12) private String rolJugador;
    @Column(name = "pregunta_iniciada_en", nullable = false) private LocalDateTime preguntaIniciadaEn;
    @Column(name = "respondida_en") private LocalDateTime respondidaEn;
    @Column(name = "correcta") private Boolean correcta;
    @Column(name = "goles_jugador_generados", nullable = false) private Integer golesJugadorGenerados = 0;
    @Column(name = "goles_rival_generados", nullable = false) private Integer golesRivalGenerados = 0;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_respuesta_id") private OpcionRespuesta opcionRespuesta;
    @Column(name = "texto_respuesta", length = 255) private String textoRespuesta;
    @CreationTimestamp @Column(name = "fecha_creacion", nullable = false, updatable = false) private LocalDateTime fechaCreacion;
}
