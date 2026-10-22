package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.TemporadaFixture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TemporadaFixtureRepository extends JpaRepository<TemporadaFixture, Long> {
    List<TemporadaFixture> findByTemporadaCarreraTemporadaIdAndJornadaOrderByNumeroPartido(Long temporadaId, Integer jornada);
    List<TemporadaFixture> findByTemporadaCarreraTemporadaIdOrderByJornadaAscNumeroPartidoAsc(Long temporadaId);
    Optional<TemporadaFixture> findByTemporadaFixtureIdAndTemporadaCarreraCarreraId(Long fixtureId, Long carreraId);
    Optional<TemporadaFixture> findByPartidoPartidoId(Long partidoId);
}
