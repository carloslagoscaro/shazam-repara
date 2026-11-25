package cl.shazamrepara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.shazamrepara.app.audio.AudioAnalyzer
import cl.shazamrepara.app.audio.EngineFault
import cl.shazamrepara.app.audio.EnginePreset
import cl.shazamrepara.app.audio.EngineSimulator
import cl.shazamrepara.app.data.DiagnosisEngine
import cl.shazamrepara.app.data.DiagnosisResult
import cl.shazamrepara.app.data.EquipmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen() {
    val engine = remember { EngineSimulator() }
    val scope = rememberCoroutineScope()

    var preset by remember { mutableStateOf(EnginePreset.FOUR_INLINE) }
    var faults by remember { mutableStateOf(setOf<EngineFault>()) }
    var running by remember { mutableStateOf(false) }
    var pressing by remember { mutableStateOf(false) }
    var rpm by remember { mutableStateOf(EnginePreset.FOUR_INLINE.idleRpm) }
    var analyzing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<DiagnosisResult?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { engine.stop() }
    }

    LaunchedEffect(running, preset) {
        engine.setPreset(preset)
        if (!running) rpm = preset.idleRpm
        while (running) {
            rpm = engine.rpm
            delay(90)
        }
    }

    LaunchedEffect(faults) {
        engine.setFaults(faults)
    }

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
            text = "Simulador de motor con fallas",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "1. Elige el motor",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        EnginePreset.values().forEach { option ->
            FilterChip(
                selected = preset == option,
                onClick = {
                    preset = option
                    result = null
                    message = null
                },
                enabled = !pressing,
                label = { Text("${option.label} · ${option.detail}") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "2. Enciende el motor y acelera",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = {
                if (running) {
                    engine.stop()
                    running = false
                    rpm = preset.idleRpm
                } else {
                    engine.start()
                    running = engine.isRunning()
                    if (!running) message = "No se pudo iniciar el audio del teléfono."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (running) "Apagar motor" else "Encender motor")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    color = if (pressing) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(running) {
                    detectTapGestures(
                        onPress = {
                            if (!running) {
                                message = "Primero enciende el motor."
                            } else {
                                engine.setPedal(1f)
                                pressing = true
                            }
                            tryAwaitRelease()
                            engine.setPedal(0f)
                            pressing = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (pressing) "ACELERANDO" else "Mantén presionado para acelerar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "${rpm.roundToInt()} RPM",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            progress = {
                ((rpm - preset.idleRpm) / (preset.maxRpm - preset.idleRpm)).coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "3. Activa fallas para simular el problema",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EngineFault.values().forEach { fault ->
                FilterChip(
                    selected = fault in faults,
                    onClick = {
                        faults = if (fault in faults) faults - fault else faults + fault
                        result = null
                        message = null
                    },
                    label = { Text(fault.label) }
                )
            }
        }
        if (faults.isNotEmpty()) {
            Text(
                text = faults.joinToString(" · ") { it.description },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                scope.launch {
                    analyzing = true
                    message = null
                    result = null
                    val samples = engine.capture(5)
                    val features = withContext(Dispatchers.Default) {
                        AudioAnalyzer.analyze(samples, EngineSimulator.SAMPLE_RATE)
                    }
                    if (features == null) {
                        message = "Casi no se captó sonido: enciende el motor y acelera un momento antes de diagnosticar."
                    } else {
                        result = withContext(Dispatchers.Default) {
                            DiagnosisEngine.diagnose(
                                type = EquipmentType.AUTO,
                                brand = "",
                                model = "",
                                serial = "",
                                audio = features,
                                vibration = null,
                                signs = emptySet()
                            )
                        }
                    }
                    analyzing = false
                }
            },
            enabled = !analyzing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (analyzing) "Analizando el sonido…" else "Diagnosticar este sonido")
        }

        message?.let { text ->
            Text(
                text = text,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        result?.let { current -> SimulatorResult(current) }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SimulatorResult(result: DiagnosisResult) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider()

        Text(
            text = "Resultado del diagnóstico",
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

        if (result.top.reasons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Por qué",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            result.top.reasons.forEach { Text(text = "• $it") }
        }

        result.audioSummary?.let { summary ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lo que se midió",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = "• Audio: $summary")
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
        result.checks.take(3).forEach { Text(text = "• $it") }
    }
}
