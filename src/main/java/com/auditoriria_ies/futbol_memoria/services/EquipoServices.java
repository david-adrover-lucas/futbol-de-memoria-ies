package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.Equipo;
import com.auditoriria_ies.futbol_memoria.repository.EquipoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class EquipoServices {
    private final EquipoRepository equipoRepository;

    public EquipoServices(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    public Page<Equipo> listarEquiposDisponibles(String pais, Integer prestigio, Boolean esInternacional, Pageable paginacion) {
        validarPrestigio(prestigio);
        return equipoRepository.buscarConFiltros(pais, prestigio, esInternacional, paginacion);
    }

    public Equipo obtenerEquipoPorId(Long equipoId) {
        return buscarEquipo(equipoId);
    }

    public java.util.List<Equipo> listarEquiposSeleccionablesPorDirectorTecnico(Integer estrellasDT) {
        validarEstrellas(estrellasDT);
        return equipoRepository.findAll().stream()
                .filter(equipo -> equipo.getPrestigio() <= estrellasDT)
                .toList();
    }

    public Equipo buscarRivalAleatorioExcluyendoEquipo(Long equipoId, String pais, Integer prestigio, Boolean esInternacional) {
        Equipo local = buscarEquipo(equipoId);
        validarPrestigio(prestigio);
        var candidatos = equipoRepository.buscarConFiltros(pais, prestigio, esInternacional, Pageable.unpaged())
                .stream().filter(e -> !e.getEquipoId().equals(local.getEquipoId())).toList();
        if (candidatos.isEmpty()) throw new RecursoNoEncontradoException("No hay equipos rivales disponibles con esos filtros");
        return candidatos.get(ThreadLocalRandom.current().nextInt(candidatos.size()));
    }

    public BigDecimal calcularSueldoDelEquipoPorPrestigio(Integer prestigio) {
        validarPrestigio(prestigio);
        return new BigDecimal("480000.00").multiply(BigDecimal.valueOf(prestigio));
    }

    public void validarQueLosEquiposSeanDistintos(Long equipoLocalId, Long equipoVisitanteId) {
        if (equipoLocalId == null || equipoVisitanteId == null || equipoLocalId.equals(equipoVisitanteId)) {
            throw new ReglaNegocioException("El equipo local y el visitante deben ser distintos");
        }
    }

    void validarEquipoDisponibleParaDirectorTecnico(Equipo equipo, Integer estrellasDT) {
        if (equipo == null) throw new ReglaNegocioException("El equipo es obligatorio");
        validarEstrellas(estrellasDT);
        if (equipo.getPrestigio() > estrellasDT) {
            throw new ReglaNegocioException("El prestigio del equipo supera las estrellas actuales del DT");
        }
    }

    Equipo obtenerEntidadPorId(Long equipoId) {
        return buscarEquipo(equipoId);
    }

    private Equipo buscarEquipo(Long equipoId) {
        if (equipoId == null) throw new ReglaNegocioException("El ID del equipo es obligatorio");
        return equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el equipo solicitado"));
    }

    private void validarEstrellas(Integer estrellas) {
        if (estrellas == null || estrellas < 1 || estrellas > 5) {
            throw new ReglaNegocioException("Las estrellas del DT deben estar entre 1 y 5");
        }
    }

    private void validarPrestigio(Integer prestigio) {
        if (prestigio != null && (prestigio < 1 || prestigio > 5)) {
            throw new ReglaNegocioException("El prestigio debe estar entre 1 y 5");
        }
    }

}
