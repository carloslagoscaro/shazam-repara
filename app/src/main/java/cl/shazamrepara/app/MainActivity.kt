package cl.shazamrepara.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.shazamrepara.app.data.AudioFeatures
import cl.shazamrepara.app.data.DiagnosisEngine
import cl.shazamrepara.app.data.DiagnosisResult
import cl.shazamrepara.app.data.EquipmentType
import cl.shazamrepara.app.data.Symptom
import cl.shazamrepara.app.data.VibrationFeatures
import cl.shazamrepara.app.data.VisualSign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    var equipment by remember { mutableStateOf(EquipmentType.LAVADORA) }
    var result by remember { mutableStateOf<DiagnosisResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Shazam Repara",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Diagnóstico de fallas de autos y electrodomésticos",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona el equipo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        EquipmentType.values().forEach { type ->
            Button(
                onClick = {
                    equipment = type
                    result = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (equipment == type) "► ${type.label}" else type.label)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { result = diagnoseDemo(equipment) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Diagnosticar ahora")
        }

        val current = result
        if (current != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Resultado",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Causa más probable: ${current.top.fault.name}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Probabilidad: ${current.top.probability}%")
            Text(text = "Seguridad: ${current.safety.label}")
            Text(text = current.safetyReason)
            Text(text = "Repuesto: ${current.top.fault.partName}")
            Text(text = "Dificultad: ${current.top.fault.difficulty.label}")
            Text(
                text = "Costo estimado: $${current.totalMinClp} - $${current.totalMaxClp} CLP",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Otras causas posibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            current.ranked.drop(1).forEach { item ->
                Text(text = "• ${item.fault.name} (${item.probability}%)")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Qué revisar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            current.checks.forEach { check ->
                Text(text = "• $check")
            }
        }
    }
}

/**
 * Diagnóstico de demostración: usa el motor real con señales simuladas.
 * Las mediciones reales de audio, vibración y cámara se conectarán aquí.
 */
private fun diagnoseDemo(equipment: EquipmentType): DiagnosisResult {
    val symptoms = mapOf(
        Symptom.VIBRACION to 0.6f,
        Symptom.RUIDO_METALICO to 0.45f
    )
    val audio = AudioFeatures(
        rmsDb = -42f,
        peak = 0.35f,
        crest = 2.1f,
        dominantHz = 120f,
        spectralCentroidHz = 1450f,
        lowRatio = 0.4f,
        midRatio = 0.35f,
        highRatio = 0.25f,
        transientRate = 0.08f,
        zeroCrossRate = 0.03f,
        seconds = 10f,
        symptoms = symptoms,
        summary = "Ruido con componente metálica y vibración moderada"
    )
    val vibration = VibrationFeatures(
        rms = 0.42f,
        peak = 0.95f,
        dominantHz = 48f,
        spikeRate = 0.015f,
        seconds = 10f,
        symptoms = mapOf(Symptom.VIBRACION to 0.6f),
        summary = "Vibración sostenida de baja frecuencia"
    )
    val signs = setOf(VisualSign.PIEZA_SUELTA)

    return DiagnosisEngine.diagnose(
        type = equipment,
        brand = "",
        model = "",
        serial = "",
        audio = audio,
        vibration = vibration,
        signs = signs
    )
}