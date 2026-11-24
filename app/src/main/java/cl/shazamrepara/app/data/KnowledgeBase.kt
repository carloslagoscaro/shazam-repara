package cl.shazamrepara.app.data

import cl.shazamrepara.app.data.Difficulty.ESPECIALISTA
import cl.shazamrepara.app.data.Difficulty.FACIL
import cl.shazamrepara.app.data.Difficulty.MEDIA
import cl.shazamrepara.app.data.EquipmentType.AIRE
import cl.shazamrepara.app.data.EquipmentType.ASPIRADORA
import cl.shazamrepara.app.data.EquipmentType.AUTO
import cl.shazamrepara.app.data.EquipmentType.CALEFON
import cl.shazamrepara.app.data.EquipmentType.LAVADORA
import cl.shazamrepara.app.data.EquipmentType.LAVAVAJILLAS
import cl.shazamrepara.app.data.EquipmentType.MICROONDAS
import cl.shazamrepara.app.data.EquipmentType.REFRIGERADOR
import cl.shazamrepara.app.data.EquipmentType.SECADORA
import cl.shazamrepara.app.data.SafetyLevel.PELIGRO
import cl.shazamrepara.app.data.SafetyLevel.PRECAUCION
import cl.shazamrepara.app.data.SafetyLevel.SEGURO
import cl.shazamrepara.app.data.Symptom.CHIRRIDO
import cl.shazamrepara.app.data.Symptom.FALTA_POTENCIA
import cl.shazamrepara.app.data.Symptom.FUGA
import cl.shazamrepara.app.data.Symptom.GOLPETEO
import cl.shazamrepara.app.data.Symptom.NO_DRENA
import cl.shazamrepara.app.data.Symptom.NO_ENCIENDE
import cl.shazamrepara.app.data.Symptom.NO_ENFRIA
import cl.shazamrepara.app.data.Symptom.RUIDO_AGUDO
import cl.shazamrepara.app.data.Symptom.RUIDO_METALICO
import cl.shazamrepara.app.data.Symptom.SILBIDO
import cl.shazamrepara.app.data.Symptom.SOBRECALIENTA
import cl.shazamrepara.app.data.Symptom.VIBRACION
import cl.shazamrepara.app.data.Symptom.ZUMBIDO

/**
 * Catálogo de fallas frecuentes en autos y electrodomésticos.
 *
 * Los rangos de precio son referencias de mercado en Chile (repuesto genérico u original
 * económico + mano de obra de taller medio, 2025-2026). Sirven para detectar cobros
 * abusivos, no como cotización oficial.
 */
object KnowledgeBase {

    val generalTips: List<String> = listOf(
        "Pide el presupuesto por escrito, separando repuesto y mano de obra.",
        "Pide que te muestren la pieza que retiran: es la prueba de que la cambiaron.",
        "Compara al menos dos presupuestos con el mismo nombre de repuesto y código.",
        "Desconfía si el técnico cambia el diagnóstico en la misma visita sin una prueba nueva.",
        "Un repuesto original no siempre es necesario: pregunta por el equivalente genérico con garantía.",
        "Exige boleta o factura y garantía escrita de la reparación (mínimo 3 meses).",
        "Si el costo supera el 60% del valor de un equipo nuevo, conviene reemplazarlo."
    )

    private fun fault(
        id: String,
        name: String,
        types: Set<EquipmentType>,
        symptoms: Map<Symptom, Float>,
        visual: Map<VisualSign, Float> = emptyMap(),
        safety: SafetyLevel,
        difficulty: Difficulty,
        partName: String,
        partHint: String,
        partCost: IntRange,
        laborCost: IntRange,
        why: String,
        check: String
    ) = FaultTemplate(
        id = id,
        name = name,
        types = types,
        symptoms = symptoms,
        visual = visual,
        safety = safety,
        difficulty = difficulty,
        partName = partName,
        partHint = partHint,
        partCostClp = partCost,
        laborCostClp = laborCost,
        why = why,
        check = check
    )

