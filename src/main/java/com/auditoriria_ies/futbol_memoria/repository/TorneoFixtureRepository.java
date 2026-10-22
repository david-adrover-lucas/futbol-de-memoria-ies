package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.TorneoFixture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TorneoFixtureRepository extends JpaRepository<TorneoFixture, Long> {
    List<TorneoFixture> findByTorneoCarreraTorneoIdOrderByRondaAscNumeroPartidoAsc(Long torneoId);
    List<TorneoFixture> findByTorneoCarreraTorneoIdAndRondaOrderByNumeroPartido(Long torneoId, Integer ronda);
    Optional<TorneoFixture> findByTorneoFixtureIdAndTorneoCarreraCarreraId(Long fixtureId, Long carreraId);
    Optional<TorneoFixture> findByPartidoPartidoId(Long partidoId);
}
