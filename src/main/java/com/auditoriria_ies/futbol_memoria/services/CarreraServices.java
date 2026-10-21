package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.Carrera;
import com.auditoriria_ies.futbol_memoria.models.Partido;
import com.auditoriria_ies.futbol_memoria.models.Usuario;
import com.auditoriria_ies.futbol_memoria.repository.CarreraRepository;
import com.auditoriria_ies.futbol_memoria.repository.PartidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CarreraServices {
    private final CarreraRepository carreraRepository;
    private final UsuarioServices usuarioServices;
    private final EquipoServices equipoServices;
    private final PartidoRepository partidoRepository;

    public CarreraServices(CarreraRepository carreraRepository, UsuarioServices usuarioServices,
                           EquipoServices equipoServices, PartidoRepository partidoRepository) {
        this.carreraRepository = carreraRepository;
        this.usuarioServices = usuarioServices;
        this.equipoServices = equipoServices;
        this.partidoRepository = partidoRepository;
    }

    @Transactional
    public Carrera crearCarreraParaUsuario(Long usuarioId, String nombreDt, String apellidoDt,
                                           Integer edadDt, String provincia, Integer numeroAvatar,
                                           Long equipoInicialId) {
        Usuario usuario = usuarioServices.buscarCuentaActiva(usuarioId);
        if (carreraRepository.findByUsuarioUsuarioIdAndFechaEliminacionIsNull(usuarioId).isPresent()) {
            throw new ReglaNegocioException("El usuario ya tiene una carrera activa");
        }
        validarPerfilDt(nombreDt, apellidoDt, edadDt, provincia, numeroAvatar);
        var equipo = equipoServices.obtenerEquipoPorId(equipoInicialId);
        equipoServices.validarEquipoDisponibleParaDirectorTecnico(equipo, 1);
        Carrera carrera = new Carrera();
        carrera.setUsuario(usuario);
        carrera.setEquipoActual(equipo);
        carrera.setNombreDt(nombreDt.trim());
        carrera.setApellidoDt(apellidoDt.trim());
        carrera.setEdadDt(edadDt);
        carrera.setProvincia(provincia.trim());
        carrera.setNumeroAvatar(numeroAvatar);
        carrera.setExperiencia(0L);
        carrera.setEstrellas(1);
        carrera.setNumeroTemporada(1);
        carrera.setObjetivoContrato("Clasificar a copas");
        carrera.setEstadoContrato("activo");
        return carreraRepository.save(carrera);
    }

    public Carrera obtenerCarreraActivaPorUsuario(Long usuarioId) {
        usuarioServices.buscarCuentaActiva(usuarioId);
        return carreraRepository.findByUsuarioUsuarioIdAndFechaEliminacionIsNull(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario todavía no tiene una carrera activa"));
    }

    @Transactional
    public Carrera cambiarEquipoActual(Long carreraId, Long nuevoEquipoId) {
        Carrera carrera = buscarCarrera(carreraId);
        var equipo = equipoServices.obtenerEquipoPorId(nuevoEquipoId);
        equipoServices.validarEquipoDisponibleParaDirectorTecnico(equipo, carrera.getEstrellas());
        carrera.setEquipoActual(equipo);
        return carreraRepository.save(carrera);
    }

    @Transactional
    public Carrera aplicarExperienciaPorResultado(Long partidoId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el partido"));
        if (!"finalizado".equals(partido.getEstado())) throw new ReglaNegocioException("Solo se aplica experiencia al finalizar el partido");
        if (partido.getVariacionExperiencia() != null && partido.getVariacionExperiencia() != 0) {
            throw new ReglaNegocioException("La experiencia de este partido ya fue aplicada");
        }
        Carrera carrera = buscarCarrera(partido.getCarrera().getCarreraId());
        String resultado = calcularResultado(partido);
        long experienciaAnterior = carrera.getExperiencia() == null ? 0L : carrera.getExperiencia();
        int deltaSolicitado = calcularVariacionDeExperienciaPorResultado(resultado);
        long experienciaNueva = Math.max(0L, experienciaAnterior + deltaSolicitado);
        int deltaAplicado = Math.toIntExact(experienciaNueva - experienciaAnterior);
        carrera.setExperiencia(experienciaNueva);
        carrera.setEstrellas(calcularEstrellasSegunExperiencia(experienciaNueva));
        partido.setVariacionExperiencia(deltaAplicado);
        carreraRepository.save(carrera);
        partidoRepository.save(partido);
        return carrera;
    }

    void validarQueLaCarreraPertenezcaAlUsuario(Long carreraId, Long usuarioId) {
        Carrera carrera = buscarCarrera(carreraId);
        if (!carrera.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new ReglaNegocioException("No tenés acceso a esta carrera");
        }
    }

    Carrera buscarCarrera(Long carreraId) {
        if (carreraId == null) throw new ReglaNegocioException("El ID de carrera es obligatorio");
        return carreraRepository.findByCarreraIdAndFechaEliminacionIsNull(carreraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la carrera activa"));
    }

    private void validarPerfilDt(String nombre, String apellido, Integer edad, String provincia, Integer avatar) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 60 || apellido == null || apellido.isBlank() || apellido.length() > 60
                || provincia == null || provincia.isBlank() || provincia.length() > 80) {
            throw new ReglaNegocioException("Completá nombre, apellido y provincia con valores válidos");
        }
        if (edad == null || edad < 16 || edad > 100) throw new ReglaNegocioException("La edad debe estar entre 16 y 100");
        if (avatar == null || avatar < 1 || avatar > 6) throw new ReglaNegocioException("El avatar debe estar entre 1 y 6");
    }

    private int calcularVariacionDeExperienciaPorResultado(String resultado) {
        return switch (resultado) {
            case "VICTORIA" -> 100;
            case "EMPATE" -> 50;
            case "DERROTA" -> -50;
            default -> throw new ReglaNegocioException("El resultado del partido no es válido");
        };
    }

    private Integer calcularEstrellasSegunExperiencia(Long experiencia) {
        // Curva inicial adoptada: 1★ desde 0 XP; 2★ desde 500; 3★ desde 1.500; 4★ desde 3.000; 5★ desde 5.000.
        if (experiencia >= 5_000) return 5;
        if (experiencia >= 3_000) return 4;
        if (experiencia >= 1_500) return 3;
        if (experiencia >= 500) return 2;
        return 1;
    }

    private String calcularResultado(Partido partido) {
        if (partido.getGolesLocal() > partido.getGolesVisitante()) return "VICTORIA";
        if (partido.getGolesLocal().equals(partido.getGolesVisitante())) return "EMPATE";
        return "DERROTA";
    }
}
