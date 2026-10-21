package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.PartidoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface PartidoPreguntaRepository extends JpaRepository<PartidoPregunta, Long> {
    List<PartidoPregunta> findByPartidoPartidoIdOrderByNumeroPreguntaAscPartidoPreguntaIdAsc(Long partidoId);
    List<PartidoPregunta> findByPartidoPartidoIdAndEstadoOrderByNumeroPreguntaAscPartidoPreguntaIdAsc(Long partidoId, String estado);
    Optional<PartidoPregunta> findFirstByPartidoPartidoIdAndEstadoOrderByNumeroPreguntaAscPartidoPreguntaIdAsc(Long partidoId, String estado);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select pp from PartidoPregunta pp where pp.partido.partidoId = :partidoId and pp.estado = :estado order by pp.numeroPregunta asc, pp.partidoPreguntaId asc")
    List<PartidoPregunta> buscarPendientesParaActualizar(@org.springframework.data.repository.query.Param("partidoId") Long partidoId,
                                                          @org.springframework.data.repository.query.Param("estado") String estado);
    boolean existsByPartidoPartidoIdAndPreguntaPreguntaIdAndEstado(Long partidoId, Long preguntaId, String estado);
    long countByPartidoPartidoIdAndEstado(Long partidoId, String estado);
}
