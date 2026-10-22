package com.auditoriria_ies.futbol_memoria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "temporada_fixture", uniqueConstraints = @UniqueConstraint(name = "uq_temporada_fixture_jornada_partido", columnNames = {"carrera_temporada_id", "jornada", "numero_partido"}))
@Getter @Setter @NoArgsConstructor
public class TemporadaFixture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temporada_fixture_id", nullable = false, updatable = false)
    private Long temporadaFixtureId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_temporada_id", nullable = false)
    @JsonIgnore private CarreraTemporada temporada;
    @Column(nullable = false) private Integer jornada;
    @Column(name = "numero_partido", nullable = false) private Integer numeroPartido;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_local_id", nullable = false)
    private Equipo equipoLocal;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_visitante_id", nullable = false)
    private Equipo equipoVisitante;
    @Column(name = "goles_local") private Integer golesLocal;
    @Column(name = "goles_visitante") private Integer golesVisitante;
    @Column(nullable = false, length = 20) private String estado = "pendiente";
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partido_id", unique = true)
    @JsonIgnore private Partido partido;
}
