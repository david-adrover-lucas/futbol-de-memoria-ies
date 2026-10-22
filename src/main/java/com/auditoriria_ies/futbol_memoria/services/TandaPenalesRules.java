package com.auditoriria_ies.futbol_memoria.services;

import java.util.Objects;

final class TandaPenalesRules {
    static final int TURNOS_REGLAMENTARIOS = 10;

    private TandaPenalesRules() {}

    static String rolJugador(int numeroTurno) {
        return numeroTurno % 2 == 1 ? "patea" : "ataja";
    }

    static String fase(int numeroTurno) {
        return numeroTurno <= TURNOS_REGLAMENTARIOS ? "regular" : "muerte_subita";
    }

    static int numeroRonda(int numeroTurno) {
        return numeroTurno <= TURNOS_REGLAMENTARIOS ? (numeroTurno + 1) / 2 : ((numeroTurno - 11) / 2) + 1;
    }

    static boolean tieneGanadorTrasTurno(int numeroTurnoRespondido, int golesJugador, int golesRival) {
        if (numeroTurnoRespondido < TURNOS_REGLAMENTARIOS) return false;
        boolean rondaCompleta = numeroTurnoRespondido == TURNOS_REGLAMENTARIOS
                || (numeroTurnoRespondido > TURNOS_REGLAMENTARIOS && numeroTurnoRespondido % 2 == 0);
        return rondaCompleta && !Objects.equals(golesJugador, golesRival);
    }
}
