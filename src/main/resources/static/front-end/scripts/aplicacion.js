document.addEventListener("DOMContentLoaded", () => {
    const splash = document.getElementById("pantalla-inicio");
    const loginScreen = document.getElementById("pantalla-acceso");
    const loginForm = document.getElementById("formulario-acceso");
    const loginButton = document.getElementById("boton-acceso");
    const registerButton = document.getElementById("boton-modo-registro");
    const email = document.getElementById("correo-electronico");
    let registering = false;

    window.setTimeout(() => {
        if (splash && loginScreen) {
            splash.classList.add("oculto");
            loginScreen.classList.remove("oculto");
        }
    }, 700);

    registerButton?.addEventListener("click", () => {
        registering = !registering;
        email.classList.toggle("oculto", !registering);
        email.required = registering;
        loginButton.textContent = registering ? "REGISTRAR CUENTA" : "LOGIN";
        registerButton.textContent = registering ? "VOLVER AL LOGIN" : "CREAR CUENTA";
    });

    loginForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        loginButton.disabled = true;
        const originalText = loginButton.textContent;
        loginButton.textContent = registering ? "REGISTRANDO..." : "CONECTANDO...";

        try {
            const username = document.getElementById("nombre-usuario").value.trim();
            const password = document.getElementById("contrasena").value;
            const response = registering
                ? await window.FDM.services.authService.register(username, email.value.trim(), password)
                : await window.FDM.services.authService.login(username, password);
            window.FDM.session.set({ usuarioId: response.usuarioId, nombreUsuario: username });
            window.location.href = "pantallas/crear-dt.html";
        } catch (error) {
            alert(error.message);
        } finally {
            loginButton.disabled = false;
            loginButton.textContent = originalText;
        }
    });
});
