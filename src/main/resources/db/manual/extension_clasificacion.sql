-- Ejecutar una vez sobre futbol_memoria luego de extension_carrera.sql.
-- Guarda una única decisión de clasificación por carrera y temporada.
USE futbol_memoria;

CREATE TABLE IF NOT EXISTS clasificacion_temporada (
  clasificacion_temporada_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  carrera_id BIGINT UNSIGNED NOT NULL,
  equipo_id BIGINT UNSIGNED NOT NULL,
  numero_temporada SMALLINT UNSIGNED NOT NULL,
  prestigio_equipo TINYINT UNSIGNED NOT NULL,
  posicion_final SMALLINT UNSIGNED NOT NULL,
  codigo_competicion VARCHAR(24) NULL,
  metodo ENUM('tabla','sorteo','sin_cupo') NOT NULL,
  tirada_libertadores TINYINT UNSIGNED NULL,
  tirada_sudamericana TINYINT UNSIGNED NULL,
  probabilidad_libertadores TINYINT UNSIGNED NULL,
  probabilidad_sudamericana TINYINT UNSIGNED NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (clasificacion_temporada_id),
  UNIQUE KEY uq_clasificacion_carrera_temporada (carrera_id, numero_temporada),
  KEY idx_clasificacion_competicion_temporada (codigo_competicion, numero_temporada),
  CONSTRAINT fk_clasificacion_carrera FOREIGN KEY (carrera_id)
    REFERENCES carrera(carrera_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_clasificacion_equipo FOREIGN KEY (equipo_id)
    REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_clasificacion_competicion FOREIGN KEY (codigo_competicion)
    REFERENCES competicion(codigo) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_clasificacion_prestigio CHECK (prestigio_equipo BETWEEN 1 AND 5),
  CONSTRAINT ck_clasificacion_posicion CHECK (posicion_final >= 1),
  CONSTRAINT ck_clasificacion_tirada_libertadores CHECK (tirada_libertadores IS NULL OR tirada_libertadores BETWEEN 1 AND 100),
  CONSTRAINT ck_clasificacion_tirada_sudamericana CHECK (tirada_sudamericana IS NULL OR tirada_sudamericana BETWEEN 1 AND 100),
  CONSTRAINT ck_clasificacion_probabilidad_libertadores CHECK (probabilidad_libertadores IS NULL OR probabilidad_libertadores BETWEEN 0 AND 100),
  CONSTRAINT ck_clasificacion_probabilidad_sudamericana CHECK (probabilidad_sudamericana IS NULL OR probabilidad_sudamericana BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
