package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.OpcionRespuesta;
import com.auditoriria_ies.futbol_memoria.repository.OpcionRespuestaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OpcionRespuestaServices {
    private final OpcionRespuestaRepository opcionRepository;

    public OpcionRespuestaServices(OpcionRespuestaRepository opcionRepository) {
        this.opcionRepository = opcionRepository;
    }

    public List<OpcionRespuesta> listarOpcionesDePregunta(Long preguntaId) {
        if (preguntaId == null) throw new ReglaNegocioException("El ID de pregunta es obligatorio");
        return opcionRepository.findByPreguntaPreguntaIdOrderByOrdenOpcion(preguntaId);
    }

    public OpcionRespuesta validarOpcionDeRespuesta(Long preguntaId, Long opcionRespuestaId) {
        return buscarYValidarOpcion(preguntaId, opcionRespuestaId);
    }

    OpcionRespuesta buscarYValidarOpcion(Long preguntaId, Long opcionRespuestaId) {
        if (preguntaId == null || opcionRespuestaId == null) throw new ReglaNegocioException("La pregunta y la opción son obligatorias");
        OpcionRespuesta opcion = opcionRepository.findById(opcionRespuestaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la opción de respuesta"));
        if (!opcion.getPregunta().getPreguntaId().equals(preguntaId)) {
            throw new ReglaNegocioException("La opción seleccionada no pertenece a la pregunta");
        }
        return opcion;
    }
}
