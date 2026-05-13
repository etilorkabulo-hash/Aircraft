package com.aircraftwar.pro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

class StyledEnemy(var x: Float, val mapLevel: Int = 0) {

    var y = -50f
    private val width = 40f
    private val height = 50f
    private val speed = 3f * (1 + mapLevel * 0.3f)
    private var rotation = 0f
    private var wingFlap = 0f

    fun update(screenHeight: Int) {
        y += speed
        rotation += 5f
        wingFlap = (wingFlap + 8) % 360
        if (rotation > 360) rotation = 0f
    }

    fun draw(canvas: Canvas) {
        when (mapLevel) {
            0 -> drawSpaceEnemy(canvas)
            1 -> drawNeonEnemy(canvas)
            2 -> drawFireEnemy(canvas)
            3 -> drawStormEnemy(canvas)
        }
    }

    private fun drawSpaceEnemy(canvas: Canvas) {
        val alienPaint = Paint().apply {
            color = Color.rgb(100, 200, 100)
            style = Paint.Style.FILL
        }
        
        val eyePaint = Paint().apply {
            color = Color.RED
        }

        canvas.drawOval(
            RectF(x - 30f, y - 15f, x + 30f, y + 15f),
            alienPaint
        )

        canvas.drawCircle(x, y - 8f, 12f, alienPaint)
        
        val lightPaint = Paint().apply {
            color = Color.YELLOW
            alpha = 200
        }
        canvas.drawCircle(x, y - 8f, 8f, lightPaint)

        canvas.drawCircle(x - 8f, y, 4f, eyePaint)
        canvas.drawCircle(x + 8f, y, 4f, eyePaint)

        val tentaclePaint = Paint().apply {
            color = Color.rgb(100, 200, 100)
            strokeWidth = 2.5f
        }
        canvas.drawLine(x - 25f, y + 12f, x - 30f, y + 25f, tentaclePaint)
        canvas.drawLine(x, y + 12f, x, y + 28f, tentaclePaint)
        canvas.drawLine(x + 25f, y + 12f, x + 30f, y + 25f, tentaclePaint)

        val auraPaint = Paint().apply {
            color = Color.argb(80, 100, 200, 100)
        }
        canvas.drawOval(
            RectF(x - 35f, y - 20f, x + 35f, y + 20f),
            auraPaint
        )
    }

