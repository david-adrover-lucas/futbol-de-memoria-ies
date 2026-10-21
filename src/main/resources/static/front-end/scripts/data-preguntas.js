(function () {
    const preguntas = [
        {
            texto: "CUANTAS COPAS LIBERTADORES TIENE EL CLUB ATLETICO INDEPENDIENTE?",
            opciones: ["7", "6", "8", "9"],
            correcta: 0,
            dificultad: 5
        },
        {
            texto: "QUE CLUB ES CONOCIDO COMO EL XENEISE?",
            opciones: ["Boca Juniors", "River Plate", "Lanus", "Platense"],
            correcta: 0,
            dificultad: 1
        },
        {
            texto: "QUE EQUIPO JUEGA EN EL ESTADIO MAS MONUMENTAL?",
            opciones: ["Racing Club", "River Plate", "Huracan", "Belgrano"],
            correcta: 1,
            dificultad: 1
        },
        {
            texto: "CUAL ES EL APODO DE RACING CLUB?",
            opciones: ["El Globo", "La Academia", "El Fortin", "El Decano"],
            correcta: 1,
            dificultad: 2
        },
        {
            texto: "QUE CLUB ES CONOCIDO COMO EL PINCHA?",
            opciones: ["Estudiantes LP", "San Lorenzo", "Newell's", "Union"],
            correcta: 0,
            dificultad: 2
        },
        {
            texto: "QUE CLASICO DISPUTAN INDEPENDIENTE Y RACING?",
            opciones: ["Rosarino", "Platense", "Avellaneda", "Cordobes"],
            correcta: 2,
            dificultad: 3
        },
        {
            texto: "QUE CLUB TIENE COMO APODO EL CICLON?",
            opciones: ["San Lorenzo", "Velez", "Tigre", "Aldosivi"],
            correcta: 0,
            dificultad: 2
        },
        {
            texto: "QUE EQUIPO ES LLAMADO EL TALADRO?",
            opciones: ["Banfield", "Huracan", "Argentinos Juniors", "Instituto"],
            correcta: 0,
            dificultad: 3
        },
        {
            texto: "QUE CLUB ES EL DECANO TUCUMANO?",
            opciones: ["Atletico Tucuman", "Belgrano", "Sarmiento", "Riestra"],
            correcta: 0,
            dificultad: 3
        },
        {
            texto: "QUE TORNEO CONTINENTAL ES EL MAS IMPORTANTE DE CONMEBOL?",
            opciones: ["Sudamericana", "Libertadores", "Copa Nacional", "Recopa Argentina"],
            correcta: 1,
            dificultad: 1
        },
        {
            texto: "CUANTOS PUNTOS OTORGA UNA VICTORIA EN LIGA?",
            opciones: ["1", "2", "3", "4"],
            correcta: 2,
            dificultad: 1
        },
        {
            texto: "QUE CLUB ES CONOCIDO COMO EL GLOBO?",
            opciones: ["Huracan", "Lanus", "Defensa y Justicia", "Talleres"],
            correcta: 0,
            dificultad: 2
        },
        {
            texto: "QUE EQUIPO CORDOBES ES APODADO LA T?",
            opciones: ["Instituto", "Talleres", "Belgrano", "Racing de Cordoba"],
            correcta: 1,
            dificultad: 2
        },
        {
            texto: "QUE CLUB ROSARINO ES CONOCIDO COMO CANALLA?",
            opciones: ["Newell's", "Rosario Central", "Colon", "Union"],
            correcta: 1,
            dificultad: 2
        },
        {
            texto: "QUE CLUB ES LLAMADO GRANATE?",
            opciones: ["Lanus", "Tigre", "Platense", "Argentinos Juniors"],
            correcta: 0,
            dificultad: 2
        }
    ];

    function paraRival(stars) {
        const max = Math.max(1, Number(stars) || 1);
        const candidatas = preguntas.filter((pregunta) => pregunta.dificultad <= max + 1);
        return candidatas.length ? candidatas : preguntas;
    }

    window.FDM = window.FDM || {};
    window.FDM.preguntas = { all: preguntas, paraRival };
})();
