package com.auditoriria_ies.futbol_memoria.services;

import java.util.ArrayList;
import java.util.List;

public final class ProgramacionTemporada {
    public record Evento(int fecha, String competicion, int ronda) {}
    public static final int TOTAL_FECHAS = 41;
    private static final List<Evento> EVENTOS = construir();

    private ProgramacionTemporada() {}

    public static List<Evento> eventos() { return EVENTOS; }

    private static List<Evento> construir() {
        List<Evento> eventos = new ArrayList<>();
        int jornada = 0;
        for (int fecha = 1; fecha <= TOTAL_FECHAS; fecha++) {
            if (fecha == 3) eventos.add(new Evento(fecha, "nacional", 1));
            else if (fecha == 6) eventos.add(new Evento(fecha, "libertadores", 1));
            else if (fecha == 9) eventos.add(new Evento(fecha, "sudamericana", 1));
            else if (fecha == 12) eventos.add(new Evento(fecha, "nacional", 2));
            else if (fecha == 15) eventos.add(new Evento(fecha, "libertadores", 2));
            else if (fecha == 18) eventos.add(new Evento(fecha, "sudamericana", 2));
            else if (fecha == 21) eventos.add(new Evento(fecha, "nacional", 3));
            else if (fecha == 24) eventos.add(new Evento(fecha, "libertadores", 3));
            else if (fecha == 27) eventos.add(new Evento(fecha, "sudamericana", 3));
            else if (fecha == 30) eventos.add(new Evento(fecha, "nacional", 4));
            else if (fecha == 33) eventos.add(new Evento(fecha, "libertadores", 4));
            else if (fecha == 36) eventos.add(new Evento(fecha, "sudamericana", 4));
            else eventos.add(new Evento(fecha, "temporada", ++jornada));
        }
        return List.copyOf(eventos);
    }
}
