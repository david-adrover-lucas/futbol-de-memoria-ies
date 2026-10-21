package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.IntentoPartido;
import com.auditoriria_ies.futbol_memoria.repository.IntentoPartidoRepository;
import com.auditoriria_ies.futbol_memoria.repository.PartidoRepository;
import com.auditoriria_ies.futbol_memoria.repository.PreguntaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class IntentoPartidoServices {
    private final IntentoPartidoRepository intentoRepository;
    private final PartidoRepository partidoRepository;
    private final PreguntaRepository preguntaRepository;
    private final OpcionRespuestaServices opcionServices;
    private final CarreraServices carreraServices;

    public IntentoPartidoServices(IntentoPartidoRepository intentoRepository, PartidoRepository partidoRepository,
                                  PreguntaRepository preguntaRepository, OpcionRespuestaServices opcionServices,
                                  CarreraServices carreraServices) {
        this.intentoRepository = intentoRepository;
        this.partidoRepository = partidoRepository;
        this.preguntaRepository = preguntaRepository;
        this.opcionServices = opcionServices;
        this.carreraServices = carreraServices;
    }

    @Transactional
    IntentoPartido registrarIntentoDeRespuesta(Long partidoId, Long preguntaId, Long opcionRespuestaId,
                                               String textoRespuesta, Integer tiempoSegundos,
                                               String tipoComodin, boolean esCorrecta) {
        var partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el partido"));
        if (!"en_curso".equals(partido.getEstado())) throw new ReglaNegocioException("El partido ya no está en curso");
        var pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la pregunta"));
        validarTiempoRegistrado(tiempoSegundos);
        Integer siguiente = intentoRepository.findByPartidoPartidoIdOrderByNumeroIntento(partidoId).size() + 1;
        validarOrdenDelIntento(partidoId, siguiente);

        IntentoPartido intento = new IntentoPartido();
        intento.setPartido(partido);
        intento.setPregunta(pregunta);
        if (opcionRespuestaId != null) intento.setOpcionRespuesta(opcionServices.buscarYValidarOpcion(preguntaId, opcionRespuestaId));
        intento.setNumeroIntento(siguiente);
        intento.setTextoRespuesta(textoRespuesta);
        intento.setEsCorrecta(esCorrecta);
        intento.setTiempoRespuestaSegundos(tiempoSegundos);
        intento.setTipoComodin(normalizarComodin(tipoComodin));
        return intentoRepository.save(intento);
    }

    public List<IntentoPartido> listarIntentosDePartido(Long usuarioId, Long partidoId) {
        var partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el partido"));
        carreraServices.validarQueLaCarreraPertenezcaAlUsuario(partido.getCarrera().getCarreraId(), usuarioId);
        return intentoRepository.findByPartidoPartidoIdOrderByNumeroIntento(partidoId);
    }

    private void validarOrdenDelIntento(Long partidoId, Integer numeroIntento) {
        if (intentoRepository.existsByPartidoPartidoIdAndNumeroIntento(partidoId, numeroIntento)) {
            throw new ReglaNegocioException("Ya existe una respuesta con ese número de intento");
        }
    }

    private void validarTiempoRegistrado(Integer segundosEmpleado) {
        if (segundosEmpleado == null || segundosEmpleado < 0 || segundosEmpleado > 15) {
            throw new ReglaNegocioException("El tiempo de respuesta debe estar entre 0 y 15 segundos");
        }
    }

    private String normalizarComodin(String tipoComodin) {
        if (tipoComodin == null || tipoComodin.isBlank()) return "ninguno";
        if (!List.of("ninguno", "doble", "penal", "cincuenta").contains(tipoComodin)) {
            throw new ReglaNegocioException("El comodín indicado no es válido");
        }
        return tipoComodin;
    }
}
