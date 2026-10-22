-- Ejecutar sobre la base futbol_memoria luego de aplicar el esquema base.
-- Persiste el orden de preguntas y los comodines utilizados por partido.

CREATE TABLE partido_pregunta (
  partido_pregunta_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  partido_id BIGINT UNSIGNED NOT NULL,
  pregunta_id BIGINT UNSIGNED NOT NULL,
  numero_pregunta SMALLINT UNSIGNED NOT NULL,
  estado ENUM('pendiente','contestada','sustituida') NOT NULL DEFAULT 'pendiente',
  pregunta_iniciada_en DATETIME NULL,
  respondida_en DATETIME NULL,
  fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (partido_pregunta_id),
  UNIQUE KEY uq_partido_pregunta_partido_pregunta (partido_id, pregunta_id),
  KEY idx_partido_pregunta_estado (partido_id, estado, numero_pregunta),
  CONSTRAINT fk_partido_pregunta_partido FOREIGN KEY (partido_id)
    REFERENCES partido(partido_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_partido_pregunta_pregunta FOREIGN KEY (pregunta_id)
    REFERENCES pregunta(pregunta_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_partido_pregunta_numero CHECK (numero_pregunta BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE partido_comodin (
  partido_comodin_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  partido_id BIGINT UNSIGNED NOT NULL,
  pregunta_id BIGINT UNSIGNED NOT NULL,
  opcion_distractor_id BIGINT UNSIGNED NULL,
  tipo_comodin ENUM('doble','penal','cincuenta') NOT NULL,
  fecha_uso TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (partido_comodin_id),
  UNIQUE KEY uq_partido_comodin_tipo (partido_id, tipo_comodin),
  UNIQUE KEY uq_partido_comodin_por_pregunta (partido_id, pregunta_id),
  CONSTRAINT fk_partido_comodin_partido FOREIGN KEY (partido_id)
    REFERENCES partido(partido_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_partido_comodin_pregunta FOREIGN KEY (pregunta_id)
    REFERENCES pregunta(pregunta_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_partido_comodin_distractor FOREIGN KEY (opcion_distractor_id)
    REFERENCES opcion_respuesta(opcion_respuesta_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
