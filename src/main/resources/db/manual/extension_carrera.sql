
USE futbol_memoria;

CREATE TABLE IF NOT EXISTS carrera_temporada (
  carrera_temporada_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  carrera_id BIGINT UNSIGNED NOT NULL,
  numero_temporada SMALLINT UNSIGNED NOT NULL,
  jornada_actual SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  estado ENUM('activa','finalizada') NOT NULL DEFAULT 'activa',
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (carrera_temporada_id),
  UNIQUE KEY uq_temporada_carrera_numero (carrera_id, numero_temporada),
  CONSTRAINT fk_temporada_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(carrera_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS temporada_fixture (
  temporada_fixture_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  carrera_temporada_id BIGINT UNSIGNED NOT NULL,
  jornada SMALLINT UNSIGNED NOT NULL,
  numero_partido SMALLINT UNSIGNED NOT NULL,
  equipo_local_id BIGINT UNSIGNED NOT NULL,
  equipo_visitante_id BIGINT UNSIGNED NOT NULL,
  goles_local SMALLINT UNSIGNED NULL,
  goles_visitante SMALLINT UNSIGNED NULL,
  estado ENUM('pendiente','finalizado') NOT NULL DEFAULT 'pendiente',
  partido_id BIGINT UNSIGNED NULL,
  PRIMARY KEY (temporada_fixture_id),
  UNIQUE KEY uq_temporada_fixture_jornada_partido (carrera_temporada_id, jornada, numero_partido),
  UNIQUE KEY uq_temporada_fixture_partido (partido_id),
  KEY idx_temporada_fixture_jornada_estado (carrera_temporada_id, jornada, estado),
  CONSTRAINT fk_fixture_temporada FOREIGN KEY (carrera_temporada_id) REFERENCES carrera_temporada(carrera_temporada_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_fixture_temporada_local FOREIGN KEY (equipo_local_id) REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_fixture_temporada_visitante FOREIGN KEY (equipo_visitante_id) REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_fixture_temporada_partido FOREIGN KEY (partido_id) REFERENCES partido(partido_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS carrera_torneo (
  carrera_torneo_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  carrera_id BIGINT UNSIGNED NOT NULL,
  equipo_participante_id BIGINT UNSIGNED NOT NULL,
  codigo_competicion VARCHAR(24) NOT NULL,
  numero_temporada SMALLINT UNSIGNED NOT NULL,
  ronda_actual TINYINT UNSIGNED NOT NULL DEFAULT 1,
  estado ENUM('en_curso','campeon','eliminado') NOT NULL DEFAULT 'en_curso',
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (carrera_torneo_id),
  UNIQUE KEY uq_carrera_torneo_temporada_codigo (carrera_id, numero_temporada, codigo_competicion),
  CONSTRAINT fk_carrera_torneo_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(carrera_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_carrera_torneo_equipo FOREIGN KEY (equipo_participante_id) REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_carrera_torneo_competicion FOREIGN KEY (codigo_competicion) REFERENCES competicion(codigo) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS torneo_fixture (
  torneo_fixture_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  carrera_torneo_id BIGINT UNSIGNED NOT NULL,
  ronda TINYINT UNSIGNED NOT NULL,
  numero_partido TINYINT UNSIGNED NOT NULL,
  equipo_local_id BIGINT UNSIGNED NOT NULL,
  equipo_visitante_id BIGINT UNSIGNED NOT NULL,
  goles_local SMALLINT UNSIGNED NULL,
  goles_visitante SMALLINT UNSIGNED NULL,
  estado ENUM('pendiente','finalizado') NOT NULL DEFAULT 'pendiente',
  partido_id BIGINT UNSIGNED NULL,
  PRIMARY KEY (torneo_fixture_id),
  UNIQUE KEY uq_torneo_fixture_ronda_partido (carrera_torneo_id, ronda, numero_partido),
  UNIQUE KEY uq_torneo_fixture_partido (partido_id),
  KEY idx_torneo_fixture_ronda (carrera_torneo_id, ronda),
  CONSTRAINT fk_fixture_torneo FOREIGN KEY (carrera_torneo_id) REFERENCES carrera_torneo(carrera_torneo_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_fixture_torneo_local FOREIGN KEY (equipo_local_id) REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_fixture_torneo_visitante FOREIGN KEY (equipo_visitante_id) REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_fixture_torneo_partido FOREIGN KEY (partido_id) REFERENCES partido(partido_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
