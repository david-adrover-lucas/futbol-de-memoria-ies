package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.CalendarioFecha;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Optional;

public interface CalendarioFechaRepository extends JpaRepository<CalendarioFecha, Long> {
    List<CalendarioFecha> findByTemporadaCarreraTemporadaIdOrderByNumeroFechaAsc(Long temporadaId);
    Optional<CalendarioFecha> findByTemporadaCarreraTemporadaIdAndNumeroFecha(Long temporadaId, Integer numeroFecha);
    Optional<CalendarioFecha> findByTemporadaCarreraTemporadaIdAndCompeticionCodigoAndNumeroRonda(
            Long temporadaId, String codigoCompeticion, Integer numeroRonda);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CalendarioFecha> findFirstByTemporadaCarreraTemporadaIdAndEstadoOrderByNumeroFechaAsc(
            Long temporadaId, String estado);
    boolean existsByTemporadaCarreraTemporadaId(Long temporadaId);
}
