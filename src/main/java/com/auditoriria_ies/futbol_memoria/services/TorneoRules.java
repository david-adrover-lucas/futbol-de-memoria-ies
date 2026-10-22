package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.models.Equipo;

import java.util.Objects;

public final class TorneoRules {
    private TorneoRules() {}

    public static Equipo ganador(Equipo local, Equipo visitante, Integer golesLocal, Integer golesVisitante,
                                 Equipo ganadorPenales) {
        if (local == null || visitante == null || golesLocal == null || golesVisitante == null) return null;
        if (golesLocal > golesVisitante) return local;
        if (golesVisitante > golesLocal) return visitante;
        if (ganadorPenales == null) return null;
        if (Objects.equals(ganadorPenales.getEquipoId(), local.getEquipoId())) return local;
        if (Objects.equals(ganadorPenales.getEquipoId(), visitante.getEquipoId())) return visitante;
        return null;
    }
}
