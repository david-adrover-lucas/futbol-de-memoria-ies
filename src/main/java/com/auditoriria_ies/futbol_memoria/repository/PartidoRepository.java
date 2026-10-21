package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Long> {
    List<Partido> findByCarreraCarreraIdOrderByIniciadoEnDesc(Long carreraId);
    List<Partido> findByEstadoOrderByIniciadoEnDesc(String estado);
}
