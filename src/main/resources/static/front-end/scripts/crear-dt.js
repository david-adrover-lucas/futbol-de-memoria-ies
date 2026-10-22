document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;

    const avatarCards = [...document.querySelectorAll(".avatar-card")];
    const form = document.getElementById("form-registro-dt");
    let avatarSeleccionado = null;

    try {
        const carrera = await window.FDM.services.carreraService.obtener(session.usuarioId);
        window.FDM.session.set({ ...session, carreraId: carrera.carreraId, carrera });
        window.location.replace("menu.html");
        return;
    } catch (error) {
        if (error.status !== 404) {
            mostrarError(error.message);
            return;
        }
    }

    avatarCards.forEach((card) => card.addEventListener("click", () => seleccionarAvatar(card)));
    document.getElementById("btn-back").addEventListener("click", () => window.location.assign("../index.html"));
    document.getElementById("btn-random").addEventListener("click", () => {
        seleccionarAvatar(avatarCards[Math.floor(Math.random() * avatarCards.length)]);
    });

    form.addEventListener("submit", (event) => {
        event.preventDefault();
        if (!avatarSeleccionado) return mostrarError("Elegí un avatar para tu DT antes de continuar.");
        if (!form.reportValidity()) return;

        const provincia = document.getElementById("dt-province");
        const draft = {
            nombreDt: document.getElementById("dt-name").value.trim(),
            apellidoDt: document.getElementById("dt-lastname").value.trim(),
            edadDt: Number(document.getElementById("dt-age").value),
            provincia: provincia.options[provincia.selectedIndex].text,
            numeroAvatar: Number(avatarSeleccionado)
        };
        window.FDM.session.set({ ...session, perfilDtPendiente: draft });
        window.location.assign("elegir-club.html");
    });

    function seleccionarAvatar(card) {
        avatarCards.forEach((item) => item.classList.remove("selected"));
        card.classList.add("selected");
        avatarSeleccionado = card.dataset.avatar;
    }

    function mostrarError(message) { alert(message); }
});
