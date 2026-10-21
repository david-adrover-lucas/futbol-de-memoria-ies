document.addEventListener("DOMContentLoaded", () => {
    const clubes = window.FDM.clubes.all.map((club) => ({
        ...club,
        estadio: estadioPorClub(club.id),
        socios: sociosPorEstrellas(club.stars),
        hinchada: club.apodo,
        desc: descripcionPorClub(club)
    }));

    const objetivosPorEstrellas = {
        5: ["Clasificar a Libertadores", "Ganar la temporada", "Llegar a 4tos de final"],
        4: ["Clasificar a Libertadores", "Llegar a semifinales", "Pelear el titulo"],
        3: ["Clasificar a Sudamericana", "Terminar top 10", "Superar octavos"],
        2: ["Mitad de tabla", "Evitar promocion", "Mejorar el promedio"],
        1: ["Salvarse del descenso", "Formar juveniles", "Llegar a 16avos"]
    };

    const gridClubes = document.getElementById("grid-clubes");
    const btnConfirmar = document.getElementById("btn-confirmar-club");
    const imgEscudo = document.getElementById("det-escudo");
    const nombre = document.getElementById("det-nombre");
    const apodo = document.getElementById("det-apodo");
    const estadio = document.getElementById("det-estadio");
    const socios = document.getElementById("det-hinchada");
    const hinchada = document.getElementById("det-provincia");
    const descripcion = document.getElementById("det-descripcion");
    const sueldo = document.getElementById("det-sueldo");
    const estrellas = document.getElementById("det-estrellas");
    const listaObjetivos = document.getElementById("lista-objetivos");

    let clubSeleccionado = null;

    function aplicarMarcoPorEstrellas(elemento, stars) {
        elemento.style.backgroundImage = `url('../assets/marco-${stars}-estrella.png')`;
    }

    function renderEstrellas(cantidad) {
        estrellas.innerHTML = "";

        for (let i = 0; i < 5; i += 1) {
            const star = document.createElement("span");
            star.className = i < cantidad ? "star-on" : "star-off";
            star.textContent = "★";
            estrellas.appendChild(star);
        }
    }

    function renderObjetivos(stars) {
        listaObjetivos.innerHTML = "";
        objetivosPorEstrellas[stars].forEach((objetivo) => {
            const item = document.createElement("div");
            item.className = "btn-objetivo-tag";
            item.textContent = objetivo;
            listaObjetivos.appendChild(item);
        });
    }

    function mostrarFichaClub(club) {
        clubSeleccionado = club;

        document.querySelectorAll(".club-slot-card").forEach((slot) => {
            slot.classList.toggle("selected", slot.dataset.id === club.id);
        });

        imgEscudo.src = club.escudo;
        imgEscudo.alt = `Escudo de ${club.name}`;
        imgEscudo.classList.remove("hidden");
        nombre.textContent = club.name.toUpperCase();
        apodo.textContent = `Apodo: '${club.apodo}'`;
        estadio.textContent = `Estadio: ${club.estadio}`;
        socios.textContent = `Socios: ${club.socios}`;
        hinchada.textContent = `Hinchada: '${club.hinchada}'`;
        descripcion.textContent = club.desc;
        sueldo.textContent = `$ ${club.sueldo}`;

        aplicarMarcoPorEstrellas(document.querySelector(".escudo-detalle-frame"), club.stars);
        renderEstrellas(club.stars);
        renderObjetivos(club.stars);

        const bloqueado = club.stars >= 3;
        btnConfirmar.disabled = bloqueado;
        btnConfirmar.classList.toggle("disabled", bloqueado);
    }

    function crearSlot(club) {
        const slot = document.createElement("button");
        slot.type = "button";
        slot.className = "club-slot-card";
        slot.dataset.id = club.id;
        slot.setAttribute("aria-label", club.name);
        aplicarMarcoPorEstrellas(slot, club.stars);

        if (club.stars >= 3) {
            slot.classList.add("blocked-club");

            const lockIcon = document.createElement("img");
            lockIcon.src = "../assets/candado.png";
            lockIcon.alt = "";
            lockIcon.className = "club-slot-lock";
            slot.appendChild(lockIcon);
        }

        const img = document.createElement("img");
        img.src = club.escudo;
        img.alt = "";
        img.className = "club-badge-img";
        slot.appendChild(img);
        slot.addEventListener("click", () => mostrarFichaClub(club));
        return slot;
    }

    clubes.forEach((club) => gridClubes.appendChild(crearSlot(club)));

    document.getElementById("btn-club-random").addEventListener("click", () => {
        const disponibles = clubes.filter((club) => club.stars < 3);
        mostrarFichaClub(disponibles[Math.floor(Math.random() * disponibles.length)]);
    });

    document.getElementById("btn-club-back").addEventListener("click", () => {
        window.location.href = "crear-dt.html";
    });

    btnConfirmar.addEventListener("click", () => {
        if (!clubSeleccionado || clubSeleccionado.stars >= 3) return;
        window.FDM.storage.saveClub(clubSeleccionado);
        window.location.href = "menu.html";
    });

    mostrarFichaClub(clubes.find((club) => club.id === "rivadavia") || clubes[0]);

    function estadioPorClub(id) {
        const estadios = {
            boca: "La Bombonera",
            river: "Mas Monumental",
            independiente: "Ricardo Bochini",
            racing: "Presidente Peron",
            huracan: "Tomas A. Duco"
        };
        return estadios[id] || "Estadio principal";
    }

    function sociosPorEstrellas(stars) {
        return `${Math.max(8, stars * 18)}.000`;
    }

    function descripcionPorClub(club) {
        return `${club.name} presenta un desafio de ${club.stars} estrellas. La directiva espera identidad, resultados y crecimiento sostenido durante la temporada.`;
    }
});
