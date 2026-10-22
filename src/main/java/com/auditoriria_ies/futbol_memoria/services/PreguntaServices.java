package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.Pregunta;
import com.auditoriria_ies.futbol_memoria.repository.PreguntaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class PreguntaServices {
    private final PreguntaRepository preguntaRepository;
    private final OpcionRespuestaServices opcionServices;

    public PreguntaServices(PreguntaRepository preguntaRepository, OpcionRespuestaServices opcionServices) {
        this.preguntaRepository = preguntaRepository;
        this.opcionServices = opcionServices;
    }

    public List<Pregunta> seleccionarPreguntasParaPartido(Integer cantidad, Integer nivelMaximo, Set<Long> preguntasExcluidas) {
        if (cantidad == null || cantidad < 1 || cantidad > 10) throw new ReglaNegocioException("La cantidad de preguntas debe estar entre 1 y 10");
        validarNivelMaximo(nivelMaximo);
        Set<Long> excluidas = preguntasExcluidas == null ? Set.of() : preguntasExcluidas;
        // Stream.toList() devuelve una lista inmutable; shuffle necesita una lista modificable.
        List<Pregunta> elegibles = new ArrayList<>(filtrarPreguntasActivasPorDificultad(nivelMaximo, excluidas));
        if (elegibles.size() < cantidad) throw new RecursoNoEncontradoException("No hay suficientes preguntas activas para este partido");
        Collections.shuffle(elegibles);
        return new ArrayList<>(elegibles.subList(0, cantidad));
    }

    public Pregunta obtenerPreguntaConOpcionesParaPartido(Long preguntaId) {
        Pregunta pregunta = buscarPreguntaActiva(preguntaId);
        if ("opcion_multiple".equals(pregunta.getTipoPregunta())) pregunta.getOpciones().size();
        return pregunta;
    }

    Pregunta crearPreguntaNumericaDePenal(Set<Long> preguntasExcluidas) {
        Set<Long> excluidas = preguntasExcluidas == null ? Set.of() : preguntasExcluidas;
        List<Pregunta> candidatas = preguntaRepository.findByEstaActivaTrue().stream()
                .filter(p -> "penal_numerico".equals(p.getTipoPregunta()))
                .filter(p -> p.getRespuestaNumerica() != null)
                .filter(p -> !excluidas.contains(p.getPreguntaId()))
                .toList();
        if (candidatas.isEmpty()) throw new RecursoNoEncontradoException("No hay preguntas numéricas activas para el penal");
        return candidatas.get(ThreadLocalRandom.current().nextInt(candidatas.size()));
    }

    List<Pregunta> listarPreguntasElegibles(Integer nivelMaximo, Set<Long> excluidas) {
        validarNivelMaximo(nivelMaximo);
        return filtrarPreguntasActivasPorDificultad(nivelMaximo, excluidas == null ? Set.of() : excluidas);
    }

    public boolean validarRespuestaSeleccionada(Long preguntaId, Long opcionRespuestaId) {
        Pregunta pregunta = buscarPreguntaActiva(preguntaId);
        if (!"opcion_multiple".equals(pregunta.getTipoPregunta())) {
            throw new ReglaNegocioException("La pregunta no admite una respuesta de opción múltiple");
        }
        return opcionServices.buscarYValidarOpcion(preguntaId, opcionRespuestaId).getEsCorrecta();
    }

    public boolean validarRespuestaNumericaDePenal(Long preguntaId, BigDecimal respuesta) {
        Pregunta pregunta = buscarPreguntaActiva(preguntaId);
        if (!"penal_numerico".equals(pregunta.getTipoPregunta()) || pregunta.getRespuestaNumerica() == null) {
            throw new ReglaNegocioException("La pregunta no es un penal numérico válido");
        }
        if (respuesta == null) throw new ReglaNegocioException("La respuesta numérica es obligatoria");
        BigDecimal distancia = respuesta.subtract(pregunta.getRespuestaNumerica()).abs();
        return distancia.compareTo(pregunta.getToleranciaNumerica()) <= 0;
    }

    Pregunta buscarPreguntaActiva(Long preguntaId) {
        if (preguntaId == null) throw new ReglaNegocioException("El ID de pregunta es obligatorio");
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la pregunta"));
        if (!Boolean.TRUE.equals(pregunta.getEstaActiva())) throw new RecursoNoEncontradoException("La pregunta no está disponible");
        return pregunta;
    }

    private List<Pregunta> filtrarPreguntasActivasPorDificultad(Integer nivelMaximo, Set<Long> excluidas) {
        return preguntaRepository.findByEstaActivaTrue().stream()
                .filter(p -> p.getNivelDificultad().getNumeroNivel() <= nivelMaximo)
                .filter(p -> !excluidas.contains(p.getPreguntaId()))
                .filter(p -> "opcion_multiple".equals(p.getTipoPregunta()))
                .toList();
    }

    private void validarNivelMaximo(Integer nivelMaximo) {
        if (nivelMaximo == null || nivelMaximo < 1 || nivelMaximo > 5) throw new ReglaNegocioException("El nivel máximo debe estar entre 1 y 5");
    }
}
