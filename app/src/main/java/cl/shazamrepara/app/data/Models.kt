package cl.shazamrepara.app.data

enum class EquipmentType(val label: String, val hint: String) {
    AUTO("Auto / camioneta", "motor, frenos, dirección, bomba de agua"),
    LAVADORA("Lavadora", "carga superior o frontal"),
    REFRIGERADOR("Refrigerador", "no frost o convencional"),
    SECADORA("Secadora de ropa", "a tambor"),
    LAVAVAJILLAS("Lavavajillas", "lava loza"),
    AIRE("Aire acondicionado", "split o de ventana"),
    MICROONDAS("Microondas", "horno de microondas"),
    CALEFON("Calefón / termo eléctrico", "agua caliente sanitaria"),
    ASPIRADORA("Aspiradora", "doméstica"),
    OTRO("Otro equipo del hogar", "ventilador, bomba, etc.");

    companion object {
        fun fromName(name: String?): EquipmentType = values().firstOrNull { it.name == name } ?: OTRO
    }
}

enum class SafetyLevel(val label: String, val rank: Int, val advice: String) {
    SEGURO(
        "Seguro",
        0,
        "Puedes seguir usando el equipo mientras planificas la reparación."
    ),
    PRECAUCION(
        "Precaución",
        1,
        "Úsalo solo lo necesario hasta repararlo. Evita cargas altas, no lo dejes funcionando sin supervisión."
    ),
    PELIGRO(
        "Peligro",
        2,
        "Deja de usarlo ahora. Desconéctalo de la red eléctrica y, si hay olor a quemado o humo, corta la energía desde el automático."
    )
}

enum class Difficulty(val label: String, val detail: String) {
    FACIL("Simple", "Puedes hacerlo tú con herramientas básicas."),
    MEDIA("Intermedia", "Requiere herramientas o algo de experiencia; si dudas, llama a un técnico."),
    ESPECIALISTA(
        "Solo especialista",
        "Requiere técnico con herramientas y certificación. No lo intentes tú: hay riesgo eléctrico, de gas o de daño mayor."
    )
}

enum class VisualSign(val label: String) {
    FUGA_AGUA("Fuga de agua"),
    FUGA_ACEITE("Fuga de aceite o refrigerante"),
    CABLE_QUEMADO("Cable o enchufe quemado"),
    OLOR_QUEMADO("Olor a quemado"),
    OXIDO("Óxido o corrosión"),
    HUMO("Humo o chispas"),
    PIEZA_SUELTA("Pieza suelta o golpeada"),
    GOLPE("Golpe o abolladura"),
    PANTALLA_ERROR("Código de error en pantalla"),
    NO_HAY_SENALES("Sin señales visibles")
}

enum class Symptom(val label: String) {
    GOLPETEO("Golpeteo o traqueteo"),
    CHIRRIDO("Chirrido"),
    ZUMBIDO("Zumbido"),
    SILBIDO("Silbido"),
    RUIDO_METALICO("Ruido metálico"),
    VIBRACION("Vibración"),
    NO_ENCIENDE("No enciende"),
    NO_ENFRIA("No enfría o no calienta"),
    NO_DRENA("No drena o no centrifuga"),
    FALTA_POTENCIA("Falta de potencia"),
    SOBRECALIENTA("Se sobrecalienta"),
    FUGA("Fuga de líquido"),
    RUIDO_AGUDO("Ruido agudo constante")
}

data class AudioFeatures(
    val rmsDb: Float,
    val peak: Float,
    val crest: Float,
    val dominantHz: Float,
    val spectralCentroidHz: Float,
    val lowRatio: Float,
    val midRatio: Float,
    val highRatio: Float,
    val transientRate: Float,
    val zeroCrossRate: Float,
    val seconds: Float,
    val symptoms: Map<Symptom, Float>,
    val summary: String
)

data class VibrationFeatures(
    val rms: Float,
    val peak: Float,
    val dominantHz: Float,
    val spikeRate: Float,
    val seconds: Float,
    val symptoms: Map<Symptom, Float>,
    val summary: String
)

data class FaultTemplate(
    val id: String,
    val name: String,
    val types: Set<EquipmentType>,
    val symptoms: Map<Symptom, Float>,
    val visual: Map<VisualSign, Float>,
    val safety: SafetyLevel,
    val difficulty: Difficulty,
    val partName: String,
    val partHint: String,
    val partCostClp: IntRange,
    val laborCostClp: IntRange,
    val why: String,
    val check: String
)

data class RankedFault(
    val fault: FaultTemplate,
    val probability: Int,
    val reasons: List<String>
)

data class DiagnosisResult(
    val equipmentLabel: String,
    val brand: String,
    val model: String,
    val serial: String,
    val ranked: List<RankedFault>,
    val safety: SafetyLevel,
    val safetyReason: String,
    val partCostClp: IntRange,
    val laborCostClp: IntRange,
    val checks: List<String>,
    val tips: List<String>,
    val audioSummary: String?,
    val vibrationSummary: String?,
    val createdAt: Long
) {
    val top: RankedFault get() = ranked.first()
    val totalMinClp: Int get() = partCostClp.first + laborCostClp.first
    val totalMaxClp: Int get() = partCostClp.last + laborCostClp.last
}

data class HistoryEntry(
    val createdAt: Long,
    val equipment: String,
    val title: String,
    val safety: String,
    val probability: Int,
    val totalMinClp: Int,
    val totalMaxClp: Int
)
