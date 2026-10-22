const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");

const mapperSource = fs.readFileSync("src/main/resources/static/front-end/scripts/core/mappers.js", "utf8");
const serviceSource = fs.readFileSync("src/main/resources/static/front-end/scripts/core/services.js", "utf8");

function createServices() {
    const calls = [];
    const window = { FDM: { api: { request: async (...args) => {
        calls.push(args);
        const [path] = args;
        if (path.endsWith("/carrera")) return { carreraId: 8, experiencia: "1500", estrellas: 3 };
        return { partidoId: 9, golesLocal: 1, golesVisitante: 0 };
    } } } };
    vm.runInNewContext(mapperSource, { window, Number, Boolean });
    vm.runInNewContext(serviceSource, { window, URLSearchParams, encodeURIComponent });
    return { services: window.FDM.services, calls };
}

test("servicio de carrera devuelve el modelo normalizado", async () => {
    const { services, calls } = createServices();
    const carrera = await services.carreraService.obtener(11);
    assert.equal(calls[0][0], "/usuarios/11/carrera");
    assert.equal(carrera.carreraId, 8);
    assert.equal(carrera.experiencia, 1500);
    assert.equal(carrera.estrellas, 3);
});

test("servicio de partido envía el fixture y normaliza el resultado", async () => {
    const { services, calls } = createServices();
    const game = await services.partidoService.iniciar(11, "temporada", { temporadaFixtureId: 22 });
    assert.equal(calls[0][0], "/usuarios/11/partidos");
    assert.deepEqual(JSON.parse(JSON.stringify(calls[0][1].body)), { codigoCompeticion: "temporada", temporadaFixtureId: 22 });
    assert.equal(game.partidoId, 9);
    assert.equal(game.golesLocal, 1);
});

test("servicio de penales usa los endpoints del backend y envía la respuesta vigente", async () => {
    const { services, calls } = createServices();
    await services.tandaPenalesService.iniciar(11, 35);
    await services.tandaPenalesService.responder(11, 35, { preguntaId: 17, opcionRespuestaId: 2, segundos: 4 });
    assert.equal(calls[0][0], "/usuarios/11/partidos/35/penales");
    assert.equal(calls[0][1].method, "POST");
    assert.equal(calls[1][0], "/usuarios/11/partidos/35/penales/respuestas");
    assert.deepEqual(JSON.parse(JSON.stringify(calls[1][1].body)), { preguntaId: 17, opcionRespuestaId: 2, segundos: 4 });
});

test("calendarioService consulta y avanza una fecha mediante la API", async () => {
    const { services, calls } = createServices();
    await services.calendarioService.actual(7);
    await services.calendarioService.avanzar(7);
    assert.equal(calls[0][0], "/usuarios/7/calendario");
    assert.equal(calls[1][0], "/usuarios/7/calendario/avanzar");
    assert.equal(calls[1][1].method, "POST");
});
