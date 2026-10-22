package com.auditoriria_ies.futbol_memoria.services;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "juego.clasificacion")
public record ClasificacionProperties(
        int prestigioMaximoPorTabla,
        int puestoMaximoLibertadores,
        int puestoMaximoSudamericana,
        int probabilidadLibertadores4,
        int probabilidadSudamericana4,
        int probabilidadLibertadores5,
        int probabilidadSudamericana5) {
    public static final int PRESTIGIO_EQUIPO_4_ESTRELLAS = 4;
    public static final int PRESTIGIO_EQUIPO_5_ESTRELLAS = 5;

    public ClasificacionProperties {
        validarPorcentaje(probabilidadLibertadores4, "probabilidad-libertadores-4");
        validarPorcentaje(probabilidadSudamericana4, "probabilidad-sudamericana-4");
        validarPorcentaje(probabilidadLibertadores5, "probabilidad-libertadores-5");
        validarPorcentaje(probabilidadSudamericana5, "probabilidad-sudamericana-5");
        if (prestigioMaximoPorTabla < 1 || puestoMaximoLibertadores < 1
                || puestoMaximoSudamericana < puestoMaximoLibertadores) {
            throw new IllegalArgumentException("La configuración de clasificación no es válida");
        }
    }

    public int probabilidadLibertadores(int prestigio) {
        return switch (prestigio) {
            case PRESTIGIO_EQUIPO_4_ESTRELLAS -> probabilidadLibertadores4;
            case PRESTIGIO_EQUIPO_5_ESTRELLAS -> probabilidadLibertadores5;
            default -> 0;
        };
    }

    public int probabilidadSudamericana(int prestigio) {
        return switch (prestigio) {
            case PRESTIGIO_EQUIPO_4_ESTRELLAS -> probabilidadSudamericana4;
            case PRESTIGIO_EQUIPO_5_ESTRELLAS -> probabilidadSudamericana5;
            default -> 0;
        };
    }

    private static void validarPorcentaje(int valor, String nombre) {
        if (valor < 0 || valor > 100) throw new IllegalArgumentException(nombre + " debe estar entre 0 y 100");
    }
}
