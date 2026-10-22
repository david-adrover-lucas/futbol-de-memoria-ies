-- Ejecutar una sola vez sobre futbol_memoria después de extension_carrera.sql.
-- Persiste las 41 fechas de la temporada y su avance.
USE futbol_memoria;

-- Permite conservar una copa seguida solo para consultar resultados de la CPU.
ALTER TABLE carrera_torneo MODIFY COLUMN equipo_participante_id BIGINT UNSIGNED NULL;

CREATE TABLE IF NOT EXISTS calendario_fecha (
  calendario_fecha_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  carrera_temporada_id BIGINT UNSIGNED NOT NULL,
  numero_fecha TINYINT UNSIGNED NOT NULL,
  codigo_competicion VARCHAR(24) NOT NULL,
  numero_ronda TINYINT UNSIGNED NOT NULL,
  estado ENUM('pendiente','completada') NOT NULL DEFAULT 'pendiente',
  completada_en DATETIME NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (calendario_fecha_id),
  UNIQUE KEY uq_calendario_fecha_numero (carrera_temporada_id, numero_fecha),
  UNIQUE KEY uq_calendario_fecha_evento (carrera_temporada_id, codigo_competicion, numero_ronda),
  KEY idx_calendario_fecha_estado (carrera_temporada_id, estado, numero_fecha),
  CONSTRAINT fk_calendario_fecha_temporada FOREIGN KEY (carrera_temporada_id)
    REFERENCES carrera_temporada(carrera_temporada_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_calendario_fecha_competicion FOREIGN KEY (codigo_competicion)
    REFERENCES competicion(codigo) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_calendario_fecha_numero CHECK (numero_fecha BETWEEN 1 AND 41),
  CONSTRAINT ck_calendario_ronda CHECK (numero_ronda BETWEEN 1 AND 29)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
