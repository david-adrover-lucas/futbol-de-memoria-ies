(function () {
    const TIMEOUT_MS = 10000;
    const baseUrl = document.querySelector('meta[name="api-base-url"]')?.content || "/api";

    class ApiError extends Error {
        constructor(status, message, details) {
            super(message || "No se pudo completar la solicitud.");
            this.name = "ApiError";
            this.status = status;
            this.details = details;
        }
    }

    async function request(path, options = {}) {
        const controller = new AbortController();
        const timer = window.setTimeout(() => controller.abort(), TIMEOUT_MS);
        const headers = new Headers(options.headers || {});
        headers.set("Accept", "application/json");
        if (options.body !== undefined) headers.set("Content-Type", "application/json");

        try {
            const response = await fetch(`${baseUrl}${path}`, {
                ...options,
                headers,
                signal: controller.signal,
                body: options.body === undefined ? undefined : JSON.stringify(options.body)
            });
            const contentType = response.headers.get("content-type") || "";
            const data = response.status === 204 ? null
                : contentType.includes("application/json") ? await response.json() : await response.text();

            if (!response.ok) {
                const message = typeof data === "object" && data
                    ? data.mensaje || data.message || data.error
                    : data;
                throw new ApiError(response.status, message || mensajeHttp(response.status), data);
            }
            return data;
        } catch (error) {
            if (error.name === "AbortError") throw new ApiError(0, "La solicitud tardó demasiado. Intenta nuevamente.");
            if (error instanceof ApiError) throw error;
            throw new ApiError(0, "No hay conexión con el servidor. Comprueba que Spring Boot esté iniciado.", error);
        } finally {
            window.clearTimeout(timer);
        }
    }

    function mensajeHttp(status) {
        if (status === 400 || status === 422) return "Revisa los datos ingresados.";
        if (status === 401 || status === 403) return "La sesión no está disponible para esta operación.";
        if (status === 404) return "No se encontró el recurso solicitado.";
        if (status === 409) return "La operación entra en conflicto con los datos existentes.";
        return status >= 500 ? "El servidor tuvo un problema. Intenta nuevamente." : "No se pudo completar la solicitud.";
    }

    window.FDM = window.FDM || {};
    window.FDM.api = { request, ApiError };
})();
