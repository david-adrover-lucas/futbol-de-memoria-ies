package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.NivelDificultad;
import com.auditoriria_ies.futbol_memoria.repository.NivelDificultadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NivelDificultadServices {
    private final NivelDificultadRepository nivelRepository;

    public NivelDificultadServices(NivelDificultadRepository nivelRepository) {
        this.nivelRepository = nivelRepository;
    }

    public List<NivelDificultad> listarNivelesDeDificultad() {
        return nivelRepository.findAll().stream().sorted(Comparator.comparing(NivelDificultad::getNumeroNivel))
                .toList();
    }

    public NivelDificultad obtenerNivelDeDificultadPorNumero(Integer numeroNivel) {
        validarNumeroDeNivel(numeroNivel);
        return obtenerEntidadPorNumero(numeroNivel);
    }

    NivelDificultad obtenerEntidadPorNumero(Integer numeroNivel) {
        validarNumeroDeNivel(numeroNivel);
        return nivelRepository.findByNumeroNivel(numeroNivel)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el nivel de dificultad solicitado"));
    }

    int calcularNivelMaximoDePregunta(Integer estrellasRival, Integer modificadorCompeticion) {
        if (estrellasRival == null || estrellasRival < 1 || estrellasRival > 5 || modificadorCompeticion == null || modificadorCompeticion < 0) {
            throw new ReglaNegocioException("Los datos para calcular la dificultad no son válidos");
        }
        return Math.min(5, estrellasRival + modificadorCompeticion);
    }

    private void validarNumeroDeNivel(Integer numeroNivel) {
        if (numeroNivel == null || numeroNivel < 1 || numeroNivel > 5) {
            throw new ReglaNegocioException("El nivel debe estar entre 1 y 5");
        }
    }

}
