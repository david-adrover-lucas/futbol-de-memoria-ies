package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.models.Competicion;
import com.auditoriria_ies.futbol_memoria.models.Carrera;
import com.auditoriria_ies.futbol_memoria.repository.CompeticionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CompeticionServices {
    private final CompeticionRepository competicionRepository;

    public CompeticionServices(CompeticionRepository competicionRepository) {
        this.competicionRepository = competicionRepository;
    }

    public List<Competicion> listarCompeticionesDisponibles(String tipoCompeticion) {
        List<Competicion> competiciones = tipoCompeticion == null || tipoCompeticion.isBlank()
                ? competicionRepository.findAll()
                : competicionRepository.findByTipoCompeticion(tipoCompeticion);
        return competiciones;
    }

    public Competicion obtenerCompeticionPorCodigo(String codigo) {
        return obtenerEntidadPorCodigo(codigo);
    }

    Competicion obtenerEntidadPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) throw new RecursoNoEncontradoException("No se encontró la competición solicitada");
        return competicionRepository.findByCodigo(codigo.trim().toLowerCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la competición solicitada"));
    }

    void validarQueLaCompeticionEsteHabilitada(Competicion competicion, Carrera carrera) {
        // La guía no define requisitos de desbloqueo. Por ahora, todas las competiciones del catálogo están disponibles.
        if (competicion == null || carrera == null) throw new ReglaNegocioException("La competición y la carrera son obligatorias");
    }

    int obtenerModificadorDeDificultad(Competicion competicion) {
        return competicion == null || competicion.getModificadorDificultad() == null ? 0 : competicion.getModificadorDificultad();
    }

}
