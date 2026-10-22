const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");

const source = fs.readFileSync("src/main/resources/static/front-end/scripts/core/mappers.js", "utf8");
const window = {};
vm.runInNewContext(source, { window, Number, Boolean });

test("mapper de carrera convierte campos de JPA y anida el equipo con valores seguros", () => {
    const model = window.FDM.mappers.carrera({
        carreraId: 4, nombreDt: "Ana", experiencia: 500, estrellas: 2,
        equipoActual: { equipoId: 7, nombre: "Club", prestigio: 2, sueldo: "960000.00" }
    });
    assert.equal(model.carreraId, 4);
    assert.equal(model.experiencia, 500);
    assert.equal(model.equipoActual.equipoId, 7);
    assert.equal(model.equipoActual.sueldo, 960000);
    assert.equal(model.apellidoDt, "");
});

test("mapper de partido separa equipos y normaliza marcador", () => {
    const model = window.FDM.mappers.partido({
        partidoId: 2, golesLocal: null, golesVisitante: 1,
        equipoLocal: { equipoId: 1, nombre: "Local" }, equipoVisitante: { equipoId: 2, nombre: "Visita" }
    });
    assert.equal(model.golesLocal, 0);
    assert.equal(model.golesVisitante, 1);
    assert.equal(model.equipoLocal.nombre, "Local");
});

test("mapper de tanda de penales normaliza marcador, pregunta y turnos", () => {
    const tanda = window.FDM.mappers.tandaPenales({
        partidoId: "35", estado: "en_curso", golesJugador: "2", golesRival: 1,
        preguntaActual: { preguntaId: "17", opciones: [{ opcionRespuestaId: "4", ordenOpcion: "1", textoOpcion: "A" }] },
        turnos: [{ numeroTurno: "3", rolJugador: "patea" }]
    });
    assert.equal(tanda.partidoId, 35);
    assert.equal(tanda.golesJugador, 2);
    assert.equal(tanda.preguntaActual.preguntaId, 17);
    assert.equal(tanda.preguntaActual.opciones[0].opcionRespuestaId, 4);
    assert.equal(tanda.turnos[0].numeroTurno, 3);
});

test("mapper de torneo conserva el estado de consulta y normaliza el campeon", () => {
    const value = window.FDM.mappers.torneo({
        torneoId: null, codigoCompeticion: "libertadores", numeroTemporada: 2,
        rondaActual: 4, estado: "eliminado",
        campeon: { equipoId: 12, nombre: "River Plate", prestigio: 5 }, fixtures: []
    });
    assert.equal(value.torneoId, null);
    assert.equal(value.estado, "eliminado");
    assert.equal(value.campeon.equipoId, 12);
    assert.equal(value.campeon.nombre, "River Plate");
});
