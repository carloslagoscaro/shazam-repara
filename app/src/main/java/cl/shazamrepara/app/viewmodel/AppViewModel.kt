package cl.shazamrepara.app.viewmodel

import cl.shazamrepara.app.data.DiagnosisResult
import cl.shazamrepara.app.data.EquipmentType
import cl.shazamrepara.app.data.SafetyLevel
import cl.shazamrepara.app.data.Symptom
import cl.shazamrepara.app.audio.AudioAnalyzer
import cl.shazamrepara.app.audio.AudioRecorder
import cl.shazamrepara.app.sensor.VibrationRecorder
import cl.shazamrepara.app.sensor.SensorManager  // placeholder, lo conectaremos después
import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import kotlin.runBlocking

class AppViewModel : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: State<AppState> = _state.asState()

    // Parámetros de grabación
    private val recordSeconds = 15

    fun startScan(equipment: EquipmentType) {
        viewModelScope.launch {
            val audioResult = recordAndAnalyzeAudio()
            val vibResult = recordAndAnalyzeVibration()
            // Señales visuales simuladas para poder compilar y probar el diagnóstico
            val visualSigns = setOf(VisualSign.FUGA_AGUA) // en producción vendrían de la cámara / usuario

            val result = DiagnosisEngine.diagnose(
                type = equipment,
                brand = "",
                model = "",
                serial = "",
                audio = audioResult,
                vibration = vibResult,
                signs = visualSigns
            )
            _state.value = _state.value.copy(result = result, isScanning = false)
        }
    }

    private fun recordAndAnalyzeAudio(): AudioAnalyzer.AudioFeatures? {
        return try {
            val recorder = AudioRecorder()
            var progress = 0f
            val samples = recorder.record(recordSeconds) { progress_ ->
                progress = progress_.coerceIn(0f, 1f)
            }
            AudioAnalyzer.analyze(samples, 44100)
        } catch (e: Exception) {
            null
        }
    }

    private fun recordAndAnalyzeVibration(): cl.shazamrepara.app.data.VibrationFeatures? {
        return try {
            // En producción real: VibrationRecorder con SensorManager
            // Por ahora simulamos una vibración ligera
            cl.shazamrepara.app.data.VibrationFeatures(
                rms = 0.45f,
                peak = 1.2f,
                dominantHz = 45f,
                spikeRate = 0.02f,
                seconds = recordSeconds.toFloat(),
                symptoms = mapOf(Symptom.VIBRACION to 0.6f),
                summary = "Vibración ligera detectada"
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class AppState(
    val equipment: EquipmentType? = null,
    val result: DiagnosisResult? = null,
    val isScanning: Boolean = false
) {
    fun selectEquipment(type: EquipmentType) = copy(equipment = type)
    fun scanAction() = copy(isScanning = true)
    fun scanComplete(result: DiagnosisResult) = copy(result = result, isScanning = false)
}