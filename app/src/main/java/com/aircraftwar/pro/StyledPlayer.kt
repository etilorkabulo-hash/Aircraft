package com.aircraftwar.pro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

class StyledPlayer(var x: Int, var y: Int) {
    private val width = 60
    private val height = 80
    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    fun draw(canvas: Canvas) {
        val path = Path()
        path.moveTo(x.toFloat(), (y - height / 2).toFloat())
        path.lineTo((x - width / 2).toFloat(), (y + height / 2).toFloat())
        path.lineTo((x + width / 2).toFloat(), (y + height / 2).toFloat())
        path.close()
        canvas.drawPath(path, paint)

        val cockpitPaint = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x.toFloat(), (y - height / 4).toFloat(), 5f, cockpitPaint)
    }

    fun getBounds(): FloatArray {
        return floatArrayOf(x - width / 2f, y - height / 2f, x + width / 2f, y + height / 2f)
    }
}