    private fun drawNeonEnemy(canvas: Canvas) {
        val neonPaint = Paint().apply {
            color = Color.rgb(255, 50, 150)
            style = Paint.Style.FILL
        }
        
        val outlinePaint = Paint().apply {
            color = Color.rgb(255, 50, 150)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val fuselagePath = Path()
        fuselagePath.moveTo(x, y - 30f)
        fuselagePath.lineTo(x - 18f, y + 10f)
        fuselagePath.lineTo(x - 15f, y + 30f)
        fuselagePath.lineTo(x + 15f, y + 30f)
        fuselagePath.lineTo(x + 18f, y + 10f)
        fuselagePath.close()
        canvas.drawPath(fuselagePath, neonPaint)
        canvas.drawPath(fuselagePath, outlinePaint)

        canvas.drawCircle(x, y - 15f, 7f, neonPaint)
        val corePaint = Paint().apply {
            color = Color.rgb(255, 0, 255)
        }
        val pulseSize = 2 + (rotation / 360) * 3
        canvas.drawCircle(x, y - 15f, pulseSize, corePaint)

        val leftWing = Path()
        leftWing.moveTo(x - 15f, y - 5f)
        leftWing.lineTo(x - 45f, y + 8f)
        leftWing.lineTo(x - 18f, y + 15f)
        leftWing.close()
        canvas.drawPath(leftWing, neonPaint)
        canvas.drawPath(leftWing, outlinePaint)

        val rightWing = Path()
        rightWing.moveTo(x + 15f, y - 5f)
        rightWing.lineTo(x + 45f, y + 8f)
        rightWing.lineTo(x + 18f, y + 15f)
        rightWing.close()
        canvas.drawPath(rightWing, neonPaint)
        canvas.drawPath(rightWing, outlinePaint)

        canvas.drawCircle(x - 10f, y + 25f, 4f, corePaint)
        canvas.drawCircle(x + 10f, y + 25f, 4f, corePaint)
    }

    private fun drawFireEnemy(canvas: Canvas) {
        val firePaint = Paint().apply {
            color = Color.rgb(200, 50, 0)
            style = Paint.Style.FILL
        }
        
        val accentPaint = Paint().apply {
            color = Color.rgb(255, 150, 0)
            style = Paint.Style.FILL
        }

        val fuselagePath = Path()
        fuselagePath.moveTo(x, y - 35f)
        fuselagePath.lineTo(x - 20f, y + 8f)
        fuselagePath.lineTo(x - 18f, y + 32f)
        fuselagePath.lineTo(x + 18f, y + 32f)
        fuselagePath.lineTo(x + 20f, y + 8f)
        fuselagePath.close()
        canvas.drawPath(fuselagePath, firePaint)

        canvas.drawCircle(x, y - 18f, 8f, accentPaint)
        val eyePaint = Paint().apply { color = Color.RED }
        canvas.drawCircle(x, y - 18f, 3f, eyePaint)

        val leftWing = Path()
        leftWing.moveTo(x - 18f, y - 2f)
        leftWing.lineTo(x - 50f, y + 5f)
        leftWing.lineTo(x - 40f, y + 18f)
        leftWing.lineTo(x - 15f, y + 8f)
        leftWing.close()
        canvas.drawPath(leftWing, firePaint)

        val rightWing = Path()
        rightWing.moveTo(x + 18f, y - 2f)
        rightWing.lineTo(x + 50f, y + 5f)
        rightWing.lineTo(x + 40f, y + 18f)
        rightWing.lineTo(x + 15f, y + 8f)
        rightWing.close()
        canvas.drawPath(rightWing, firePaint)

        canvas.drawRect(
            RectF(x - 10f, y + 28f, x - 5f, y + 40f),
            firePaint
        )
        canvas.drawRect(
            RectF(x + 5f, y + 28f, x + 10f, y + 40f),
            firePaint
        )

        val flameLength = 12f + Math.sin(Math.toRadians(wingFlap.toDouble())).toFloat() * 6
        
        canvas.drawCircle(x - 12f, y + 30f, 5f, accentPaint)
        val leftFlamePath = Path()
        leftFlamePath.moveTo(x - 12f, y + 30f)
        leftFlamePath.lineTo(x - 16f, y + 30f + flameLength)
        leftFlamePath.lineTo(x - 8f, y + 30f + flameLength)
        leftFlamePath.close()
        canvas.drawPath(leftFlamePath, accentPaint)

        canvas.drawCircle(x + 12f, y + 30f, 5f, accentPaint)
        val rightFlamePath = Path()
        rightFlamePath.moveTo(x + 12f, y + 30f)
        rightFlamePath.lineTo(x + 16f, y + 30f + flameLength)
        rightFlamePath.lineTo(x + 8f, y + 30f + flameLength)
        rightFlamePath.close()
        canvas.drawPath(rightFlamePath, accentPaint)

        val heatAura = Paint().apply {
            color = Color.argb(100, 255, 100, 0)
        }
        canvas.drawOval(
            RectF(x - 30f, y - 32f, x + 30f, y + 35f),
            heatAura
        )
    }

    private fun drawStormEnemy(canvas: Canvas) {
        val stormPaint = Paint().apply {
            color = Color.rgb(100, 150, 255)
            style = Paint.Style.FILL
        }
        
        val boltPaint = Paint().apply {
            color = Color.rgb(255, 255, 100)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        val fuselagePath = Path()
        fuselagePath.moveTo(x, y - 32f)
        fuselagePath.lineTo(x + 18f, y - 10f)
        fuselagePath.lineTo(x + 20f, y + 12f)
        fuselagePath.lineTo(x + 8f, y + 30f)
        fuselagePath.lineTo(x - 8f, y + 30f)
        fuselagePath.lineTo(x - 20f, y + 12f)
        fuselagePath.lineTo(x - 18f, y - 10f)
        fuselagePath.close()
        canvas.drawPath(fuselagePath, stormPaint)
        canvas.drawPath(fuselagePath, boltPaint)

        canvas.drawCircle(x, y - 15f, 7f, stormPaint)
        val techPaint = Paint().apply {
            color = Color.rgb(0, 255, 255)
        }
        canvas.drawCircle(x, y - 15f, 4f, techPaint)

        val leftWing = Path()
        leftWing.moveTo(x - 18f, y - 8f)
        leftWing.quadTo(x - 45f, y + 2f, x - 48f, y + 20f)
        leftWing.lineTo(x - 25f, y + 22f)
        leftWing.quadTo(x - 20f, y + 10f, x - 12f, y)
        leftWing.close()
        canvas.drawPath(leftWing, stormPaint)
        canvas.drawPath(leftWing, boltPaint)

        val rightWing = Path()
        rightWing.moveTo(x + 18f, y - 8f)
        rightWing.quadTo(x + 45f, y + 2f, x + 48f, y + 20f)
        rightWing.lineTo(x + 25f, y + 22f)
        rightWing.quadTo(x + 20f, y + 10f, x + 12f, y)
        rightWing.close()
        canvas.drawPath(rightWing, stormPaint)
        canvas.drawPath(rightWing, boltPaint)

        val reactorPaint = Paint().apply {
            color = Color.rgb(100, 200, 255)
        }
        
        val ionLength = 10f + Math.sin(Math.toRadians(wingFlap.toDouble())).toFloat() * 5
        
        canvas.drawCircle(x - 12f, y + 28f, 4f, reactorPaint)
        val leftIon = Path()
        leftIon.moveTo(x - 12f, y + 28f)
        leftIon.lineTo(x - 14f, y + 28f + ionLength)
        leftIon.lineTo(x - 10f, y + 28f + ionLength)
        leftIon.close()
        canvas.drawPath(leftIon, reactorPaint)

        canvas.drawCircle(x + 12f, y + 28f, 4f, reactorPaint)
        val rightIon = Path()
        rightIon.moveTo(x + 12f, y + 28f)
        rightIon.lineTo(x + 14f, y + 28f + ionLength)
        rightIon.lineTo(x + 10f, y + 28f + ionLength)
        rightIon.close()
        canvas.drawPath(rightIon, reactorPaint)

        repeat(3) { i ->
            val angle = (i * 120 + wingFlap) * Math.PI / 180
            val ex = x + Math.cos(angle) * 40
            val ey = y + Math.sin(angle) * 25
            canvas.drawLine(x.toFloat(), y.toFloat(), ex.toFloat(), ey.toFloat(), boltPaint)
        }
    }

    fun collidesWith(bullet: StyledBullet): Boolean {
        val distance = Math.sqrt(
            Math.pow((x - bullet.x).toDouble(), 2.0) +
            Math.pow((y - bullet.y).toDouble(), 2.0)
        )
        return distance < 40.0
    }
}
