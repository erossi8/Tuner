package com.example.tuner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor


class AudioWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), AudioProcessor {


    private var waveform: FloatArray = FloatArray(0)


    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 2f
        isAntiAlias = true
    }


    override fun process(audioEvent: AudioEvent): Boolean {

        waveform = audioEvent.floatBuffer.copyOf()

        postInvalidate()
        return true
    }


    override fun processingFinished() {}


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val h = height.toFloat()
        val w = width.toFloat()


        if (waveform.isEmpty()) return

        val centerY = h / 2
        val scale = h / 2
        val step = waveform.size.toFloat() / w

        var i = 0f

        while (i < w) {

            val index = (i * step).toInt()


            val y = centerY - (waveform.getOrNull(index) ?: 0f) * scale


            canvas.drawPoint(i, y, paint)
            i += 1f
        }
    }
}
