package cl.shazamrepara.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import cl.shazamrepara.app.data.VibrationFeatures
import kotlin.math.sqrt

class VibrationRecorder(private val context: Context) {

    fun record(seconds: Int, onProgress: (Float) -> Unit): VibrationFeatures? {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return null
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return null

        val values = ArrayList<Float>(2048)
        val timestamps = ArrayList<Long>(2048)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                values.add(sqrt(x * x + y * y + z * z))
                timestamps.add(event.timestamp)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val durationMs = seconds * 1000L
        val started = SystemClock.elapsedRealtime()
        if (!manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST)) return null

        try {
            while (SystemClock.elapsedRealtime() - started < durationMs) {
                val elapsed = (SystemClock.elapsedRealtime() - started).toFloat()
                onProgress((elapsed / durationMs).coerceIn(0f, 1f))
                Thread.sleep(80)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } finally {
            manager.unregisterListener(listener)
        }

        if (values.size < 32) return null

        val spanNanos = (timestamps.last() - timestamps.first()).coerceAtLeast(1L)
        val sampleRate = ((timestamps.size - 1) * 1_000_000_000.0 / spanNanos).toFloat()
        val realSeconds = spanNanos / 1_000_000_000f

        return VibrationAnalyzer.analyze(values.toFloatArray(), sampleRate, realSeconds)
    }
}
