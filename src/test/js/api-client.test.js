const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");

const source = fs.readFileSync("src/main/resources/static/front-end/scripts/core/api-client.js", "utf8");

function createApi(fetchImpl) {
    const window = { setTimeout, clearTimeout };
    const context = { window, document: { querySelector: () => null }, Headers, AbortController, fetch: fetchImpl };
    vm.runInNewContext(source, context);
    return window.FDM.api;
}

test("cliente central envía JSON y devuelve respuestas normalizadas", async () => {
    let captured;
    const api = createApi(async (url, options) => {
        captured = { url, options };
        return new Response(JSON.stringify({ usuarioId: 12 }), { status: 200, headers: { "content-type": "application/json" } });
    });
    const data = await api.request("/auth/login", { method: "POST", body: { nombreUsuario: "dt", contrasena: "clave" } });
    assert.equal(captured.url, "/api/auth/login");
    assert.equal(captured.options.headers.get("Content-Type"), "application/json");
    assert.deepEqual(JSON.parse(captured.options.body), { nombreUsuario: "dt", contrasena: "clave" });
    assert.equal(data.usuarioId, 12);
});

test("cliente conserva el mensaje legible de errores del backend", async () => {
    const api = createApi(async () => new Response(JSON.stringify({ estado: "Bad Request", mensaje: "La contraseña no coincide" }), {
        status: 400, headers: { "content-type": "application/json" }
    }));
    await assert.rejects(api.request("/auth/login", { method: "POST", body: {} }), (error) => {
        assert.equal(error.status, 400);
        assert.equal(error.message, "La contraseña no coincide");
        return true;
    });
});
