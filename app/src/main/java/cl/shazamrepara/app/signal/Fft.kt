package cl.shazamrepara.app.signal

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** FFT iterativa radix-2 en el lugar. El tamaño de los arreglos debe ser potencia de 2. */
object Fft {

    fun transform(re: DoubleArray, im: DoubleArray) {
        val n = re.size
        require(n == im.size && n and (n - 1) == 0) { "El tamaño debe ser potencia de 2" }

        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j or bit
            if (i < j) {
                val tr = re[i]; re[i] = re[j]; re[j] = tr
                val ti = im[i]; im[i] = im[j]; im[j] = ti
            }
        }

        var len = 2
        while (len <= n) {
            val angle = -2.0 * PI / len
            val wRe = cos(angle)
            val wIm = sin(angle)
            var start = 0
            while (start < n) {
                var curRe = 1.0
                var curIm = 0.0
                val half = len / 2
                for (k in 0 until half) {
                    val aRe = re[start + k]
                    val aIm = im[start + k]
                    val bRe = re[start + k + half] * curRe - im[start + k + half] * curIm
                    val bIm = re[start + k + half] * curIm + im[start + k + half] * curRe
                    re[start + k] = aRe + bRe
                    im[start + k] = aIm + bIm
                    re[start + k + half] = aRe - bRe
                    im[start + k + half] = aIm - bIm
                    val nextRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe
                    curRe = nextRe
                }
                start += len
            }
            len = len shl 1
        }
    }
}
