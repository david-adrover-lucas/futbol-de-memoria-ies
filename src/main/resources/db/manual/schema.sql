-- Esquema base extraido del diseno relacional. Compatible con MySQL y MariaDB de XAMPP.
CREATE DATABASE IF NOT EXISTS futbol_memoria
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE futbol_memoria;
CREATE TABLE usuario (
  usuario_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre_usuario VARCHAR(40) NOT NULL,
  correo_electronico VARCHAR(254) NULL,
  hash_contrasena VARCHAR(255) NOT NULL COMMENT 'bcrypt o Argon2; nunca almacenar contrasenas en claro',
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  fecha_eliminacion DATETIME NULL,
  PRIMARY KEY (usuario_id), UNIQUE KEY uq_usuario_nombre_usuario (nombre_usuario), UNIQUE KEY uq_usuario_correo_electronico (correo_electronico),
  KEY idx_usuario_fecha_eliminacion (fecha_eliminacion),
  CONSTRAINT ck_usuario_nombre_usuario CHECK (CHAR_LENGTH(nombre_usuario) BETWEEN 3 AND 40)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE equipo (
  equipo_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  codigo_externo VARCHAR(40) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  apodo VARCHAR(80) NOT NULL,
  pais VARCHAR(80) NOT NULL DEFAULT 'Argentina',
  ruta_escudo VARCHAR(255) NULL,
  prestigio TINYINT UNSIGNED NOT NULL,
  sueldo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  es_internacional TINYINT(1) NOT NULL DEFAULT 0,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (equipo_id), UNIQUE KEY uq_equipo_codigo_externo (codigo_externo),
  KEY idx_equipo_pais_prestigio (pais, prestigio),
  CONSTRAINT ck_equipo_prestigio CHECK (prestigio BETWEEN 1 AND 5),
  CONSTRAINT ck_equipo_sueldo CHECK (sueldo >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE nivel_dificultad (
  nivel_dificultad_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  numero_nivel TINYINT UNSIGNED NOT NULL,
  etiqueta VARCHAR(30) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (nivel_dificultad_id), UNIQUE KEY uq_nivel_dificultad_numero (numero_nivel),
  CONSTRAINT ck_nivel_dificultad_numero CHECK (numero_nivel BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE competicion (
  competicion_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(24) NOT NULL,
  nombre VARCHAR(80) NOT NULL,
  modificador_dificultad TINYINT NOT NULL DEFAULT 0,
  tipo_competicion ENUM('liga','copa') NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (competicion_id), UNIQUE KEY uq_competicion_codigo (codigo),
  CONSTRAINT ck_competicion_modificador CHECK (modificador_dificultad BETWEEN 0 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE pregunta (
  pregunta_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nivel_dificultad_id BIGINT UNSIGNED NOT NULL,
  texto_pregunta VARCHAR(500) NOT NULL,
  tipo_pregunta ENUM('opcion_multiple','penal_numerico') NOT NULL DEFAULT 'opcion_multiple',
  respuesta_numerica DECIMAL(10,2) NULL,
  tolerancia_numerica DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  esta_activa TINYINT(1) NOT NULL DEFAULT 1,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (pregunta_id), KEY idx_pregunta_nivel_activa (nivel_dificultad_id,esta_activa),
  CONSTRAINT fk_pregunta_nivel_dificultad FOREIGN KEY (nivel_dificultad_id) REFERENCES nivel_dificultad(nivel_dificultad_id) ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT ck_pregunta_tipo_respuesta CHECK ((tipo_pregunta='penal_numerico' AND respuesta_numerica IS NOT NULL) OR (tipo_pregunta='opcion_multiple' AND respuesta_numerica IS NULL)),
  CONSTRAINT ck_pregunta_tolerancia CHECK (tolerancia_numerica >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE opcion_respuesta (
  opcion_respuesta_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  pregunta_id BIGINT UNSIGNED NOT NULL,
  orden_opcion TINYINT UNSIGNED NOT NULL,
  texto_opcion VARCHAR(255) NOT NULL,
  es_correcta TINYINT(1) NOT NULL DEFAULT 0,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (opcion_respuesta_id), UNIQUE KEY uq_opcion_pregunta_orden (pregunta_id,orden_opcion),
  KEY idx_opcion_pregunta_correcta (pregunta_id,es_correcta),
  CONSTRAINT fk_opcion_pregunta FOREIGN KEY (pregunta_id) REFERENCES pregunta(pregunta_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ck_opcion_orden CHECK (orden_opcion BETWEEN 1 AND 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE carrera (
  carrera_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT UNSIGNED NOT NULL,
  equipo_actual_id BIGINT UNSIGNED NULL,
  nombre_dt VARCHAR(60) NOT NULL,
  apellido_dt VARCHAR(60) NOT NULL,
  edad_dt TINYINT UNSIGNED NOT NULL,
  provincia VARCHAR(80) NOT NULL,
  numero_avatar TINYINT UNSIGNED NOT NULL,
  experiencia INT UNSIGNED NOT NULL DEFAULT 0,
  estrellas TINYINT UNSIGNED NOT NULL DEFAULT 1,
  numero_temporada SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  objetivo_contrato VARCHAR(150) NOT NULL DEFAULT 'Clasificar a copas',
  estado_contrato ENUM('activo','completado','terminado') NOT NULL DEFAULT 'activo',
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  fecha_eliminacion DATETIME NULL,
  PRIMARY KEY (carrera_id), UNIQUE KEY uq_carrera_usuario (usuario_id), KEY idx_carrera_equipo_actual (equipo_actual_id), KEY idx_carrera_experiencia (experiencia DESC, carrera_id ASC),
  CONSTRAINT fk_carrera_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_carrera_equipo_actual FOREIGN KEY (equipo_actual_id) REFERENCES equipo(equipo_id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT ck_carrera_edad_dt CHECK (edad_dt BETWEEN 16 AND 100),
  CONSTRAINT ck_carrera_numero_avatar CHECK (numero_avatar BETWEEN 1 AND 6),
  CONSTRAINT ck_carrera_estrellas CHECK (estrellas BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE partido (
  partido_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  carrera_id BIGINT UNSIGNED NOT NULL,
  competicion_id BIGINT UNSIGNED NOT NULL,
  equipo_local_id BIGINT UNSIGNED NOT NULL,
  equipo_visitante_id BIGINT UNSIGNED NOT NULL,
  estado ENUM('en_curso','finalizado','abandonado') NOT NULL DEFAULT 'en_curso',
  goles_local SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  goles_visitante SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  respuestas_correctas SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  respuestas_incorrectas SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  variacion_experiencia SMALLINT NOT NULL DEFAULT 0,
  iniciado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  finalizado_en DATETIME NULL,
  duracion_segundos INT UNSIGNED NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (partido_id),
  KEY idx_partido_carrera_fecha (carrera_id,iniciado_en DESC),
  KEY idx_partido_estado_fecha_final (estado,finalizado_en DESC),
  KEY idx_partido_competicion_fecha (competicion_id,iniciado_en DESC),
  KEY idx_partido_equipo_local_fecha (equipo_local_id,iniciado_en DESC),
  CONSTRAINT fk_partido_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(carrera_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_partido_competicion FOREIGN KEY (competicion_id) REFERENCES competicion(competicion_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_partido_equipo_local FOREIGN KEY (equipo_local_id) REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_partido_equipo_visitante FOREIGN KEY (equipo_visitante_id) REFERENCES equipo(equipo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_partido_equipos_distintos CHECK (equipo_local_id <> equipo_visitante_id),
  CONSTRAINT ck_partido_duracion CHECK (duracion_segundos IS NULL OR duracion_segundos >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE intento_partido (
  intento_partido_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  partido_id BIGINT UNSIGNED NOT NULL,
  pregunta_id BIGINT UNSIGNED NOT NULL,
  opcion_respuesta_id BIGINT UNSIGNED NULL,
  numero_intento SMALLINT UNSIGNED NOT NULL,
  texto_respuesta VARCHAR(255) NULL,
  es_correcta TINYINT(1) NOT NULL,
  tiempo_respuesta_segundos SMALLINT UNSIGNED NOT NULL,
  tipo_comodin ENUM('ninguno','doble','penal','cincuenta') NOT NULL DEFAULT 'ninguno',
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (intento_partido_id), UNIQUE KEY uq_intento_partido_numero (partido_id,numero_intento),
  KEY idx_intento_pregunta (pregunta_id),
  CONSTRAINT fk_intento_partido FOREIGN KEY (partido_id) REFERENCES partido(partido_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_intento_pregunta FOREIGN KEY (pregunta_id) REFERENCES pregunta(pregunta_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_intento_opcion_respuesta FOREIGN KEY (opcion_respuesta_id) REFERENCES opcion_respuesta(opcion_respuesta_id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT ck_intento_tiempo_respuesta CHECK (tiempo_respuesta_segundos <= 15)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
