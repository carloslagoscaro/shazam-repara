package cl.shazamrepara.app.sensor

import cl.shazamrepara.app.data.Symptom
import cl.shazamrepara.app.data.VibrationFeatures
import cl.shazamrepara.app.signal.Fft
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

object VibrationAnalyzer {

    private const val FFT_SIZE = 1024

    fun analyze(values: FloatArray, sampleRate: Float, seconds: Float): VibrationFeatures? {
        if (values.size < 32 || sampleRate <= 0f) return null

        val mean = values.average().toFloat()
        var sumSq = 0.0
        var peak = 0.0
        val centered = FloatArray(values.size)
        for (i in values.indices) {
            val v = values[i] - mean
            centered[i] = v
            sumSq += v.toDouble() * v
            if (abs(v) > peak) peak = abs(v).toDouble()
        }
        val rms = sqrt(sumSq / values.size).toFloat()
        if (rms <= 0f) return null

        val re = DoubleArray(FFT_SIZE)
        val im = DoubleArray(FFT_SIZE)
        val count = minOf(FFT_SIZE, centered.size)
        for (i in 0 until count) re[i] = centered[i].toDouble()
        Fft.transform(re, im)

        val binHz = sampleRate / FFT_SIZE
        var domMag = 0.0
        var domFreq = 0f
        for (k in 1 until FFT_SIZE / 2) {
            val mag = sqrt(re[k] * re[k] + im[k] * im[k])
            val freq = k * binHz
            if (freq < 2f) continue
            if (mag > domMag) {
                domMag = mag
                domFreq = freq
            }
        }

        val spikeThreshold = maxOf(rms * 3f, 0.12f)
        val spikes = centered.count { abs(it) > spikeThreshold }
        val spikeRate = spikes.toFloat() / values.size

        val symptoms = mutableMapOf<Symptom, Float>()

        val vibracion = (rms / 2.5f).coerceIn(0f, 1f)
        if (vibracion > 0.12f) symptoms[Symptom.VIBRACION] = vibracion

        val golpeteo = (spikeRate * 12f).coerceIn(0f, 1f)
        if (golpeteo > 0.2f) symptoms[Symptom.GOLPETEO] = golpeteo

        if (domFreq in 15f..140f && rms > 0.35f) {
            symptoms[Symptom.ZUMBIDO] = (rms / 2f).coerceIn(0.4f, 1f)
        }
        if (spikeRate > 0.03f && rms > 1.0f) {
            symptoms[Symptom.RUIDO_METALICO] = (spikeRate * 10f).coerceIn(0f, 1f)
        }

        val summary = buildString {
            append("Vibración RMS ")
            append((rms * 100).roundToInt() / 100f)
            append(" m/s² · pico ")
            append((peak * 10).roundToInt() / 10f)
            append(" · frecuencia ")
            append(domFreq.roundToInt())
            append(" Hz · impactos ")
            append((spikeRate * seconds).roundToInt())
        }

        return VibrationFeatures(
            rms = rms,
            peak = peak.toFloat(),
            dominantHz = domFreq,
            spikeRate = spikeRate,
            seconds = seconds,
            symptoms = symptoms,
            summary = summary
        )
    }
}
