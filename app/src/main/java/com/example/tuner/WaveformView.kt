package com.example.tuner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor

// View personalizzata per visualizzare l'onda audio in tempo reale
class AudioWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), AudioProcessor {

    // Buffer contenente i valori dell'onda audio
    private var waveform: FloatArray = FloatArray(0)

    // Oggetto Paint per disegnare l'onda (blu, linee sottili, anti-alias)
    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 2f
        isAntiAlias = true
    }

    // Metodo chiamato ogni volta che arriva un nuovo frame audio
    override fun process(audioEvent: AudioEvent): Boolean {
        // Copia i dati dell'audio nel buffer waveform
        waveform = audioEvent.floatBuffer.copyOf()
        // Richiama il redraw della view (in modo asincrono)
        postInvalidate()
        return true
    }

    // Metodo chiamato quando termina il processamento audio (vuoto qui)
    override fun processingFinished() {}

    // Metodo per disegnare la view
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val h = height.toFloat()  // altezza view
        val w = width.toFloat()   // larghezza view

        // Se il buffer è vuoto non disegna nulla
        if (waveform.isEmpty()) return

        val centerY = h / 2       // centro verticale per disegnare l'onda
        val scale = h / 2         // scala per amplificare i valori dell'onda
        val step = waveform.size.toFloat() / w  // rapporto campioni/pixel

        var i = 0f
        // Scorre tutti i pixel orizzontali della view
        while (i < w) {
            // Ottiene l'indice del campione audio corrispondente al pixel i
            val index = (i * step).toInt()

            // Calcola la posizione verticale del punto dell'onda (centra e scala)
            val y = centerY - (waveform.getOrNull(index) ?: 0f) * scale

            // Disegna un punto singolo sull'onda
            canvas.drawPoint(i, y, paint)
            i += 1f
        }
    }
}
