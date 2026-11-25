package cl.shazamrepara.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

enum class EnginePreset(
    val label: String,
    val detail: String,
    val idleRpm: Float,
    val maxRpm: Float,
    val cylinders: Int,
    val growl: Float
) {
    FOUR_INLINE("4 cilindros", "bencina, ralentí suave", 850f, 6500f, 4, 0.45f),
    V8("V8 americano", "ocho cilindros, motor grande", 650f, 6000f, 8, 0.75f),
    DIESEL("Diésel 1.9", "cuatro cilindros, golpeteo de diésel", 900f, 4600f, 4, 0.9f),
    TURBO("Deportivo turbo", "cuatro cilindros, sube rápido", 950f, 7500f, 4, 0.35f)
}

enum class EngineFault(val label: String, val description: String) {
    CORREA("Correa de accesorios", "chirrido agudo al acelerar"),
    RODAMIENTO("Rodamiento de alternador", "zumbido grave que sube con las RPM"),
    INYECTOR("Inyector / cascabeleo", "golpeteo irregular y pérdida de potencia"),
    VACIO("Fuga de vacío", "silbido constante en la admisión")
}

/**
 * Sintetiza un motor de combustión en tiempo real y lo reproduce con [AudioTrack].
 *
 * No usa archivos de audio: todo se genera muestra a muestra a partir de las RPM, por
 * lo que la app no necesita descargas ni licencias. También guarda las últimas muestras
 * para que el motor de diagnóstico pueda analizar el mismo sonido que escucha el usuario.
 */
class EngineSimulator {

    companion object {
        const val SAMPLE_RATE = 44100
        private const val CHUNK = 1024
        private const val HISTORY_SECONDS = 6
    }

    private var track: AudioTrack? = null
    private var thread: Thread? = null

    @Volatile private var running = false
    @Volatile private var pedal = 0f
    @Volatile private var preset = EnginePreset.FOUR_INLINE
    @Volatile private var faults: Set<EngineFault> = emptySet()
    @Volatile private var currentRpm = EnginePreset.FOUR_INLINE.idleRpm

    private val history = ShortArray(SAMPLE_RATE * HISTORY_SECONDS)
    private val historyLock = Any()
    private var historyPos = 0

    private var phase = 0.0
    private var chirpPhase = 0.0
    private var chirpMod = 0.0
    private var bearingPhase = 0.0
    private var whistlePhase = 0.0
    private var knockEnvelope = 0.0
    private var knockCountdown = 0
    private var knockLimit = 2200

    val rpm: Float get() = currentRpm

    fun isRunning(): Boolean = running

    fun setPedal(value: Float) {
        pedal = value.coerceIn(0f, 1f)
    }

    fun setPreset(value: EnginePreset) {
        preset = value
    }

    fun setFaults(value: Set<EngineFault>) {
        faults = value
    }

    fun start() {
        if (running) return
        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) return

        val audioTrack = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(max(minBuffer, CHUNK * 8))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (t: Throwable) {
            return
        }

        track = audioTrack
        running = true
        currentRpm = preset.idleRpm
        audioTrack.play()

        thread = Thread({
            val buffer = ShortArray(CHUNK)
            while (running) {
                for (i in 0 until CHUNK) buffer[i] = nextSample()
                try {
                    audioTrack.write(buffer, 0, CHUNK)
                } catch (t: Throwable) {
                    break
                }
                synchronized(historyLock) {
                    for (i in 0 until CHUNK) {
                        history[historyPos] = buffer[i]
                        historyPos = (historyPos + 1) % history.size
                    }
                }
            }
        }, "engine-sim").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        running = false
        thread?.join(300)
        thread = null
        track?.let { audio ->
            try {
                audio.stop()
            } catch (_: Throwable) {
            }
            audio.release()
        }
        track = null
        pedal = 0f
        currentRpm = preset.idleRpm
    }

    /** Copia las últimas [seconds] segundos de sonido generado, en el orden correcto. */
    fun capture(seconds: Int): ShortArray {
        val wanted = (SAMPLE_RATE * seconds).coerceAtMost(history.size)
        val output = ShortArray(wanted)
        synchronized(historyLock) {
            var index = (historyPos - wanted + history.size) % history.size
            for (i in 0 until wanted) {
                output[i] = history[index]
                index = (index + 1) % history.size
            }
        }
        return output
    }

    private fun nextSample(): Short {
        val config = preset
        val target = config.idleRpm + pedal * (config.maxRpm - config.idleRpm)
        currentRpm += (target - currentRpm) * 0.0025f
        if (currentRpm > config.maxRpm) currentRpm = config.maxRpm

        val load = ((currentRpm - config.idleRpm) / (config.maxRpm - config.idleRpm)).coerceIn(0f, 1f)
        // Motor de 4 tiempos: una explosión cada dos vueltas por cilindro.
        val firing = currentRpm / 60f * config.cylinders / 2f
        phase += firing / SAMPLE_RATE
        if (phase >= 1.0) phase -= 1.0

        var value = 0.0
        var harmonic = 1
        while (harmonic <= 14) {
            val weight = 1.0 / (harmonic.toDouble() * 1.15)
            value += weight * sin(2.0 * PI * phase * harmonic + harmonic * 0.3)
            harmonic++
        }
        // El motor suena más "ronco" con carga y en los motores grandes.
        val body = 0.22 + 0.20 * load
        value *= body * (0.8 + 0.4 * config.growl)

        // Ruido de admisión/escape: da textura y crece con las RPM.
        value += (Random.nextDouble() - 0.5) * (0.05 + 0.10 * load) * (0.6 + config.growl)

        // Correa patinando: tono agudo modulado.
        if (EngineFault.CORREA in faults) {
            chirpMod += 6.0 / SAMPLE_RATE
            val wobble = 0.6 + 0.4 * sin(2.0 * PI * chirpMod * 5.0)
            chirpPhase += 2600.0 / SAMPLE_RATE
            if (chirpPhase >= 1.0) chirpPhase -= 1.0
            value += 0.40 * wobble * sin(2.0 * PI * chirpPhase)
        }

        // Rodamiento del alternador: zumbido grave constante.
        if (EngineFault.RODAMIENTO in faults) {
            bearingPhase += (95.0 + 40.0 * load) / SAMPLE_RATE
            if (bearingPhase >= 1.0) bearingPhase -= 1.0
            value += 0.45 * sin(2.0 * PI * bearingPhase)
        }

        // Fuga de vacío: silbido fino y constante en la admisión.
        if (EngineFault.VACIO in faults) {
            whistlePhase += 4800.0 / SAMPLE_RATE
            if (whistlePhase >= 1.0) whistlePhase -= 1.0
            value += 0.38 * sin(2.0 * PI * whistlePhase)
        }

        // Inyector o cascabeleo: golpes cortos e irregulares sobre la explosión.
        if (EngineFault.INYECTOR in faults) {
            if (knockCountdown <= 0) {
                knockCountdown = knockLimit
                knockEnvelope = 0.65
                knockLimit = 1400 + Random.nextInt(2600)
            }
            knockCountdown--
            if (knockEnvelope > 0.0005) {
                value += knockEnvelope * (Random.nextDouble() * 2.0 - 1.0)
                knockEnvelope *= 0.985
            }
        }

        value = value.coerceIn(-1.0, 1.0)
        return (value * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
    }
}
