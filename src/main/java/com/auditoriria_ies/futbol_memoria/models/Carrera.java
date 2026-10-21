package com.auditoriria_ies.futbol_memoria.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "carrera", uniqueConstraints = @UniqueConstraint(name = "uq_carrera_usuario", columnNames = "usuario_id"), indexes = {
        @Index(name = "idx_carrera_equipo_actual", columnList = "equipo_actual_id"),
        @Index(name = "idx_carrera_experiencia", columnList = "experiencia DESC, carrera_id ASC")})
@Getter @Setter @NoArgsConstructor
public class Carrera {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrera_id", nullable = false, updatable = false)
    private Long carreraId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_carrera_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_actual_id", foreignKey = @ForeignKey(name = "fk_carrera_equipo_actual"))
    private Equipo equipoActual;

    @Column(name = "nombre_dt", nullable = false, length = 60) private String nombreDt;
    @Column(name = "apellido_dt", nullable = false, length = 60) private String apellidoDt;
    @Column(name = "edad_dt", nullable = false) private Integer edadDt;
    @Column(nullable = false, length = 80) private String provincia;
    @Column(name = "numero_avatar", nullable = false) private Integer numeroAvatar;
    @Column(nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 0") private Long experiencia = 0L;
    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 1") private Integer estrellas = 1;
    @Column(name = "numero_temporada", nullable = false, columnDefinition = "SMALLINT UNSIGNED DEFAULT 1") private Integer numeroTemporada = 1;
    @Column(name = "objetivo_contrato", nullable = false, length = 150, columnDefinition = "VARCHAR(150) DEFAULT 'Clasificar a copas'") private String objetivoContrato = "Clasificar a copas";
    @Column(name = "estado_contrato", nullable = false, columnDefinition = "ENUM('activo','completado','terminado') DEFAULT 'activo'") private String estadoContrato = "activo";

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime fechaActualizacion;
    @Column(name = "fecha_eliminacion") private LocalDateTime fechaEliminacion;
}
