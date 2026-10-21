package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.PartidoComodin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PartidoComodinRepository extends JpaRepository<PartidoComodin, Long> {
    boolean existsByPartidoPartidoIdAndTipoComodin(Long partidoId, String tipoComodin);
    boolean existsByPartidoPartidoIdAndPreguntaPreguntaId(Long partidoId, Long preguntaId);
    Optional<PartidoComodin> findByPartidoPartidoIdAndPreguntaPreguntaIdAndTipoComodin(Long partidoId, Long preguntaId, String tipoComodin);
    Optional<PartidoComodin> findByPartidoPartidoIdAndPreguntaPreguntaId(Long partidoId, Long preguntaId);
    List<PartidoComodin> findByPartidoPartidoId(Long partidoId);
}
