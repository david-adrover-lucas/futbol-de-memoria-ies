package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    List<Pregunta> findByNivelDificultadNivelDificultadIdAndEstaActivaTrue(Long nivelDificultadId);
    List<Pregunta> findByEstaActivaTrue();
}
