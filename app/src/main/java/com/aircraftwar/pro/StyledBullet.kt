package com.aircraftwar.pro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class StyledBullet(var x: Int, var y: Int) {
    private val width = 10
    private val height = 20
    private val paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }
    private val speed = 15

    fun update() {
        y -= speed
    }

    fun draw(canvas: Canvas) {
        canvas.drawRect(
            (x - width / 2).toFloat(),
            (y - height / 2).toFloat(),
            (x + width / 2).toFloat(),
            (y + height / 2).toFloat(),
            paint
        )

        val glowPaint = Paint().apply {
            color = Color.argb(100, 255, 255, 0)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(
            (x - width / 2 - 3).toFloat(),
            (y - height / 2 - 3).toFloat(),
            (x + width / 2 + 3).toFloat(),
            (y + height / 2 + 3).toFloat(),
            glowPaint
        )
    }

    fun collidesWith(enemy: StyledEnemy): Boolean {
        val bulletBounds = getBounds()
        val enemyBounds = enemy.getBounds()
        return bulletBounds[0] < enemyBounds[2] && bulletBounds[2] > enemyBounds[0] &&
                bulletBounds[1] < enemyBounds[3] && bulletBounds[3] > enemyBounds[1]
    }

    fun getBounds(): FloatArray {
        return floatArrayOf(
            (x - width / 2).toFloat(),
            (y - height / 2).toFloat(),
            (x + width / 2).toFloat(),
            (y + height / 2).toFloat()
        )
    }
}
