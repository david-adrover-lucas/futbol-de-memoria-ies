document.addEventListener("DOMContentLoaded", () => {
    const splash = document.getElementById("splash-screen");
    const loginScreen = document.getElementById("login-screen");
    const loginForm = document.getElementById("login-form");
    const loginButton = document.getElementById("login-btn");
    const registerButton = document.getElementById("register-mode-btn");
    const email = document.getElementById("email");
    let registering = false;

    window.setTimeout(() => {
        if (splash && loginScreen) {
            splash.classList.add("hidden");
            loginScreen.classList.remove("hidden");
        }
    }, 700);

    registerButton?.addEventListener("click", () => {
        registering = !registering;
        email.classList.toggle("hidden", !registering);
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
            const username = document.getElementById("username").value.trim();
            const password = document.getElementById("password").value;
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
