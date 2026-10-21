(function () {
    const KEYS = {
        dt: "fdmDT",
        club: "fdmClubInicial",
        carrera: "fdmCarrera",
        temporada: "fdmTemporada",
        torneos: "fdmTorneos",
        ultimoPartido: "fdmUltimoPartido",
        noticias: "fdmNoticias",
        mensajes: "fdmMensajes",
        ofertas: "fdmOfertas",
        guardado: "fdmUltimoGuardado"
    };

    function read(key, fallback) {
        try {
            const raw = localStorage.getItem(key);
            return raw ? JSON.parse(raw) : fallback;
        } catch (error) {
            return fallback;
        }
    }

    function write(key, value) {
        localStorage.setItem(key, JSON.stringify(value));
        return value;
    }

    function defaultDT() {
        return {
            nombre: "Lucas",
            apellido: "Ardrover",
            edad: "22",
            provincia: "Mendocino",
            avatar: "1",
            avatarSrc: "../assets/avatar-1.png",
            experiencia: 1200,
            estrellas: 3
        };
    }

    function defaultClub() {
        return {
            id: "boca",
            name: "Boca Juniors",
            apodo: "El Xeneise",
            stars: 5,
            escudo: "../assets/Boca_escudo.png",
            sueldo: "2.500.000"
        };
    }

    function defaultCarrera() {
        return {
            estadisticas: {
                jugados: 0,
                ganados: 0,
                empatados: 0,
                perdidos: 0,
                gf: 0,
                gc: 0
            },
            historial: [],
            trofeos: [],
            logros: [],
            contrato: {
                temporada: 1,
                objetivo: "Clasificar a copas",
                estado: "Activo"
            }
        };
    }

    function getDT() {
        return read(KEYS.dt, defaultDT());
    }

    function saveDT(dt) {
        return write(KEYS.dt, dt);
    }

    function getClub() {
        return read(KEYS.club, defaultClub());
    }

    function saveClub(club) {
        return write(KEYS.club, club);
    }

    function getCarrera() {
        return read(KEYS.carrera, defaultCarrera());
    }

    function saveCarrera(carrera) {
        return write(KEYS.carrera, carrera);
    }

    function pushList(key, item, limit) {
        const list = read(key, []);
        list.unshift(item);
        return write(key, list.slice(0, limit || 20));
    }

    function saveTimestamp() {
        return write(KEYS.guardado, new Date().toISOString());
    }

    window.FDM = window.FDM || {};
    window.FDM.storage = {
        KEYS,
        read,
        write,
        getDT,
        saveDT,
        getClub,
        saveClub,
        getCarrera,
        saveCarrera,
        pushList,
        saveTimestamp
    };
})();
