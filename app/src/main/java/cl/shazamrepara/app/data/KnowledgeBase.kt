package cl.shazamrepara.app.data

import cl.shazamrepara.app.data.Difficulty.ESPECIALISTA
import cl.shazamrepara.app.data.Difficulty.FACIL
import cl.shazamrepara.app.data.Difficulty.MEDIA
import cl.shazamrepara.app.data.EquipmentType.AUTO
import cl.shazamrepara.app.data.SafetyLevel.PELIGRO
import cl.shazamrepara.app.data.SafetyLevel.PRECAUCION
import cl.shazamrepara.app.data.SafetyLevel.SEGURO
import cl.shazamrepara.app.data.Symptom.CHIRRIDO
import cl.shazamrepara.app.data.Symptom.FALTA_POTENCIA
import cl.shazamrepara.app.data.Symptom.FUGA
import cl.shazamrepara.app.data.Symptom.GOLPETEO
import cl.shazamrepara.app.data.Symptom.NO_ENCIENDE
import cl.shazamrepara.app.data.Symptom.RUIDO_AGUDO
import cl.shazamrepara.app.data.Symptom.RUIDO_METALICO
import cl.shazamrepara.app.data.Symptom.SILBIDO
import cl.shazamrepara.app.data.Symptom.SOBRECALIENTA
import cl.shazamrepara.app.data.Symptom.VIBRACION
import cl.shazamrepara.app.data.Symptom.ZUMBIDO

/**
 * Catálogo de fallas del motor del auto (bencina, diésel o GLP).
 *
 * Los rangos de precio son referencias de mercado en Chile (repuesto genérico u original
 * económico + mano de obra de taller medio, 2025-2026). Sirven para detectar cobros
 * abusivos, no como cotización oficial.
 */
object KnowledgeBase {

    val generalTips: List<String> = listOf(
        "Pide el presupuesto por escrito, separando repuesto y mano de obra.",
        "Pide que te muestren la pieza que retiran: es la prueba de que la cambiaron.",
        "Con la luz de check engine encendida, pide el escaneo OBD y el código exacto (P0xxx) antes de autorizar cualquier cambio.",
        "Compara al menos dos presupuestos con el mismo nombre de repuesto y código.",
        "Desconfía si el mecánico cambia el diagnóstico en la misma visita sin una prueba nueva.",
        "Un repuesto original no siempre es necesario: pregunta por el equivalente genérico con garantía.",
        "Exige boleta o factura y garantía escrita de la reparación (mínimo 3 meses).",
        "Si el auto ya está perdiendo potencia o humo, no sigas andando: un motor reparado tarde cuesta el triple."
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
            id = "auto_radiador",
            name = "Fuga en radiador o mangueras",
            types = setOf(AUTO),
            symptoms = mapOf(FUGA to 1.0f, SOBRECALIENTA to 0.75f, SILBIDO to 0.3f),
            visual = mapOf(VisualSign.FUGA_AGUA to 0.9f, VisualSign.HUMO to 0.4f),
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
        fault(
            id = "auto_vacio_admision",
            name = "Fuga de vacío en la admisión",
            types = setOf(AUTO),
            symptoms = mapOf(SILBIDO to 0.9f, FALTA_POTENCIA to 0.6f, NO_ENCIENDE to 0.35f),
            visual = mapOf(VisualSign.PIEZA_SUELTA to 0.4f),
            safety = SEGURO,
            difficulty = FACIL,
            partName = "Manguera de admisión, empaquetadura o tapa de aceite",
            partHint = "Es de las fallas más baratas: pide que prueben con humo o con agua jabonosa antes de cambiar sensores.",
            partCost = 5_000..25_000,
            laborCost = 10_000..30_000,
            why = "Un silbido constante en ralentí con marcha irregular y check engine es aire entrando por una manguera rota.",
            check = "¿El silbido sube al acelerar y el motor tiembla en ralentí? ¿La manguera de admisión está rajada?"
        ),
        fault(
            id = "auto_sobrecalentamiento",
            name = "Sobrecalentamiento del motor (termostato o electroventilador)",
            types = setOf(AUTO),
            symptoms = mapOf(SOBRECALIENTA to 1.0f, ZUMBIDO to 0.3f),
            visual = mapOf(VisualSign.HUMO to 0.8f, VisualSign.OLOR_QUEMADO to 0.4f),
            safety = PELIGRO,
            difficulty = MEDIA,
            partName = "Termostato, electroventilador o sensor de temperatura",
            partHint = "Antes de cambiar el radiador, pide probar el termostato y que el electroventilador enciende: son las causas más comunes.",
            partCost = 15_000..90_000,
            laborCost = 20_000..60_000,
            why = "La aguja de temperatura sube y el ventilador no parte: el motor no está disipando calor.",
            check = "¿El electroventilador gira con el motor caliente y detenido? ¿La aguja sube solo en tacos?"
        )
    )

    val unknownFault: FaultTemplate = fault(
        id = "unknown",
        name = "Sin falla clara en el motor",
        types = emptySet(),
        symptoms = emptyMap(),
        safety = PRECAUCION,
        difficulty = MEDIA,
        partName = "Por definir",
        partHint = "Repite las mediciones en un lugar silencioso, con el capó abierto y el motor en la etapa donde falla (frío, ralentí o acelerando).",
        partCost = 0..0,
        laborCost = 0..0,
        why = "El sonido medido no coincide con ningún patrón de falla conocido; si el motor suena normal, no hay nada que reparar todavía.",
        check = "Describe cuándo falla exactamente (al partir en frío, en ralentí, al acelerar) y vuelve a medir."
    )
}
