package com.aircraftwar.pro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class StyledEnemy(var x: Int, var y: Int) {
    private val width = 50
    private val height = 50
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val speed = 5
    private var health = 2

    fun update() {
        y += speed
    }

    fun draw(canvas: Canvas) {
        canvas.drawRect(
            (x - width / 2).toFloat(),
            (y - height / 2).toFloat(),
            (x + width / 2).toFloat(),
            (y + height / 2).toFloat(),
            paint
        )

        val eyePaint = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }
        canvas.drawCircle((x - 10).toFloat(), (y - 5).toFloat(), 3f, eyePaint)
        canvas.drawCircle((x + 10).toFloat(), (y - 5).toFloat(), 3f, eyePaint)
    }

    fun collidesWith(bullet: StyledBullet): Boolean {
        val enemyBounds = FloatArray(4) { i ->
            when (i) {
                0 -> (x - width / 2).toFloat()
                1 -> (y - height / 2).toFloat()
                2 -> (x + width / 2).toFloat()
                else -> (y + height / 2).toFloat()
            }
        }
        val bulletBounds = bullet.getBounds()
        return enemyBounds[0] < bulletBounds[2] && enemyBounds[2] > bulletBounds[0] &&
                enemyBounds[1] < bulletBounds[3] && enemyBounds[3] > bulletBounds[1]
    }

    fun takeDamage() {
        health--
    }

    fun isDead(): Boolean = health <= 0

    fun getBounds(): FloatArray {
        return floatArrayOf(
            (x - width / 2).toFloat(),
            (y - height / 2).toFloat(),
            (x + width / 2).toFloat(),
            (y + height / 2).toFloat()
        )
    }
}
