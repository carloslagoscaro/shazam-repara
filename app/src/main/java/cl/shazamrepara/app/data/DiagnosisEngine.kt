package cl.shazamrepara.app.data

import kotlin.math.roundToInt

object DiagnosisEngine {

    private data class Scored(
        val fault: FaultTemplate,
        val score: Float,
        val reasons: MutableList<String>
    )

    fun diagnose(
        type: EquipmentType,
        brand: String,
        model: String,
        serial: String,
        audio: AudioFeatures?,
        vibration: VibrationFeatures?,
        signs: Set<VisualSign>
    ): DiagnosisResult {
        val detected = mutableMapOf<Symptom, Float>()

        audio?.symptoms?.forEach { (symptom, confidence) ->
            if (confidence >= 0.2f) {
                detected[symptom] = maxOf(detected[symptom] ?: 0f, confidence)
            }
        }
        vibration?.symptoms?.forEach { (symptom, confidence) ->
            if (confidence >= 0.2f) {
                detected[symptom] = maxOf(detected[symptom] ?: 0f, confidence)
            }
        }
        visualSymptoms(signs).forEach { (symptom, confidence) ->
            detected[symptom] = maxOf(detected[symptom] ?: 0f, confidence)
        }

        val scored = KnowledgeBase.faults.mapNotNull { fault ->
            if (fault.types.isNotEmpty() && !fault.types.contains(type)) return@mapNotNull null

            var score = 0.05f
            val reasons = mutableListOf<String>()

            fault.symptoms.forEach { (symptom, weight) ->
                val confidence = detected[symptom] ?: 0f
                if (confidence > 0.15f) {
                    score += weight * confidence
                    reasons.add("${symptom.label} detectado (${(confidence * 100).roundToInt()}% de confianza)")
                }
            }
            fault.visual.forEach { (sign, weight) ->
                if (sign in signs) {
                    score += weight
                    reasons.add("Señal visual: ${sign.label.lowercase()}")
                }
            }
            if (fault.types.contains(type)) score *= 1.2f

            Scored(fault, score, reasons)
        }.filter { it.score > 0.12f }

        val best = if (scored.isEmpty()) {
            listOf(
                Scored(
                    KnowledgeBase.unknownFault,
                    1f,
                    mutableListOf("Ninguna señal coincide con los patrones conocidos")
                )
            )
        } else {
            scored.sortedByDescending { it.score }.take(4)
        }

        val total = best.sumOf { it.score.toDouble() }.toFloat().coerceAtLeast(0.01f)
        val ranked = best.map { item ->
            RankedFault(
                fault = item.fault,
                probability = ((item.score / total) * 100f).roundToInt().coerceIn(3, 96),
                reasons = item.reasons.distinct().take(4)
            )
        }

        val top = ranked.first()

        val dangerousSigns = setOf(VisualSign.CABLE_QUEMADO, VisualSign.OLOR_QUEMADO, VisualSign.HUMO)
        val visualDanger = signs.any { it in dangerousSigns }
        val highest = ranked.map { it.fault.safety }.maxByOrNull { it.rank } ?: SafetyLevel.PRECAUCION
        val safety = if (visualDanger) SafetyLevel.PELIGRO else highest

        val safetyReason = when {
            visualDanger ->
                "Marcaste señales de sobrecalentamiento eléctrico (cable quemado, olor o humo). No sigas usándolo hasta que lo revise un técnico."
            safety == SafetyLevel.PELIGRO ->
                "La causa más probable (${top.fault.name}) puede provocar daño grave, incendio o electrocución."
            safety == SafetyLevel.PRECAUCION ->
                "La causa más probable no es crítica todavía, pero puede empeorar y encarecer la reparación."
            else ->
                "La causa más probable no presenta riesgo inmediato si el equipo se usa con normalidad."
        }

        val checks = buildList {
            add(top.fault.check)
            add("Anota el ruido o la falla en video: es la mejor prueba si luego discutes el diagnóstico con un técnico.")
            if (audio == null) add("No grabaste audio: repetir la medición sube la confianza del resultado.")
            if (vibration == null) add("No registraste vibración: apoya el teléfono sobre el equipo y repite para afinar el diagnóstico.")
            if (signs.isEmpty()) add("No marcaste señales visuales: revisa fugas, cables y piezas sueltas antes de pagar una reparación.")
        }

        val tips = buildList {
            addAll(KnowledgeBase.generalTips)
            when (top.fault.difficulty) {
                Difficulty.FACIL ->
                    add("Esta falla es de mantenimiento básico: no pagues visita técnica sin intentarlo primero.")
                Difficulty.MEDIA ->
                    add("Pide que el técnico explique el paso a paso antes de autorizar el trabajo.")
                Difficulty.ESPECIALISTA ->
                    add("Exige técnico con certificación y garantía escrita; no autorices trabajo en el domicilio sin presupuesto.")
            }
            if (top.fault.partCostClp.first >= 60_000) {
                add("El repuesto es caro: pide el número de parte y cotízalo tú en una tienda de repuestos.")
            }
        }.distinct().take(8)

        return DiagnosisResult(
            equipmentLabel = type.label,
            brand = brand.trim(),
            model = model.trim(),
            serial = serial.trim(),
            ranked = ranked,
            safety = safety,
            safetyReason = safetyReason,
            partCostClp = top.fault.partCostClp,
            laborCostClp = top.fault.laborCostClp,
            checks = checks,
            tips = tips,
            audioSummary = audio?.summary,
            vibrationSummary = vibration?.summary,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun visualSymptoms(signs: Set<VisualSign>): Map<Symptom, Float> {
        val result = mutableMapOf<Symptom, Float>()
        fun add(symptom: Symptom, confidence: Float) {
            result[symptom] = maxOf(result[symptom] ?: 0f, confidence)
        }
        if (VisualSign.FUGA_AGUA in signs) add(Symptom.FUGA, 0.85f)
        if (VisualSign.FUGA_ACEITE in signs) add(Symptom.FUGA, 0.8f)
        if (VisualSign.PIEZA_SUELTA in signs) add(Symptom.GOLPETEO, 0.65f)
        if (VisualSign.HUMO in signs) add(Symptom.SOBRECALIENTA, 0.8f)
        if (VisualSign.CABLE_QUEMADO in signs) add(Symptom.SOBRECALIENTA, 0.7f)
        if (VisualSign.OLOR_QUEMADO in signs) add(Symptom.SOBRECALIENTA, 0.6f)
        if (VisualSign.PANTALLA_ERROR in signs) add(Symptom.NO_ENCIENDE, 0.35f)
        if (VisualSign.OXIDO in signs) add(Symptom.FUGA, 0.3f)
        return result
    }
}
