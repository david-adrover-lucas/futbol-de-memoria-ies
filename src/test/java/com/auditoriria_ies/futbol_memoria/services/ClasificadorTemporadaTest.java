package com.auditoriria_ies.futbol_memoria.services;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClasificadorTemporadaTest {
    private final ClasificadorTemporada clasificador = new ClasificadorTemporada();
    private final ClasificacionProperties reglas = new ClasificacionProperties(3, 6, 12, 45, 65, 80, 80);

    @Test
    void aplicaLosCortesDeTablaSinSorteoAEquiposDeHastaTresEstrellas() {
        assertEquals("libertadores", decidir(3, 1).codigoCompeticion());
        assertEquals("libertadores", decidir(3, 6).codigoCompeticion());
        assertEquals("sudamericana", decidir(2, 7).codigoCompeticion());
        assertEquals("sudamericana", decidir(1, 12).codigoCompeticion());
        assertEquals("sin_cupo", decidir(3, 13).metodo());
    }

    @Test
    void unaTiradaExitosaMejoraAUnEquipoDeCuatroEstrellasALibertadores() {
        AtomicInteger cantidadTiradas = new AtomicInteger();
        var decision = clasificador.decidir(4, 9, reglas, () -> {
            cantidadTiradas.incrementAndGet();
            return 45;
        });

        assertEquals("libertadores", decision.codigoCompeticion());
        assertEquals("sorteo", decision.metodo());
        assertEquals(45, decision.tiradaLibertadores());
        assertEquals(1, cantidadTiradas.get());
    }

    @Test
    void siFallaLaTiradaDeLibertadoresConservaSudamericanaPorTabla() {
        var decision = clasificador.decidir(4, 9, reglas, () -> 46);

        assertEquals("sudamericana", decision.codigoCompeticion());
        assertEquals("tabla", decision.metodo());
        assertEquals(46, decision.tiradaLibertadores());
    }

    @Test
    void fueraDeLosPuestosDeCopaAplicaLasDosProbabilidadesEnOrden() {
        AtomicInteger indice = new AtomicInteger();
        int[] valores = {81, 80};
        var decision = clasificador.decidir(5, 13, reglas, () -> valores[indice.getAndIncrement()]);

        assertEquals("sudamericana", decision.codigoCompeticion());
        assertEquals("sorteo", decision.metodo());
        assertEquals(81, decision.tiradaLibertadores());
        assertEquals(80, decision.tiradaSudamericana());
        assertEquals(2, indice.get());
    }

    @Test
    void unEquipoDeCincoEstrellasPuedeClasificarDirectoALibertadores() {
        AtomicInteger indice = new AtomicInteger();
        int[] valores = {80};
        var decision = clasificador.decidir(5, 25, reglas, () -> valores[indice.getAndIncrement()]);

        assertEquals("libertadores", decision.codigoCompeticion());
        assertEquals(80, decision.tiradaLibertadores());
        assertEquals(1, indice.get());
    }

    private ClasificadorTemporada.Decision decidir(int prestigio, int posicion) {
        AtomicReference<Boolean> generoTirada = new AtomicReference<>(false);
        var decision = clasificador.decidir(prestigio, posicion, reglas, () -> {
            generoTirada.set(true);
            return 1;
        });
        assertFalse(generoTirada.get());
        return decision;
    }

}
