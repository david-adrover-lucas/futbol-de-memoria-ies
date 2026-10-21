document.addEventListener("DOMContentLoaded", () => {
    const avatarCards = document.querySelectorAll(".avatar-card");
    const btnBack = document.getElementById("btn-back");
    const btnRandom = document.getElementById("btn-random");
    const formDT = document.getElementById("form-registro-dt");
    const inputName = document.getElementById("dt-name");
    const inputLastname = document.getElementById("dt-lastname");
    const inputAge = document.getElementById("dt-age");
    const inputProvince = document.getElementById("dt-province");

    let avatarSeleccionado = null;

    function seleccionarAvatar(card) {
        avatarCards.forEach((item) => item.classList.remove("selected"));
        card.classList.add("selected");
        avatarSeleccionado = card.getAttribute("data-avatar");
    }

    avatarCards.forEach((card) => {
        card.addEventListener("click", () => seleccionarAvatar(card));
    });

    btnBack.addEventListener("click", () => {
        window.location.href = "../index.html";
    });

    btnRandom.addEventListener("click", () => {
        const numeroAlAzar = Math.floor(Math.random() * avatarCards.length);
        seleccionarAvatar(avatarCards[numeroAlAzar]);
    });

    formDT.addEventListener("submit", (event) => {
        event.preventDefault();

        if (!avatarSeleccionado) {
            alert("Elegi un avatar para tu DT antes de continuar.");
            return;
        }

        if (!formDT.checkValidity()) {
            formDT.reportValidity();
            return;
        }

        const dt = {
            nombre: inputName.value.trim(),
            apellido: inputLastname.value.trim(),
            edad: inputAge.value,
            provincia: inputProvince.options[inputProvince.selectedIndex].text,
            avatar: avatarSeleccionado,
            avatarSrc: `../assets/avatar-${avatarSeleccionado}.png`,
            experiencia: 0,
            estrellas: 1
        };

        localStorage.setItem("fdmDT", JSON.stringify(dt));
        window.location.href = "elegir-club.html";
    });
});
