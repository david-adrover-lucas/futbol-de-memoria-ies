package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.TandaPenalTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TandaPenalTurnoRepository extends JpaRepository<TandaPenalTurno, Long> {
    List<TandaPenalTurno> findByTandaTandaPenalesIdOrderByNumeroTurnoAsc(Long tandaId);
    Optional<TandaPenalTurno> findByTandaTandaPenalesIdAndNumeroTurno(Long tandaId, Integer numeroTurno);
    boolean existsByTandaTandaPenalesIdAndPreguntaPreguntaId(Long tandaId, Long preguntaId);
}
