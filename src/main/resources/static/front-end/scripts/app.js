document.addEventListener("DOMContentLoaded", () => {
    const splashScreen = document.getElementById("splash-screen");
    const loginScreen = document.getElementById("login-screen");
    const loadingText = document.getElementById("loading-text");
    const loginForm = document.getElementById("login-form");
    const loginBtn = document.getElementById("login-btn");

    setTimeout(() => {
        if (loadingText) loadingText.innerText = "CHARLA TECNICA COMPLETADA...";
    }, 1600);

    setTimeout(() => {
        if (splashScreen && loginScreen) {
            splashScreen.classList.add("hidden");
            loginScreen.classList.remove("hidden");
        }
    }, 2600);

    if (!loginForm) return;

    loginForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const usuarioDT = document.getElementById("username").value.trim();
        const contrasena = document.getElementById("password").value;

        if (!usuarioDT || !contrasena) {
            alert("Completa todos los campos tecnicos.");
            return;
        }

        loginBtn.innerText = "CONECTANDO...";
        loginBtn.style.filter = "brightness(0.8)";
        loginBtn.disabled = true;

        setTimeout(() => {
            loginScreen.style.opacity = "0";
            setTimeout(() => {
                window.location.href = "pantallas/crear-dt.html";
            }, 420);
        }, 900);
    });
});
