package com.auditoriria_ies.futbol_memoria.services;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramacionTemporadaTest {
    @Test
    void programaCuarentaYUnaFechasConVeintinueveJornadasYCuatroRondasPorCopa() {
        List<ProgramacionTemporada.Evento> eventos = ProgramacionTemporada.eventos();

        assertEquals(41, eventos.size());
        assertEquals(29, eventos.stream().filter(e -> "temporada".equals(e.competicion())).count());
        assertEquals(4, eventos.stream().filter(e -> "nacional".equals(e.competicion())).count());
        assertEquals(4, eventos.stream().filter(e -> "libertadores".equals(e.competicion())).count());
        assertEquals(4, eventos.stream().filter(e -> "sudamericana".equals(e.competicion())).count());
        assertTrue(eventos.stream().allMatch(e -> e.fecha() >= 1 && e.fecha() <= 41));
        assertEquals(List.of(3, 12, 21, 30), fechasDe(eventos, "nacional"));
        assertEquals(List.of(6, 15, 24, 33), fechasDe(eventos, "libertadores"));
        assertEquals(List.of(9, 18, 27, 36), fechasDe(eventos, "sudamericana"));
    }

    private List<Integer> fechasDe(List<ProgramacionTemporada.Evento> eventos, String codigo) {
        return eventos.stream().filter(evento -> codigo.equals(evento.competicion()))
                .map(ProgramacionTemporada.Evento::fecha).toList();
    }
}
