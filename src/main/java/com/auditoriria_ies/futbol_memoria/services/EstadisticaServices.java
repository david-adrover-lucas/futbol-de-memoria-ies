package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.Carrera;
import com.auditoriria_ies.futbol_memoria.models.Partido;
import com.auditoriria_ies.futbol_memoria.repository.CarreraRepository;
import com.auditoriria_ies.futbol_memoria.repository.PartidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class EstadisticaServices {
    private final CarreraServices carreraServices;
    private final CarreraRepository carreraRepository;
    private final PartidoRepository partidoRepository;

    public EstadisticaServices(CarreraServices carreraServices, CarreraRepository carreraRepository, PartidoRepository partidoRepository) {
        this.carreraServices = carreraServices;
        this.carreraRepository = carreraRepository;
        this.partidoRepository = partidoRepository;
    }

    public Map<String, Long> obtenerEstadisticasDeCarrera(Long usuarioId) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        List<Partido> partidos = partidoRepository.findByCarreraCarreraIdOrderByIniciadoEnDesc(carrera.getCarreraId()).stream()
                .filter(p -> "finalizado".equals(p.getEstado())).toList();
        return calcularEstadisticasDesdePartidos(partidos);
    }

    public List<Partido> obtenerHistorialDePartidos(Long usuarioId, Long competicionId) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        return partidoRepository.findByCarreraCarreraIdOrderByIniciadoEnDesc(carrera.getCarreraId()).stream()
                .filter(p -> competicionId == null || p.getCompeticion().getCompeticionId().equals(competicionId))
                .toList();
    }

    public Page<Carrera> listarRankingGlobal(int pagina, int tamano) {
        validarPaginacion(pagina, tamano);
        return carreraRepository.findByFechaEliminacionIsNullAndUsuarioFechaEliminacionIsNull(PageRequest.of(pagina, tamano,
                Sort.by(Sort.Order.desc("experiencia"), Sort.Order.asc("carreraId"))));
    }

    public long calcularPosicionDeCarrera(Long carreraId) {
        List<Carrera> ranking = carreraRepository.findByFechaEliminacionIsNullAndUsuarioFechaEliminacionIsNullOrderByExperienciaDescCarreraIdAsc();
        for (int i = 0; i < ranking.size(); i++) if (ranking.get(i).getCarreraId().equals(carreraId)) return i + 1L;
        throw new RecursoNoEncontradoException("No se encontró la carrera en el ranking");
    }

    private Map<String, Long> calcularEstadisticasDesdePartidos(List<Partido> partidos) {
        long ganados = 0, empatados = 0, perdidos = 0, golesFavor = 0, golesContra = 0;
        for (Partido partido : partidos) {
            int gl = partido.getGolesLocal();
            int gv = partido.getGolesVisitante();
            if (gl > gv) ganados++;
            else if (gl == gv) empatados++;
            else perdidos++;
            golesFavor += gl;
            golesContra += gv;
        }
        Map<String, Long> resultado = new HashMap<>();
        resultado.put("jugados", (long) partidos.size());
        resultado.put("ganados", ganados);
        resultado.put("empatados", empatados);
        resultado.put("perdidos", perdidos);
        resultado.put("golesFavor", golesFavor);
        resultado.put("golesContra", golesContra);
        return Map.copyOf(resultado);
    }

    private void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 100) throw new ReglaNegocioException("La paginación solicitada no es válida");
    }
}
