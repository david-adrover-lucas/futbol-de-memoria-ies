package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Long> {
    List<OpcionRespuesta> findByPreguntaPreguntaIdOrderByOrdenOpcion(Long preguntaId);
    Optional<OpcionRespuesta> findByPreguntaPreguntaIdAndEsCorrectaTrue(Long preguntaId);
}
