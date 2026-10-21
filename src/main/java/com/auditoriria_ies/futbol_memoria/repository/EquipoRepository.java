package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    @Query("select e from Equipo e where (:pais is null or e.pais = :pais) and (:prestigio is null or e.prestigio = :prestigio) and (:internacional is null or e.esInternacional = :internacional)")
    Page<Equipo> buscarConFiltros(@Param("pais") String pais, @Param("prestigio") Integer prestigio,
                                  @Param("internacional") Boolean internacional, Pageable pageable);
    Optional<Equipo> findByCodigoExterno(String codigoExterno);
    List<Equipo> findByPaisOrderByPrestigioDesc(String pais);
    List<Equipo> findByEsInternacional(Boolean esInternacional);
}
