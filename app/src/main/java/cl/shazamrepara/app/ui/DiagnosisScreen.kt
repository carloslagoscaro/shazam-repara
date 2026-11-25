package cl.shazamrepara.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.shazamrepara.app.audio.AudioAnalyzer
import cl.shazamrepara.app.audio.AudioRecorder
import cl.shazamrepara.app.data.AudioFeatures
import cl.shazamrepara.app.data.DiagnosisEngine
import cl.shazamrepara.app.data.DiagnosisResult
import cl.shazamrepara.app.data.EquipmentType
import cl.shazamrepara.app.data.VibrationFeatures
import cl.shazamrepara.app.data.VisualSign
import cl.shazamrepara.app.sensor.VibrationRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private const val VIBRATION_SECONDS = 10
private const val AUDIO_SECONDS = 10

private sealed interface ScanStatus {
    data object Idle : ScanStatus
    data class Running(val phase: String, val progress: Float) : ScanStatus
    data class Done(val result: DiagnosisResult, val notes: List<String>) : ScanStatus
    data class Error(val message: String) : ScanStatus
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var signs by remember { mutableStateOf(setOf<VisualSign>()) }
    var status by remember { mutableStateOf<ScanStatus>(ScanStatus.Idle) }

    fun scan(withAudio: Boolean) {
        scope.launch {
            status = ScanStatus.Running("Preparando sensores…", 0f)
            status = performScan(context, EquipmentType.AUTO, signs, withAudio) { phase, progress ->
                // Los avisos de progreso llegan desde hilos de grabación y pueden
                // ejecutarse después de que el escaneo ya terminó: si el estado
                // dejó de ser Running, se ignoran para no tapar el resultado.
                mainHandler.post {
                    if (status is ScanStatus.Running) {
                        status = ScanStatus.Running(phase, progress)
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> scan(granted) }

    fun startScan() {
        if (status is ScanStatus.Running) return
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) scan(true) else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val running = status is ScanStatus.Running

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Shazam Repara",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Diagnóstico del motor del auto",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "1. Marca lo que ves (opcional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VisualSign.values().forEach { sign ->
                FilterChip(
                    selected = sign in signs,
                    onClick = {
                        signs = if (sign in signs) signs - sign else signs + sign
                        if (!running) status = ScanStatus.Idle
                    },
                    enabled = !running,
                    label = { Text(sign.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "2. Graba con el motor encendido y el capó abierto. Primero apoya el teléfono " +
                "sobre la carrocería (${VIBRATION_SECONDS} s de vibración) y después acerca el " +
                "micrófono al motor (${AUDIO_SECONDS} s de audio).",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { startScan() },
            enabled = !running,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (running) "Escaneando…" else "Grabar y diagnosticar")
        }

        when (val current = status) {
            ScanStatus.Idle -> Unit

            is ScanStatus.Running -> {
                LinearProgressIndicator(
                    progress = { current.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = current.phase,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${(current.progress * 100).roundToInt()} %",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            is ScanStatus.Error -> {
                Text(
                    text = current.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is ScanStatus.Done -> ResultView(current.result, current.notes)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResultView(result: DiagnosisResult, notes: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider()

        Text(
            text = "Resultado",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Causa más probable: ${result.top.fault.name}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Probabilidad: ${result.top.probability}%")
        Text(text = "Seguridad: ${result.safety.label}", fontWeight = FontWeight.Bold)
        Text(text = result.safetyReason)
        Text(text = "Repuesto: ${result.top.fault.partName}")
        Text(text = result.top.fault.partHint)
        Text(text = "Dificultad: ${result.top.fault.difficulty.label}")
        Text(
            text = "Costo estimado: $${result.totalMinClp} - $${result.totalMaxClp} CLP",
            fontWeight = FontWeight.Bold
        )

        if (result.top.reasons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Por qué",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            result.top.reasons.forEach { reason -> Text(text = "• $reason") }
        }

        val audioSummary = result.audioSummary
        val vibrationSummary = result.vibrationSummary
        if (audioSummary != null || vibrationSummary != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lo que se midió",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (audioSummary != null) Text(text = "• Audio: $audioSummary")
            if (vibrationSummary != null) Text(text = "• Vibración: $vibrationSummary")
        }

        if (notes.isNotEmpty()) {
            notes.forEach { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (result.ranked.size > 1) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Otras causas posibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            result.ranked.drop(1).forEach { item ->
                Text(text = "• ${item.fault.name} (${item.probability}%)")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Qué revisar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        result.checks.forEach { check -> Text(text = "• $check") }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Consejos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        result.tips.forEach { tip -> Text(text = "• $tip") }
    }
}

/**
 * Mide vibración y audio reales del equipo y los envía al motor de diagnóstico.
 * Si un sensor falla o falta el permiso, se avisa en [notes] y se diagnostica con
 * las señales que sí se obtuvieron.
 */
private suspend fun performScan(
    context: Context,
    equipment: EquipmentType,
    signs: Set<VisualSign>,
    withAudio: Boolean,
    onProgress: (String, Float) -> Unit
): ScanStatus {
    var vibration: VibrationFeatures? = null
    var audio: AudioFeatures? = null
    var audioNote: String? = null

    onProgress("Paso 1 de 2 · midiendo vibración (${VIBRATION_SECONDS} s)", 0f)
    vibration = withContext(Dispatchers.IO) {
        try {
            VibrationRecorder(context.applicationContext).record(VIBRATION_SECONDS) { fraction ->
                onProgress(
                    "Paso 1 de 2 · midiendo vibración (${VIBRATION_SECONDS} s)",
                    (fraction * 0.5f).coerceIn(0f, 0.5f)
                )
            }
        } catch (t: Throwable) {
            null
        }
    }

    if (withAudio) {
        onProgress("Paso 2 de 2 · grabando audio (${AUDIO_SECONDS} s)", 0.5f)
        val samples = withContext(Dispatchers.IO) {
            try {
                AudioRecorder().record(AUDIO_SECONDS) { fraction ->
                    onProgress(
                        "Paso 2 de 2 · grabando audio (${AUDIO_SECONDS} s)",
                        (0.5f + fraction * 0.5f).coerceIn(0.5f, 1f)
                    )
                }
            } catch (t: Throwable) {
                null
            }
        }

        audio = if (samples == null) {
            audioNote = "No se pudo abrir el micrófono. Revisa que otra app no lo esté usando y vuelve a intentar."
            null
        } else {
            withContext(Dispatchers.Default) {
                AudioAnalyzer.analyze(samples, AudioRecorder.SAMPLE_RATE)
            }
        }
        if (samples != null && audio == null) {
            audioNote = "El micrófono captó muy poco sonido: acerca el teléfono al equipo y repite la grabación."
        }
    } else {
        audioNote = "Sin permiso de micrófono: el diagnóstico se basa solo en vibración y señales visuales."
    }

    onProgress("Analizando señales…", 1f)

    val result = try {
        DiagnosisEngine.diagnose(
            type = equipment,
            brand = "",
            model = "",
            serial = "",
            audio = audio,
            vibration = vibration,
            signs = signs
        )
    } catch (t: Throwable) {
        return ScanStatus.Error("No se pudo analizar la medición: ${t.message ?: "error desconocido"}")
    }

    val notes = buildList {
        audioNote?.let { add(it) }
        if (vibration == null) {
            add("No se pudo leer el acelerómetro: el resultado no incluye vibración.")
        }
        if (audio == null && vibration == null && signs.isEmpty()) {
            add("No se obtuvo ninguna señal: el resultado es genérico. Repite la medición con el equipo encendido.")
        }
    }

    return ScanStatus.Done(result, notes)
}
