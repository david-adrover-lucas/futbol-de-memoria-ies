(function () {
    const KEY = "fdmSession";

    function get() {
        try { return JSON.parse(sessionStorage.getItem(KEY) || "null"); }
        catch (_) { return null; }
    }

    function set(session) {
        sessionStorage.setItem(KEY, JSON.stringify(session));
        return session;
    }

    function clear() { sessionStorage.removeItem(KEY); }

    function requireUser() {
        const session = get();
        if (!session?.usuarioId) {
            window.location.replace("../index.html");
            return null;
        }
        return session;
    }

    window.FDM = window.FDM || {};
    window.FDM.session = { get, set, clear, requireUser };
})();
