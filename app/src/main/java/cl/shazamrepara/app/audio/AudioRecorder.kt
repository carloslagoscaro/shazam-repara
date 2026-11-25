package cl.shazamrepara.app.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.max

class AudioRecorder {

    companion object {
        const val SAMPLE_RATE = 44100
    }

    @SuppressLint("MissingPermission")
    fun record(seconds: Int, onProgress: (Float) -> Unit): ShortArray? {
        val sampleRate = SAMPLE_RATE
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) return null

        val bufferSize = max(minBuffer, sampleRate / 2)
        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2
            )
        } catch (t: Throwable) {
            return null
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return null
        }

        val total = sampleRate * seconds
        val output = ShortArray(total)
        val buffer = ShortArray(bufferSize)
        var read = 0

        return try {
            record.startRecording()
            while (read < total) {
                val count = record.read(buffer, 0, minOf(buffer.size, total - read))
                if (count <= 0) break
                System.arraycopy(buffer, 0, output, read, count)
                read += count
                onProgress((read.toFloat() / total).coerceIn(0f, 1f))
            }
            if (read > 0) output.copyOf(read) else null
        } catch (t: Throwable) {
            null
        } finally {
            try {
                record.stop()
            } catch (_: Throwable) {
            }
            record.release()
        }
    }
}
