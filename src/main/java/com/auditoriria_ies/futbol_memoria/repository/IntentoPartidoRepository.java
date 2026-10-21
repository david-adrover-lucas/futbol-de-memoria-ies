package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.IntentoPartido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IntentoPartidoRepository extends JpaRepository<IntentoPartido, Long> {
    List<IntentoPartido> findByPartidoPartidoIdOrderByNumeroIntento(Long partidoId);
    boolean existsByPartidoPartidoIdAndNumeroIntento(Long partidoId, Integer numeroIntento);
    boolean existsByPartidoPartidoIdAndPreguntaPreguntaId(Long partidoId, Long preguntaId);
    Optional<IntentoPartido> findByPartidoPartidoIdAndNumeroIntento(Long partidoId, Integer numeroIntento);
}
