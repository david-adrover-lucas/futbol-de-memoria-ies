(function () {
    function crearClub([id, name, apodo, stars, escudo, codigo, pais]) {
        return {
            id,
            name,
            apodo,
            stars,
            codigo: codigo || name.slice(0, 3).toUpperCase(),
            pais: pais || "Argentina",
            escudo: `../assets/${escudo}`,
            sueldo: `${Math.max(220000, stars * 480000).toLocaleString("es-AR")}`
        };
    }

    const clubes = [
        ["boca", "Boca Juniors", "El Xeneise", 5, "Boca_escudo.png", "BOC"],
        ["river", "River Plate", "Millonario", 5, "river.png", "RIV"],
        ["independiente", "Independiente", "Rey de Copas", 5, "Escudo_del_Club_Atlético_Independiente.svg.png", "IND"],
        ["racing", "Racing Club", "La Academia", 5, "Escudo_de_Racing_Club_(2014).svg.png", "RAC"],
        ["estudiantes", "Estudiantes LP", "Pincha", 5, "estudiantes.png", "EST"],
        ["sanlorenzo", "San Lorenzo", "Ciclon", 4, "sanLorenzo.png", "SAN"],
        ["velez", "Velez", "Fortin", 4, "velez.png", "VEL"],
        ["talleres", "Talleres", "La T", 4, "talleres.png", "TAL"],
        ["rosario", "Rosario Central", "Canalla", 4, "rosarioCentral.png", "ROS"],
        ["newells", "Newell's", "Lepra", 4, "newlls.png", "NEW"],
        ["lanus", "Lanus", "Granate", 3, "Lanus.png", "LAN"],
        ["argentinos", "Argentinos Juniors", "Bicho", 3, "ArgentinosJuniors.png", "ARG"],
        ["huracan", "Huracan", "Globo", 3, "huracan.png", "HUR"],
        ["belgrano", "Belgrano", "Pirata", 3, "Belgrano.png", "BEL"],
        ["instituto", "Instituto", "Gloria", 3, "Instituto.png", "INS"],
        ["atletico", "Atletico Tucuman", "Decano", 3, "Atlético-Tucumán.png", "ATU"],
        ["defensa", "Defensa y Justicia", "Halcon", 3, "Defensa-y-Justicia.png", "DYJ"],
        ["union", "Union", "Tatengue", 3, "Club-Atletico-Union.png", "UNI"],
        ["banfield", "Banfield", "Taladro", 3, "Escudo_de_Banfield.png", "BAN"],
        ["central", "Central Cordoba", "Ferroviario", 2, "Central Córdoba.png", "CCO"],
        ["gimnasia", "Gimnasia LP", "Lobo", 2, "gimnaciaPlata.png", "GIM"],
        ["rivadavia", "Independiente Rivadavia", "Lepra Mendocina", 2, "Independiente-Rivadavia.png", "IRI"],
        ["platense", "Platense", "Calamar", 2, "Platense.png", "PLA"],
        ["sarmiento", "Sarmiento", "Verde", 2, "Sarmiento.png", "SAR"],
        ["riestra", "Deportivo Riestra", "Malevo", 2, "riestra.png", "RIE"],
        ["tigre", "Tigre", "Matador", 2, "tigre.png", "TIG"],
        ["barracas", "Barracas Central", "Guapo", 1, "Barracas_central_logo.svg.png", "BAR"],
        ["aldosivi", "Aldosivi", "Tiburon", 1, "Aldosivi.png", "ALD"],
        ["gimnasia-mendoza", "Gimnasia de Mendoza", "Lobo Mendocino", 1, "Gimnasia-de-Mendoza.png", "GME"],
        ["estudiantes-rc", "Estudiantes RC", "Leon del Imperio", 1, "Estudiantes_de_Río_Cuarto.svg.png", "ERC"]
    ].map(crearClub);

    const libertadoresInternacionales = [
        ["sao-paulo", "Sao Paulo", "Tricolor", 5, "Sao-Paulo.png", "SAO", "Brasil"],
        ["botafogo", "Botafogo", "Glorioso", 5, "Botafogo.png", "BOT", "Brasil"],
        ["atletico-mineiro", "Atletico Mineiro", "Galo", 5, "Atletico-Mineiro.png", "CAM", "Brasil"],
        ["gremio", "Gremio", "Tricolor Gaúcho", 5, "Gremio.png", "GRE", "Brasil"],
        ["u-chile", "Universidad de Chile", "La U", 4, "Universidad-de-Chile.png", "UCH", "Chile"],
        ["america-cali", "America de Cali", "Diablos Rojos", 4, "America-de-Cali.png", "AME", "Colombia"],
        ["millonarios", "Millonarios", "Embajador", 4, "Millonarios.png", "MIL", "Colombia"],
        ["olimpia", "Olimpia", "Decano", 4, "Olimpia.png", "OLI", "Paraguay"],
        ["barcelona-ecu", "Barcelona SC", "Torero", 4, "Barcelona-de-ecuador.png", "BSC", "Ecuador"],
        ["mct", "Montevideo City Torque", "City", 3, "monte-video-city.png", "MCT", "Uruguay"]
    ].map(crearClub);

    const sudamericanaInternacionales = [
        ["flamengo", "Flamengo", "Mengao", 5, "flamengo.png", "FLA", "Brasil"],
        ["palmeiras", "Palmeiras", "Verdao", 5, "palmeiras.png", "PAL", "Brasil"],
        ["fluminense", "Fluminense", "Tricolor", 4, "fluminise.png", "FLU", "Brasil"],
        ["corinthians", "Corinthians", "Timao", 4, "Corinthians.png", "COR", "Brasil"],
        ["colo-colo", "Colo-Colo", "Albo", 4, "colo-colo.png", "COL", "Chile"],
        ["idv", "Independiente del Valle", "Rayado", 4, "independien-del-valle.png", "IDV", "Ecuador"],
        ["ldu", "Liga de Quito", "Rey de Copas", 4, "Liga-de-Quito.png", "LDU", "Ecuador"],
        ["penarol", "Peñarol", "Manya", 4, "penarol.png", "PEN", "Uruguay"],
        ["nacional-uy", "Nacional", "Bolso", 4, "nacional.png", "NAL", "Uruguay"],
        ["cerro-porteno", "Cerro Porteño", "Ciclón", 4, "Cerro-Porteno.png", "CCP", "Paraguay"]
    ].map(crearClub);

    const todos = [...clubes, ...libertadoresInternacionales, ...sudamericanaInternacionales];

    function byId(id) {
        return todos.find((club) => club.id === id) || clubes[0];
    }

    function randomOpponent(currentId) {
        const pool = clubes.filter((club) => club.id !== currentId);
        return pool[Math.floor(Math.random() * pool.length)];
    }

    window.FDM = window.FDM || {};
    window.FDM.clubes = {
        all: clubes,
        argentinos: clubes,
        internacionales: {
            libertadores: libertadoresInternacionales,
            sudamericana: sudamericanaInternacionales
        },
        byId,
        randomOpponent
    };
})();
