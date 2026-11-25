package cl.shazamrepara.app.audio

import cl.shazamrepara.app.data.AudioFeatures
import cl.shazamrepara.app.data.Symptom
import cl.shazamrepara.app.signal.Fft
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sqrt

object AudioAnalyzer {

    private const val FFT_SIZE = 4096
    private const val HOP = 2048

    fun analyze(samples: ShortArray, sampleRate: Int): AudioFeatures? {
        if (samples.size < FFT_SIZE) return null

        val n = samples.size
        val signal = DoubleArray(n) { samples[it] / 32768.0 }

        var peak = 0.0
        var sumSq = 0.0
        var zeroCrossings = 0
        for (i in signal.indices) {
            val v = signal[i]
            val a = abs(v)
            if (a > peak) peak = a
            sumSq += v * v
            if (i > 0 && (signal[i - 1] >= 0) != (v >= 0)) zeroCrossings++
        }
        val rms = sqrt(sumSq / n)
        if (rms <= 0.0) return null

        val window = DoubleArray(FFT_SIZE) { 0.5 - 0.5 * cos(2.0 * PI * it / (FFT_SIZE - 1)) }
        val re = DoubleArray(FFT_SIZE)
        val im = DoubleArray(FFT_SIZE)
        val spectrum = DoubleArray(FFT_SIZE / 2)
        val frameRms = ArrayList<Double>()

        var pos = 0
        var frames = 0
        while (pos + FFT_SIZE <= n) {
            var energy = 0.0
            for (i in 0 until FFT_SIZE) {
                val v = signal[pos + i] * window[i]
                re[i] = v
                im[i] = 0.0
                energy += v * v
            }
            frameRms.add(sqrt(energy / FFT_SIZE))
            Fft.transform(re, im)
            for (k in 0 until FFT_SIZE / 2) {
                spectrum[k] += sqrt(re[k] * re[k] + im[k] * im[k])
            }
            frames++
            pos += HOP
        }
        if (frames == 0) return null

        val binHz = sampleRate.toDouble() / FFT_SIZE
        var domMag = 0.0
        var domBin = 0
        var totalMag = 0.0
        var centroidSum = 0.0
        var low = 0.0
        var mid = 0.0
        var high = 0.0
        for (k in 1 until spectrum.size) {
            val freq = k * binHz
            val mag = spectrum[k] / frames
            if (freq < 20.0) continue
            totalMag += mag
            centroidSum += freq * mag
            if (mag > domMag) {
                domMag = mag
                domBin = k
            }
            when {
                freq < 250.0 -> low += mag
                freq < 1200.0 -> mid += mag
                else -> high += mag
            }
        }
        if (totalMag <= 0.0) return null

        val dominant = domBin * binHz
        val centroid = centroidSum / totalMag
        val bandTotal = low + mid + high + 1e-9
        val lowRatio = low / bandTotal
        val midRatio = mid / bandTotal
        val highRatio = high / bandTotal
        val meanMag = totalMag / (spectrum.size - 1)
        val peakiness = domMag / (meanMag + 1e-9)

        val sorted = frameRms.sorted()
        val median = sorted[sorted.size / 2]
        val transients = frameRms.count { it > median * 2.2 && it > 0.004 }
        val transientRate = transients.toDouble() / frameRms.size

        val crest = peak / (rms + 1e-9)
        val rmsDb = 20.0 * log10(rms + 1e-12)
        val zcr = zeroCrossings.toDouble() / n

        val symptoms = mutableMapOf<Symptom, Float>()

        val golpeteo = (transientRate * 2.4).coerceIn(0.0, 1.0) * if (dominant < 320.0) 1.0 else 0.6
        if (golpeteo > 0.25) symptoms[Symptom.GOLPETEO] = golpeteo.toFloat()

        // Un chirrido (pastilla, correa) es un tono estrecho y agudo. El ruido de un
        // motor o de un electrodoméstico normal reparte su energía en muchas
        // frecuencias, así que sin pico tonal no se marca chirrido.
        val tonoAgudo = peakiness > 8.0 && dominant in 1200.0..3200.0
        val chirrido = if (tonoAgudo) (0.45 + highRatio).coerceIn(0.0, 1.0) else 0.0
        if (chirrido > 0.3) symptoms[Symptom.CHIRRIDO] = chirrido.toFloat()

        // Un zumbido de rodamiento es un tono casi puro; el motor, aunque tenga su
        // frecuencia dominante en la misma zona, siempre trae su segundo armónico.
        val domBinMag = spectrum[domBin] / frames
        val secondHarmonic = spectrum.getOrNull(domBin * 2)?.div(frames) ?: 0.0
        val pureTone = secondHarmonic < domBinMag * 0.35
        if (lowRatio > 0.45 && dominant in 80.0..320.0 && pureTone) {
            symptoms[Symptom.ZUMBIDO] = (lowRatio * 1.5).coerceIn(0.0, 1.0).toFloat()
        }

        if (peakiness > 12.0 && dominant in 3500.0..8000.0 && highRatio > 0.15) {
            symptoms[Symptom.SILBIDO] = 0.75f
        }

        if (crest > 3.5 && highRatio > 0.2 && peakiness > 3.0 && transientRate > 0.04) {
            symptoms[Symptom.RUIDO_METALICO] = ((crest - 3.0) / 4.0).coerceIn(0.0, 1.0).toFloat()
        }

        val agudo = ((centroid - 2500.0) / 4000.0).coerceIn(0.0, 1.0) * (highRatio * 2.0)
        if (agudo > 0.4) symptoms[Symptom.RUIDO_AGUDO] = agudo.coerceIn(0.0, 1.0).toFloat()

        val summary = buildString {
            append("Sonido dominante ")
            append((dominant / 10.0).roundToInt() * 10)
            append(" Hz · nivel ")
            append(rmsDb.roundToInt())
            append(" dBFS · picos ")
            append(transients)
            append(" · agudos ")
            append((highRatio * 100).roundToInt())
            append("%")
        }

        return AudioFeatures(
            rmsDb = rmsDb.toFloat(),
            peak = peak.toFloat(),
            crest = crest.toFloat(),
            dominantHz = dominant.toFloat(),
            spectralCentroidHz = centroid.toFloat(),
            lowRatio = lowRatio.toFloat(),
            midRatio = midRatio.toFloat(),
            highRatio = highRatio.toFloat(),
            transientRate = transientRate.toFloat(),
            zeroCrossRate = zcr.toFloat(),
            seconds = n.toFloat() / sampleRate,
            symptoms = symptoms,
            summary = summary
        )
    }
}
