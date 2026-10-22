package com.auditoriria_ies.futbol_memoria.services;

import org.springframework.stereotype.Component;

import java.util.function.IntSupplier;

@Component
public class ClasificadorTemporada {
    private static final int PRESTIGIO_MINIMO_EQUIPO = 1;
    private static final int PRESTIGIO_MAXIMO_EQUIPO = 5;
    private static final int TIRADA_MINIMA = 1;
    private static final int TIRADA_MAXIMA = 100;

    public record Decision(String codigoCompeticion, String metodo, Integer tiradaLibertadores,
                           Integer tiradaSudamericana) {}

    public Decision decidir(int prestigioEquipo, int posicionFinal, ClasificacionProperties reglas,
                             IntSupplier generarTirada) {
        if (prestigioEquipo < PRESTIGIO_MINIMO_EQUIPO || prestigioEquipo > PRESTIGIO_MAXIMO_EQUIPO) throw new IllegalArgumentException("El prestigio debe estar entre 1 y 5");
        if (posicionFinal < 1) throw new IllegalArgumentException("La posición final debe ser positiva");

        if (posicionFinal <= reglas.puestoMaximoLibertadores()) {
            return new Decision("libertadores", "tabla", null, null);
        }
        if (prestigioEquipo <= reglas.prestigioMaximoPorTabla()) {
            if (posicionFinal <= reglas.puestoMaximoSudamericana()) {
                return new Decision("sudamericana", "tabla", null, null);
            }
            return new Decision(null, "sin_cupo", null, null);
        }

        int tiradaLibertadores = generarTirada.getAsInt();
        validarTirada(tiradaLibertadores);
        if (tiradaLibertadores <= reglas.probabilidadLibertadores(prestigioEquipo)) {
            return new Decision("libertadores", "sorteo", tiradaLibertadores, null);
        }
        if (posicionFinal <= reglas.puestoMaximoSudamericana()) {
            return new Decision("sudamericana", "tabla", tiradaLibertadores, null);
        }

        int tiradaSudamericana = generarTirada.getAsInt();
        validarTirada(tiradaSudamericana);
        if (tiradaSudamericana <= reglas.probabilidadSudamericana(prestigioEquipo)) {
            return new Decision("sudamericana", "sorteo", tiradaLibertadores, tiradaSudamericana);
        }
        return new Decision(null, "sin_cupo", tiradaLibertadores, tiradaSudamericana);
    }

    private void validarTirada(int tirada) {
        if (tirada < TIRADA_MINIMA || tirada > TIRADA_MAXIMA) throw new IllegalArgumentException("La tirada debe estar entre 1 y 100");
    }
}
