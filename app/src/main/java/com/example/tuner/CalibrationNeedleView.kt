package com.example.tuner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class CalibrationNeedleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var cents = 0f

    fun setCents(value: Float) {
        cents = value.coerceIn(-50f, 50f)
        invalidate()
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = min(width, height) * 0.45f
        val centerX = width / 2
        val centerY = height * 0.95f

        val paint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 6f
            style = Paint.Style.STROKE
            color = Color.DKGRAY
        }

        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rect, 180f, 180f, false, paint)

        paint.textSize = 24f
        paint.style = Paint.Style.FILL
        for (i in -50..50 step 10) {
            val angle = Math.toRadians((180 + (i + 50) * 1.8).toDouble())
            val x = (centerX + cos(angle) * (radius + 20)).toFloat()
            val y = (centerY + sin(angle) * (radius + 20)).toFloat()
            canvas.drawText(i.toString(), x - 20, y, paint)
        }

        val needleAngle = Math.toRadians((180 + (cents + 50) * 1.8).toDouble())
        val needleX = (centerX + cos(needleAngle) * radius).toFloat()
        val needleY = (centerY + sin(needleAngle) * radius).toFloat()

        val needlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.RED
            strokeWidth = 8f
        }
        canvas.drawLine(centerX, centerY, needleX, needleY, needlePaint)
    }
}