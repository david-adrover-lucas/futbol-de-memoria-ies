-- Ejecutar una sola vez sobre futbol_memoria después de extension_carrera.sql.
-- Registra la tanda y cada pregunta/respuesta para resolver empates de copa sin repetir preguntas.
USE futbol_memoria;

ALTER TABLE torneo_fixture
  MODIFY COLUMN estado ENUM('pendiente','pendiente_penales','finalizado') NOT NULL DEFAULT 'pendiente';

CREATE TABLE IF NOT EXISTS tanda_penales (
  tanda_penales_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  partido_id BIGINT UNSIGNED NOT NULL,
  estado ENUM('en_curso','finalizada') NOT NULL DEFAULT 'en_curso',
  fase_actual ENUM('regular','muerte_subita') NOT NULL DEFAULT 'regular',
  numero_turno_actual SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  goles_jugador SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  goles_rival SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  equipo_ganador_id BIGINT UNSIGNED NULL,
  finalizada_en DATETIME NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (tanda_penales_id),
  UNIQUE KEY uq_tanda_penales_partido (partido_id),
  CONSTRAINT fk_tanda_penales_partido FOREIGN KEY (partido_id)
    REFERENCES partido(partido_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_tanda_penales_ganador FOREIGN KEY (equipo_ganador_id)
    REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tanda_penal_turno (
  tanda_penal_turno_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tanda_penales_id BIGINT UNSIGNED NOT NULL,
  pregunta_id BIGINT UNSIGNED NOT NULL,
  numero_turno SMALLINT UNSIGNED NOT NULL,
  numero_ronda SMALLINT UNSIGNED NOT NULL,
  fase ENUM('regular','muerte_subita') NOT NULL,
  rol_jugador ENUM('patea','ataja') NOT NULL,
  pregunta_iniciada_en DATETIME NOT NULL,
  respondida_en DATETIME NULL,
  correcta TINYINT(1) NULL,
  goles_jugador_generados TINYINT UNSIGNED NOT NULL DEFAULT 0,
  goles_rival_generados TINYINT UNSIGNED NOT NULL DEFAULT 0,
  opcion_respuesta_id BIGINT UNSIGNED NULL,
  texto_respuesta VARCHAR(255) NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (tanda_penal_turno_id),
  UNIQUE KEY uq_tanda_turno_numero (tanda_penales_id, numero_turno),
  UNIQUE KEY uq_tanda_turno_pregunta (tanda_penales_id, pregunta_id),
  KEY idx_tanda_turno_pregunta (pregunta_id),
  CONSTRAINT fk_tanda_turno_tanda FOREIGN KEY (tanda_penales_id)
    REFERENCES tanda_penales(tanda_penales_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_tanda_turno_pregunta FOREIGN KEY (pregunta_id)
    REFERENCES pregunta(pregunta_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_tanda_turno_opcion FOREIGN KEY (opcion_respuesta_id)
    REFERENCES opcion_respuesta(opcion_respuesta_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
