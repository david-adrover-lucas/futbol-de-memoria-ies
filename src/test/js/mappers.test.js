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