    val faults: List<FaultTemplate> = listOf(

        // ------------------------- AUTO -------------------------
        fault(
            id = "auto_bomba_agua",
            name = "Bomba de agua / empaquetadura",
            types = setOf(AUTO),
            symptoms = mapOf(
                CHIRRIDO to 0.9f,
                ZUMBIDO to 0.55f,
                SOBRECALIENTA to 0.8f,
                FUGA to 0.6f,
                RUIDO_METALICO to 0.4f
            ),
            visual = mapOf(VisualSign.FUGA_AGUA to 0.7f, VisualSign.FUGA_ACEITE to 0.3f),
            safety = PRECAUCION,
            difficulty = ESPECIALISTA,
            partName = "Bomba de agua con junta y sellos",
            partHint = "Pídela por marca, modelo, año y motor. Compara el número de parte del fabricante con el de la caja.",
            partCost = 45_000..120_000,
            laborCost = 40_000..90_000,
            why = "El chirrido en la zona de la correa, la pérdida de refrigerante y el sobrecalentamiento son el trío clásico de la bomba de agua o su empaquetadura.",
            check = "¿Hay líquido verde o rosado bajo el auto? ¿La temperatura sube en tacos o con aire acondicionado encendido?"
        ),
        fault(
            id = "auto_correa",
            name = "Correa de accesorios (poly-V)",
            types = setOf(AUTO),
            symptoms = mapOf(CHIRRIDO to 1.0f, SILBIDO to 0.7f, RUIDO_AGUDO to 0.55f),
            safety = PRECAUCION,
            difficulty = MEDIA,
            partName = "Correa poly-V (o banda de accesorios)",
            partHint = "Lleva la correa vieja o anota el código grabado en el reverso: es el dato exacto que debe coincidir.",
            partCost = 12_000..35_000,
            laborCost = 15_000..40_000,
            why = "El chirrido agudo que aparece al acelerar o con el motor frío es el sonido típico de la correa patinando o endurecida.",
            check = "¿El chirrido desaparece al acelerar o al mojar la correa con agua? Eso confirma el diagnóstico."
        ),
        fault(
            id = "auto_rodamiento_alternador",
            name = "Rodamiento de alternador o tensor",
            types = setOf(AUTO),
            symptoms = mapOf(ZUMBIDO to 0.9f, RUIDO_METALICO to 0.7f, RUIDO_AGUDO to 0.4f),
            safety = PRECAUCION,
            difficulty = MEDIA,
            partName = "Rodamiento o tensor de correa",
            partHint = "Pregunta si van a cambiar solo el rodamiento o el tensor completo; el tensor completo suele salir casi igual.",
            partCost = 25_000..70_000,
            laborCost = 30_000..70_000,
            why = "Un zumbido grave que crece con las RPM y no cambia al pisar el embrague apunta a un rodamiento giratorio del tren de accesorios.",
            check = "Con el motor andando y el capó abierto, ¿de qué zona viene el ruido: alternador, tensor o compresor del aire?"
        ),
        fault(
            id = "auto_frenos",
            name = "Pastillas de freno desgastadas",
            types = setOf(AUTO),
            symptoms = mapOf(CHIRRIDO to 0.85f, RUIDO_METALICO to 0.8f, VIBRACION to 0.45f),
            safety = PRECAUCION,
            difficulty = MEDIA,
            partName = "Pastillas de freno (juego por eje)",
            partHint = "Cambia siempre por eje completo y pide revisar el disco. Pide el espesor medido antes y después.",
            partCost = 25_000..70_000,
            laborCost = 20_000..50_000,
            why = "El chirrido al frenar y un ruido metálico indican pastilla gastada tocando el disco.",
            check = "¿El ruido aparece al frenar y desaparece al soltar el pedal? ¿El pedal se siente más largo?"
        ),
        fault(
            id = "auto_soporte_motor",
            name = "Soporte de motor desgastado",
            types = setOf(AUTO),
            symptoms = mapOf(VIBRACION to 1.0f, GOLPETEO to 0.8f, RUIDO_METALICO to 0.35f),
            safety = SEGURO,
            difficulty = MEDIA,
            partName = "Soporte o goma de motor",
            partHint = "Hay 3 o 4 soportes distintos. Exige que identifiquen cuál por posición (derecho, izquierdo, trasero).",
            partCost = 25_000..90_000,
            laborCost = 25_000..60_000,
            why = "Vibración fuerte en ralentí que mejora al acelerar es señal de goma hidráulica rota.",
            check = "¿La vibración se siente en el volante y en el asiento con el auto detenido en D o en neutro?"
        ),
        fault(
            id = "auto_rodamiento_rueda",
            name = "Rodamiento de rueda",
            types = setOf(AUTO),
            symptoms = mapOf(ZUMBIDO to 1.0f, RUIDO_METALICO to 0.5f, VIBRACION to 0.5f),
            safety = PRECAUCION,
            difficulty = ESPECIALISTA,
            partName = "Rodamiento de rueda (con maza si aplica)",
            partHint = "Pide la medida del rodamiento y si incluye sensor ABS: cambia el precio y el modelo.",
            partCost = 30_000..80_000,
            laborCost = 30_000..70_000,
            why = "Zumbido que aumenta con la velocidad y cambia al girar el volante es el patrón de rodamiento de rueda.",
            check = "¿El ruido cambia al doblar a izquierda o derecha? Si cambia, el rodamiento está de ese lado."
        ),
        fault(
            id = "auto_radiador",
            name = "Fuga en radiador o mangueras",
            types = setOf(AUTO),
            symptoms = mapOf(FUGA to 1.0f, SOBRECALIENTA to 0.75f, SILBIDO to 0.3f),
            visual = mapOf(VisualSign.FUGA_AGUA to 0.9f),
            safety = PRECAUCION,
            difficulty = ESPECIALISTA,
            partName = "Radiador, manguera o abrazadera",
            partHint = "No aceptes 'cambio de radiador' sin prueba de presión: muchas veces es solo una manguera o abrazadera.",
            partCost = 20_000..120_000,
            laborCost = 25_000..70_000,
            why = "Pérdida de refrigerante y sobrecalentamiento sin ruido interno apuntan a fuga en el circuito de enfriamiento.",
            check = "¿Baja el nivel del estanque de refrigerante sin mancha visible? Revisa la alfombra del copiloto (calefacción)."
        ),
        fault(
            id = "auto_bujia_bobina",
            name = "Bujías o bobina de encendido",
            types = setOf(AUTO),
            symptoms = mapOf(FALTA_POTENCIA to 1.0f, NO_ENCIENDE to 0.6f, GOLPETEO to 0.45f),
            visual = mapOf(VisualSign.PANTALLA_ERROR to 0.5f),
            safety = PRECAUCION,
            difficulty = MEDIA,
            partName = "Bujías (juego) o bobina de encendido",
            partHint = "Pide el código de falla escaneado (P0301, P0302…) y que te muestren la bujía cambiada.",
            partCost = 8_000..60_000,
            laborCost = 15_000..45_000,
            why = "Falta de potencia y marcha irregular son consistentes con falla de chispa en uno o más cilindros.",
            check = "¿Se enciende la luz de check engine y parpadea al acelerar? Eso indica falla de encendido activa."
        ),
        fault(
            id = "auto_inyector",
            name = "Inyector sucio o defectuoso",
            types = setOf(AUTO),
            symptoms = mapOf(GOLPETEO to 0.8f, FALTA_POTENCIA to 0.65f, RUIDO_METALICO to 0.4f),
            safety = SEGURO,
            difficulty = ESPECIALISTA,
            partName = "Inyector o limpieza ultrasónica",
            partHint = "Antes de cambiar inyectores, pide limpieza y prueba de caudal: el cambio completo puede costar 10 veces más.",
            partCost = 30_000..180_000,
            laborCost = 30_000..80_000,
            why = "Cascabeleo y tirones a baja carga suelen venir de inyección desbalanceada.",
            check = "¿El consumo subió y sale humo negro en aceleración? ¿Hay olor a bencina en el escape?"
        ),

        // ---------------------- LAVADORA ----------------------
        fault(
            id = "lavadora_rodamiento",
            name = "Rodamiento del tambor",
            types = setOf(LAVADORA),
            symptoms = mapOf(ZUMBIDO to 1.0f, RUIDO_METALICO to 0.75f, VIBRACION to 0.6f),
            visual = mapOf(VisualSign.FUGA_AGUA to 0.35f),
            safety = PRECAUCION,
            difficulty = ESPECIALISTA,
            partName = "Kit rodamiento y retén del tambor",
            partHint = "Pide el kit completo con retén. Cambiar solo el rodamiento sin retén dura pocos meses.",
            partCost = 30_000..70_000,
            laborCost = 40_000..90_000,
            why = "Zumbido fuerte en centrifugado con ruido metálico es el patrón clásico de rodamiento del tambor.",
            check = "Gira el tambor con la lavadora apagada: ¿se siente áspero o con juego vertical?"
        ),
        fault(
            id = "lavadora_correa",
            name = "Correa de transmisión",
            types = setOf(LAVADORA, SECADORA),
            symptoms = mapOf(CHIRRIDO to 0.9f, NO_DRENA to 0.6f, VIBRACION to 0.3f),
            safety = SEGURO,
            difficulty = FACIL,
            partName = "Correa de lavadora / secadora",
            partHint = "Anota el número grabado en la correa vieja (por ejemplo 4L-1140). Es lo único que necesitas para comprarla.",
            partCost = 8_000..20_000,
            laborCost = 15_000..35_000,
            why = "Chirrido y tambor que no gira (pero el motor sí suena) es correa estirada o rota.",
            check = "¿El motor suena pero el tambor no se mueve? ¿Hay olor a goma caliente?"
        ),
        fault(
            id = "lavadora_bomba_desague",
            name = "Bomba de desagüe",
            types = setOf(LAVADORA, LAVAVAJILLAS),
            symptoms = mapOf(NO_DRENA to 1.0f, ZUMBIDO to 0.7f, RUIDO_METALICO to 0.35f),
            visual = mapOf(VisualSign.FUGA_AGUA to 0.5f),
            safety = PRECAUCION,
            difficulty = MEDIA,
            partName = "Bomba de desagüe",
            partHint = "Revisa primero el filtro y la manguera: un objeto atascado imita la falla de la bomba.",
            partCost = 15_000..35_000,
            laborCost = 20_000..40_000,
            why = "El agua no sale y la bomba zumba sin bombear indica impulsor trabado o motor de bomba quemado.",
            check = "¿Queda agua estancada al final del ciclo? Limpia el filtro antes de comprar la bomba."
        ),
        fault(
            id = "lavadora_amortiguadores",
            name = "Amortiguadores o resortes del tambor",
            types = setOf(LAVADORA),
            symptoms = mapOf(VIBRACION to 1.0f, GOLPETEO to 0.8f, RUIDO_METALICO to 0.4f),
            safety = SEGURO,
            difficulty = MEDIA,
            partName = "Juego de amortiguadores (2 o 4) y resortes",
            partHint = "Se cambian en juego. Pide los 4 si son de la misma antigüedad.",
            partCost = 20_000..45_000,
            laborCost = 25_000..50_000,
            why = "La lavadora 'camina' o golpea en centrifugado cuando los amortiguadores están vencidos.",
            check = "¿Se mueve de lugar en centrifugado? ¿Golpea más con carga pesada?"
        ),
        fault(
            id = "lavadora_sensor_nivel",
            name = "Sensor de nivel o presostato",
            types = setOf(LAVADORA),
            symptoms = mapOf(NO_DRENA to 0.55f, NO_ENCIENDE to 0.45f, FALTA_POTENCIA to 0.4f),
            visual = mapOf(VisualSign.PANTALLA_ERROR to 0.85f),
            safety = SEGURO,
            difficulty = MEDIA,
            partName = "Presostato / sensor de nivel de agua",
            partHint = "Anota el código de error exacto que muestra la lavadora: define el sensor correcto.",
            partCost = 10_000..30_000,
            laborCost = 20_000..40_000,
            why = "Un código de error de nivel o desagüe con bomba sana apunta al presostato o su manguera.",
            check = "¿La lavadora se detiene siempre en la misma etapa del ciclo y muestra el mismo código?"
        ),

        // -------------------- REFRIGERADOR --------------------
        fault(
            id = "refri_compresor",
            name = "Compresor",
            types = setOf(REFRIGERADOR),
            symptoms = mapOf(NO_ENFRIA to 1.0f, ZUMBIDO to 0.8f, NO_ENCIENDE to 0.5f, SOBRECALIENTA to 0.5f),
            safety = PRECAUCION,
            difficulty = ESPECIALISTA,
            partName = "Compresor (con carga de gas)",
            partHint = "Exige la medición de presiones antes de cambiar compresor: muchas fallas son solo capacitor o termostato.",
            partCost = 90_000..250_000,
            laborCost = 60_000..140_000,
            why = "El compresor zumba y se apaga por térmico, sin enfriar, indica compresor o capacitor de arranque.",
            check = "¿La parte trasera está caliente y el motor intenta arrancar cada pocos minutos?"
        ),
        fault(
            id = "refri_ventilador",
            name = "Ventilador del evaporador",
            types = setOf(REFRIGERADOR),
            symptoms = mapOf(ZUMBIDO to 0.9f, GOLPETEO to 0.6f, NO_ENFRIA to 0.6f),
            safety = SEGURO,
            difficulty = MEDIA,
            partName = "Motor del ventilador del evaporador",
            partHint = "Pide también la hélice si está agrietada; suele ser la causa del golpeteo.",
            partCost = 15_000..40_000,
            laborCost = 25_000..50_000,
            why = "Ruido de aspas y frío desigual entre el congelador y el refrigerador indica ventilador del evaporador.",
            check = "¿La parte de abajo enfría menos que arriba? ¿El ruido se detiene al abrir la puerta?"
        ),
        fault(
            id = "refri_termostato",
            name = "Termostato o sensor de temperatura",
            types = setOf(REFRIGERADOR),
            symptoms = mapOf(NO_ENFRIA to 0.8f, SOBRECALIENTA to 0.35f, ZUMBIDO to 0.3f),
            safety = SEGURO,
            difficulty = MEDIA,
            partName = "Termostato / termistor",
            partHint = "Pide que midan el termistor con tester antes de cambiarlo; es una pieza barata y fácil de cambiar sin razón.",
            partCost = 12_000..35_000,
            laborCost = 20_000..45_000,
            why = "El equipo enfría de más o no corta el compresor: el control de temperatura no está regulando.",
            check = "¿El congelador congela todo y el refrigerador también? ¿El compresor nunca se detiene?"
        ),
        fault(
            id = "refri_sello",
            name = "Sello de puerta",
            types = setOf(REFRIGERADOR),
            symptoms = mapOf(NO_ENFRIA to 0.65f, ZUMBIDO to 0.25f),
            visual = mapOf(VisualSign.OXIDO to 0.4f),
            safety = SEGURO,
            difficulty = FACIL,
            partName = "Burlete / sello de puerta",
            partHint = "Mide el perímetro y pide el sello por modelo exacto; se pega o se encastra según diseño.",
            partCost = 10_000..30_000,
            laborCost = 10_000..25_000,
            why = "El frío se escapa por el sello y el compresor trabaja de más.",
            check = "Prueba del papel: cierra la puerta sobre una hoja; si sale sin resistencia, el sello está vencido."
        ),

        // ------------------ LAVAVAJILLAS ----------------------
        fault(
            id = "lavavajillas_bomba",
            name = "Bomba de lavado o desagüe",
            types = setOf(LAVAVAJILLAS),
            symptoms = mapOf(NO_DRENA to 1.0f, ZUMBIDO to 0.6f, RUIDO_METALICO to 0.4f),
            visual = mapOf(VisualSign.FUGA_AGUA to 0.5f),
            safety = PRECAUCION,
            difficulty = MEDIA,
            partName = "Bomba de lavado o desagüe",
            partHint = "Limpia filtros y revisa el brazo aspersor antes: un filtro tapado simula bomba muerta.",
            partCost = 25_000..60_000,
            laborCost = 30_000..60_000,
            why = "No desagua o no lanza agua y la bomba suena forzada.",
            check = "¿Hay agua sucia al fondo al terminar? ¿El brazo aspersor gira libre?"
        ),

        // ---------------------- AIRE --------------------------
        fault(
            id = "aire_filtro",
            name = "Filtros sucios",
            types = setOf(AIRE),
            symptoms = mapOf(FALTA_POTENCIA to 0.8f, NO_ENFRIA to 0.6f, RUIDO_AGUDO to 0.45f, ZUMBIDO to 0.3f),
            safety = SEGURO,
            difficulty = FACIL,
            partName = "Filtros de aire",
            partHint = "Se lavan o se cambian sin técnico. Hacerlo antes de pagar una visita evita la mitad de los cobros.",
            partCost = 5_000..20_000,
            laborCost = 0..0,
            why = "Poco caudal de aire y ruido de aire forzado con equipo que enfría algo: filtro saturado.",
            check = "¿El filtro se ve con polvo pegado? Lávalo y prueba de nuevo antes de llamar a nadie."
        ),
        fault(
            id = "aire_gas",
            name = "Falta de gas refrigerante o fuga",
            types = setOf(AIRE),
            symptoms = mapOf(NO_ENFRIA to 1.0f, SILBIDO to 0.55f, ZUMBIDO to 0.35f),
            visual = mapOf(VisualSign.FUGA_AGUA to 0.35f),
            safety = SEGURO,
            difficulty = ESPECIALISTA,
            partName = "Carga de gas R410A / R32 y detección de fuga",
            partHint = "Exige prueba de fuga con nitrógeno antes de cargar gas; si no, recargar es plata perdida.",
            partCost = 25_000..80_000,
            laborCost = 30_000..90_000,
            why = "Enfría poco, la tubería se congela y el equipo nunca corta por temperatura.",
            check = "¿Sale aire frío solo al principio y luego templado? ¿Hay hielo en la cañería?"
        ),
        fault(
            id = "aire_ventilador",
            name = "Motor del ventilador (unidad interior o exterior)",
            types = setOf(AIRE),
            symptoms = mapOf(ZUMBIDO to 0.8f, GOLPETEO to 0.6f, RUIDO_METALICO to 0.4f),
            safety = SEGURO,
            difficulty = ESPECIALISTA,
            partName = "Motor de ventilador o capacitor",
            partHint = "Pide medir el capacitor: es barato y provoca los mismos síntomas que el motor quemado.",
            partCost = 20_000..70_000,
            laborCost = 30_000..70_000,
            why = "Ruido de aspas y golpeteo con vibración en la unidad.",
            check = "¿El ventilador gira lento o se detiene y arranca? ¿El ruido viene de la unidad interior?"
        ),

        // ------------------- MICROONDAS -----------------------
        fault(
            id = "microondas_magnetron",
            name = "Magnetrón o capacitor de alta tensión",
            types = setOf(MICROONDAS),
            symptoms = mapOf(NO_ENFRIA to 0.8f, ZUMBIDO to 0.8f, RUIDO_METALICO to 0.6f, SOBRECALIENTA to 0.4f),
            safety = PELIGRO,
            difficulty = ESPECIALISTA,
            partName = "Magnetrón / capacitor / diodo de alta tensión",
            partHint = "No abras el microondas: el capacitor guarda alta tensión incluso desenchufado. Solo técnico.",
            partCost = 60_000..150_000,
            laborCost = 40_000..80_000,
            why = "Zumbido fuerte sin calentar, o chispazos, indican el circuito de alta tensión.",
            check = "¿Calienta algo? Si no calienta y zumba más fuerte que antes, no lo uses más."
        ),
        fault(
            id = "microondas_puerta",
            name = "Interruptor de puerta o seguro",
            types = setOf(MICROONDAS),
            symptoms = mapOf(NO_ENCIENDE to 0.7f, NO_ENFRIA to 0.5f),
            visual = mapOf(VisualSign.PANTALLA_ERROR to 0.5f),
            safety = PELIGRO,
            difficulty = ESPECIALISTA,
            partName = "Microinterruptor de puerta",
            partHint = "Es barato, pero el riesgo eléctrico es alto: no lo cambies tú.",
            partCost = 8_000..25_000,
            laborCost = 30_000..60_000,
            why = "El equipo no parte porque el seguro de puerta no detecta el cierre.",
            check = "¿Los botones responden pero no arranca? ¿La puerta se siente floja o no hace clic?"
        ),

        // --------------------- CALEFÓN ------------------------
        fault(
            id = "calefon_sarro",
            name = "Sarro en el intercambiador o resistencia",
            types = setOf(CALEFON),
            symptoms = mapOf(NO_ENFRIA to 0.8f, RUIDO_AGUDO to 0.7f, SOBRECALIENTA to 0.5f, ZUMBIDO to 0.3f),
            safety = PRECAUCION,
            difficulty = MEDIA,
            partName = "Desincrustación o resistencia",
            partHint = "Pide desincrustación antes de cambiar la resistencia: es la mitad del costo y resuelve la mayoría.",
            partCost = 15_000..60_000,
            laborCost = 20_000..50_000,
            why = "Chisporroteo o silbido al calentar y menos agua caliente: sarro acumulado.",
            check = "¿El agua caliente dura cada vez menos y el equipo suena como una tetera?"
        ),
        fault(
            id = "calefon_resistencia",
            name = "Resistencia o termostato de seguridad",
            types = setOf(CALEFON),
            symptoms = mapOf(NO_ENFRIA to 0.9f, NO_ENCIENDE to 0.4f),
            safety = PRECAUCION,
            difficulty = ESPECIALISTA,
            partName = "Resistencia y termostato",
            partHint = "Pide medir continuidad de la resistencia antes de comprarla.",
            partCost = 20_000..80_000,
            laborCost = 25_000..60_000,
            why = "El equipo enciende pero no calienta: resistencia abierta o termostato disparado.",
            check = "¿Se corta la luz del equipo después de un rato? ¿Hay olor a quemado en el tablero?"
        ),

        // ------------------- ASPIRADORA -----------------------
        fault(
            id = "aspiradora_filtro",
            name = "Filtro o bolsa saturados",
            types = setOf(ASPIRADORA),
            symptoms = mapOf(FALTA_POTENCIA to 1.0f, RUIDO_AGUDO to 0.5f, SOBRECALIENTA to 0.4f),
            safety = SEGURO,
            difficulty = FACIL,
            partName = "Filtro HEPA / bolsa",
            partHint = "Es mantenimiento: no pagues visita técnica por esto.",
            partCost = 5_000..20_000,
            laborCost = 0..0,
            why = "Succión débil y motor más agudo: el aire no pasa por el filtro tapado.",
            check = "¿El filtro está cubierto de polvo y el motor se calienta?"
        ),
        fault(
            id = "aspiradora_correa",
            name = "Correa del cepillo",
            types = setOf(ASPIRADORA),
            symptoms = mapOf(CHIRRIDO to 0.8f, VIBRACION to 0.5f, RUIDO_METALICO to 0.4f),
            safety = SEGURO,
            difficulty = FACIL,
            partName = "Correa de cepillo",
            partHint = "Compra la correa por modelo; suele venir con instructivo.",
            partCost = 4_000..12_000,
            laborCost = 10_000..25_000,
            why = "Chirrido del cepillo y pérdida de succión al usarlo en alfombra.",
            check = "¿El cepillo gira lento o se detiene al apoyarlo en la alfombra?"
        ),

        // -------------------- GENERALES -----------------------
        fault(
            id = "general_cable_quemado",
            name = "Cable, enchufe o automático quemado",
            types = emptySet(),
            symptoms = mapOf(SOBRECALIENTA to 0.6f, NO_ENCIENDE to 0.5f),
            visual = mapOf(
                VisualSign.CABLE_QUEMADO to 1.0f,
                VisualSign.OLOR_QUEMADO to 0.9f,
                VisualSign.HUMO to 0.7f
            ),
            safety = PELIGRO,
            difficulty = ESPECIALISTA,
            partName = "Cable, enchufe o protección automática",
            partHint = "No reemplaces un automático por uno de mayor amperaje: eso anula la protección y causa incendios.",
            partCost = 8_000..60_000,
            laborCost = 20_000..80_000,
            why = "Hay señales directas de sobrecalentamiento eléctrico: aislamiento quemado, olor o humo.",
            check = "¿El enchufe o el cable están calientes al tacto? ¿Saltó el automático más de una vez?"
        ),
        fault(
            id = "general_fuga_agua",
            name = "Fuga de agua o condensación",
            types = emptySet(),
            symptoms = mapOf(FUGA to 1.0f, VIBRACION to 0.2f),
            visual = mapOf(VisualSign.FUGA_AGUA to 1.0f, VisualSign.OXIDO to 0.5f),
            safety = PRECAUCION,
            difficulty = MEDIA,
            partName = "Sello, manguera o unión",
            partHint = "Identifica primero el punto exacto de la fuga y si está cerca de cables o enchufes.",
            partCost = 5_000..40_000,
            laborCost = 20_000..60_000,
            why = "Agua visible en un equipo eléctrico puede derivar en cortocircuito.",
            check = "¿La fuga aparece solo al usar el equipo? ¿Está cerca de una toma de corriente?"
        ),
        fault(
            id = "general_pieza_suelta",
            name = "Pieza suelta o mal montada",
            types = emptySet(),
            symptoms = mapOf(GOLPETEO to 0.9f, VIBRACION to 0.8f, RUIDO_METALICO to 0.5f),
            visual = mapOf(VisualSign.PIEZA_SUELTA to 1.0f, VisualSign.GOLPE to 0.4f),
            safety = SEGURO,
            difficulty = FACIL,
            partName = "Tornillería, tope o soporte",
            partHint = "Antes de comprar cualquier pieza, revisa tornillos, patas y topes; suele ser solo apriete.",
            partCost = 3_000..20_000,
            laborCost = 10_000..35_000,
            why = "El golpeteo con vibración y una pieza a la vista explica el ruido sin falla interna.",
            check = "¿El ruido cambia si presionas la tapa o mueves la pieza suelta?"
        )
    )

    val unknownFault: FaultTemplate = fault(
        id = "unknown",
        name = "Falla no identificada con certeza",
        types = emptySet(),
        symptoms = emptyMap(),
        safety = PRECAUCION,
        difficulty = MEDIA,
        partName = "Por definir",
        partHint = "Repite las mediciones en un lugar silencioso y con el equipo en la etapa donde falla.",
        partCost = 0..0,
        laborCost = 0..0,
        why = "Las señales capturadas no coinciden con los patrones conocidos de la base.",
        check = "Describe cuándo falla exactamente (al encender, al calentar, en centrifugado, al acelerar) y vuelve a medir."
    )
}
