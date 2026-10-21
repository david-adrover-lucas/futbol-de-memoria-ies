package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.NivelDificultad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NivelDificultadRepository extends JpaRepository<NivelDificultad, Long> {
    Optional<NivelDificultad> findByNumeroNivel(Integer numeroNivel);
}
