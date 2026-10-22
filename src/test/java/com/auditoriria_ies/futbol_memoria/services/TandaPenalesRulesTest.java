package com.auditoriria_ies.futbol_memoria.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TandaPenalesRulesTest {
    @Test
    void losDiezTurnosRegularesAlternanYElJugadorPateaPrimero() {
        assertEquals("patea", TandaPenalesRules.rolJugador(1));
        assertEquals("ataja", TandaPenalesRules.rolJugador(2));
        assertEquals("regular", TandaPenalesRules.fase(10));
        assertEquals(5, TandaPenalesRules.numeroRonda(10));
        assertFalse(TandaPenalesRules.tieneGanadorTrasTurno(8, 4, 3));
        assertTrue(TandaPenalesRules.tieneGanadorTrasTurno(10, 5, 4));
    }

    @Test
    void laMuerteSubitaSoloSeDecideAlCerrarCadaParejaDeTurnos() {
        assertEquals("patea", TandaPenalesRules.rolJugador(11));
        assertEquals("ataja", TandaPenalesRules.rolJugador(12));
        assertEquals("muerte_subita", TandaPenalesRules.fase(11));
        assertEquals(1, TandaPenalesRules.numeroRonda(11));
        assertFalse(TandaPenalesRules.tieneGanadorTrasTurno(11, 6, 5));
        assertTrue(TandaPenalesRules.tieneGanadorTrasTurno(12, 6, 5));
        assertFalse(TandaPenalesRules.tieneGanadorTrasTurno(12, 6, 6));
    }
}
