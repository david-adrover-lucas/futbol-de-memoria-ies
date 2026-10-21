package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "intento_partido", uniqueConstraints = @UniqueConstraint(name = "uq_intento_partido_numero", columnNames = {"partido_id", "numero_intento"}), indexes = @Index(name = "idx_intento_pregunta", columnList = "pregunta_id"))
@Getter @Setter @NoArgsConstructor
public class IntentoPartido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intento_partido_id", nullable = false, updatable = false) private Long intentoPartidoId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partido_id", nullable = false, foreignKey = @ForeignKey(name = "fk_intento_partido")) private Partido partido;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_intento_pregunta")) private Pregunta pregunta;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_respuesta_id", foreignKey = @ForeignKey(name = "fk_intento_opcion_respuesta")) private OpcionRespuesta opcionRespuesta;
    @Column(name = "numero_intento", nullable = false) private Integer numeroIntento;
    @Column(name = "texto_respuesta", length = 255) private String textoRespuesta;
    @Column(name = "es_correcta", nullable = false, columnDefinition = "TINYINT(1)") private Boolean esCorrecta;
    @Column(name = "tiempo_respuesta_segundos", nullable = false, columnDefinition = "SMALLINT UNSIGNED") private Integer tiempoRespuestaSegundos;
    @Column(name = "tipo_comodin", nullable = false, columnDefinition = "ENUM('ninguno','doble','penal','cincuenta') DEFAULT 'ninguno'") private String tipoComodin = "ninguno";
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP") private LocalDateTime fechaCreacion;
}
