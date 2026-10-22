package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.models.Equipo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TorneoRulesTest {
    private final Equipo local = equipo(1L);
    private final Equipo visitante = equipo(2L);

    @Test
    void resuelveElEmpateConElGanadorGuardadoEnLaTanda() {
        assertSame(local, TorneoRules.ganador(local, visitante, 1, 1, local));
        assertSame(visitante, TorneoRules.ganador(local, visitante, 1, 1, visitante));
    }

    @Test
    void noInventaGanadorSiElEmpateNoTieneTandaRegistrada() {
        assertNull(TorneoRules.ganador(local, visitante, 0, 0, null));
    }

    @Test
    void elMarcadorNormalDeterminaGanadorSinDependerDePenales() {
        assertSame(visitante, TorneoRules.ganador(local, visitante, 1, 2, local));
    }

    private Equipo equipo(Long id) {
        Equipo equipo = new Equipo();
        equipo.setEquipoId(id);
        return equipo;
    }
}
