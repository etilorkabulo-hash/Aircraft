package com.aircraftwar.pro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

class StyledPlayer(var x: Float, var y: Float, var mapLevel: Int = 0) {

    private val width = 60f
    private val height = 80f
    private val speed = 12f
    private var targetX = x
    private var isHit = false
    private var hitTimer = 0
    private var wingFlap = 0f

    fun update() {
        if (x < targetX) {
            x += speed
        } else if (x > targetX) {
            x -= speed
        }
        
        wingFlap = (wingFlap + 5) % 360
        
        if (isHit) {
            hitTimer++
            if (hitTimer > 10) {
                isHit = false
                hitTimer = 0
            }
        }
    }

    fun moveTo(newX: Float) {
        targetX = newX.coerceIn(width / 2, 10000f - width / 2)
    }

    fun draw(canvas: Canvas) {
        when (mapLevel) {
            0 -> drawSpaceJet(canvas)
            1 -> drawNeonJet(canvas)
            2 -> drawFireJet(canvas)
            3 -> drawStormJet(canvas)
        }
    }

    private fun drawSpaceJet(canvas: Canvas) {
        val fuselagePaint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.FILL
        }
        
        canvas.drawOval(
            RectF(x - 15f, y - 35f, x + 15f, y + 35f),
            fuselagePaint
        )

        val cockpitPaint = Paint().apply {
            color = Color.argb(255, 0, 200, 255)
        }
        canvas.drawCircle(x, y - 25f, 12f, cockpitPaint)
        
        val windowPaint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x, y - 25f, 6f, windowPaint)

        val wingPaint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.FILL
        }
        
        val leftWingPath = Path()
        leftWingPath.moveTo(x - 15f, y - 10f)
        leftWingPath.lineTo(x - 60f, y)
        leftWingPath.lineTo(x - 50f, y + 8f)
        leftWingPath.lineTo(x - 15f, y + 5f)
        leftWingPath.close()
        canvas.drawPath(leftWingPath, wingPaint)
        
        val rightWingPath = Path()
        rightWingPath.moveTo(x + 15f, y - 10f)
        rightWingPath.lineTo(x + 60f, y)
        rightWingPath.lineTo(x + 50f, y + 8f)
        rightWingPath.lineTo(x + 15f, y + 5f)
        rightWingPath.close()
        canvas.drawPath(rightWingPath, wingPaint)

        val tailPaint = Paint().apply {
            color = Color.argb(200, 0, 255, 255)
        }
        canvas.drawRect(
            RectF(x - 8f, y + 35f, x + 8f, y + 50f),
            tailPaint
        )

        canvas.drawRect(
            RectF(x - 3f, y + 45f, x + 3f, y + 55f),
            tailPaint
        )

        val reactorPaint = Paint().apply {
            color = Color.rgb(255, 200, 0)
            style = Paint.Style.FILL
        }
        val flameHeight = 15f + Math.sin(Math.toRadians(wingFlap.toDouble())).toFloat() * 5
        
        canvas.drawCircle(x - 12f, y + 40f, 6f, reactorPaint)
        canvas.drawRect(
            RectF(x - 14f, y + 40f, x - 10f, y + 40f + flameHeight),
            reactorPaint
        )
        
        canvas.drawCircle(x + 12f, y + 40f, 6f, reactorPaint)
        canvas.drawRect(
            RectF(x + 10f, y + 40f, x + 14f, y + 40f + flameHeight),
            reactorPaint
        )

        if (isHit) {
            val shieldPaint = Paint().apply {
                color = Color.argb(150, 255, 0, 0)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(x, y, 50f, shieldPaint)
        }
    }

    private fun drawNeonJet(canvas: Canvas) {
        val outlinePaint = Paint().apply {
            color = Color.rgb(255, 0, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        
        val fillPaint = Paint().apply {
            color = Color.rgb(150, 0, 150)
            style = Paint.Style.FILL
        }

        val fuselagePath = Path()
        fuselagePath.moveTo(x, y - 40f)
        fuselagePath.lineTo(x - 20f, y + 20f)
        fuselagePath.lineTo(x - 15f, y + 40f)
        fuselagePath.lineTo(x + 15f, y + 40f)
        fuselagePath.lineTo(x + 20f, y + 20f)
        fuselagePath.close()
        canvas.drawPath(fuselagePath, fillPaint)
        canvas.drawPath(fuselagePath, outlinePaint)

        canvas.drawCircle(x, y - 20f, 8f, fillPaint)
        canvas.drawCircle(x, y - 20f, 8f, outlinePaint)

        val leftDeltaWing = Path()
        leftDeltaWing.moveTo(x - 15f, y - 5f)
        leftDeltaWing.lineTo(x - 55f, y + 10f)
        leftDeltaWing.lineTo(x - 20f, y + 25f)
        leftDeltaWing.close()
        canvas.drawPath(leftDeltaWing, fillPaint)
        canvas.drawPath(leftDeltaWing, outlinePaint)

        val rightDeltaWing = Path()
        rightDeltaWing.moveTo(x + 15f, y - 5f)
        rightDeltaWing.lineTo(x + 55f, y + 10f)
        rightDeltaWing.lineTo(x + 20f, y + 25f)
        rightDeltaWing.close()
        canvas.drawPath(rightDeltaWing, fillPaint)
        canvas.drawPath(rightDeltaWing, outlinePaint)

        val reactorPaint = Paint().apply {
            color = Color.rgb(0, 255, 255)
        }
        canvas.drawCircle(x - 12f, y + 35f, 5f, reactorPaint)
        canvas.drawCircle(x + 12f, y + 35f, 5f, reactorPaint)

        val trailPaint = Paint().apply {
            color = Color.rgb(255, 0, 255)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val trailLength = 20f + Math.sin(Math.toRadians(wingFlap.toDouble())).toFloat() * 10
        canvas.drawLine(x - 8f, y + 40f, x - 8f, y + 40f + trailLength, trailPaint)
        canvas.drawLine(x + 8f, y + 40f, x + 8f, y + 40f + trailLength, trailPaint)
    }

    private fun drawFireJet(canvas: Canvas) {
        val bodyPaint = Paint().apply {
            color = Color.rgb(200, 50, 0)
            style = Paint.Style.FILL
        }
        
        val accentPaint = Paint().apply {
            color = Color.rgb(255, 150, 0)
            style = Paint.Style.FILL
        }

        val fuselagePath = Path()
        fuselagePath.moveTo(x, y - 45f)
        fuselagePath.lineTo(x - 25f, y + 15f)
        fuselagePath.lineTo(x - 20f, y + 45f)
        fuselagePath.lineTo(x + 20f, y + 45f)
        fuselagePath.lineTo(x + 25f, y + 15f)
        fuselagePath.close()
        canvas.drawPath(fuselagePath, bodyPaint)

        canvas.drawCircle(x, y - 25f, 10f, accentPaint)
        val eyePaint = Paint().apply { color = Color.RED }
        canvas.drawCircle(x, y - 25f, 4f, eyePaint)

        val leftWing = Path()
        leftWing.moveTo(x - 20f, y - 5f)
        leftWing.lineTo(x - 65f, y + 5f)
        leftWing.lineTo(x - 50f, y + 20f)
        leftWing.lineTo(x - 15f, y + 10f)
        leftWing.close()
        canvas.drawPath(leftWing, bodyPaint)

        val rightWing = Path()
        rightWing.moveTo(x + 20f, y - 5f)
        rightWing.lineTo(x + 65f, y + 5f)
        rightWing.lineTo(x + 50f, y + 20f)
        rightWing.lineTo(x + 15f, y + 10f)
        rightWing.close()
        canvas.drawPath(rightWing, bodyPaint)

        canvas.drawRect(
            RectF(x - 12f, y + 40f, x - 6f, y + 55f),
            bodyPaint
        )
        canvas.drawRect(
            RectF(x + 6f, y + 40f, x + 12f, y + 55f),
            bodyPaint
        )

        val flameLength = 25f + Math.sin(Math.toRadians(wingFlap.toDouble())).toFloat() * 10
        
        canvas.drawCircle(x - 15f, y + 45f, 8f, accentPaint)
        val leftFlamePath = Path()
        leftFlamePath.moveTo(x - 15f, y + 45f)
        leftFlamePath.lineTo(x - 20f, y + 45f + flameLength)
        leftFlamePath.lineTo(x - 10f, y + 45f + flameLength)
        leftFlamePath.close()
        canvas.drawPath(leftFlamePath, accentPaint)
        val leftFlareOuter = Paint().apply { 
            color = Color.rgb(255, 200, 0)
            alpha = 150
        }
        canvas.drawCircle(x - 15f, y + 50f + flameLength / 2, 6f, leftFlareOuter)

        canvas.drawCircle(x + 15f, y + 45f, 8f, accentPaint)
        val rightFlamePath = Path()
        rightFlamePath.moveTo(x + 15f, y + 45f)
        rightFlamePath.lineTo(x + 20f, y + 45f + flameLength)
        rightFlamePath.lineTo(x + 10f, y + 45f + flameLength)
        rightFlamePath.close()
        canvas.drawPath(rightFlamePath, accentPaint)
        canvas.drawCircle(x + 15f, y + 50f + flameLength / 2, 6f, leftFlareOuter)
    }

    private fun drawStormJet(canvas: Canvas) {
        val mainPaint = Paint().apply {
            color = Color.rgb(100, 150, 255)
            style = Paint.Style.FILL
        }
        
        val boltPaint = Paint().apply {
            color = Color.rgb(255, 255, 100)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val fuselagePath = Path()
        fuselagePath.moveTo(x, y - 42f)
        fuselagePath.quadTo(x - 15f, y - 20f, x - 20f, y + 10f)
        fuselagePath.lineTo(x - 18f, y + 42f)
        fuselagePath.lineTo(x + 18f, y + 42f)
        fuselagePath.quadTo(x + 15f, y - 20f, x, y - 42f)
        fuselagePath.close()
        canvas.drawPath(fuselagePath, mainPaint)

        canvas.drawCircle(x, y - 25f, 9f, mainPaint)
        val techPaint = Paint().apply {
            color = Color.rgb(0, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawCircle(x, y - 25f, 9f, techPaint)
        canvas.drawCircle(x, y - 25f, 5f, techPaint)

        val leftWing = Path()
        leftWing.moveTo(x - 18f, y - 8f)
        leftWing.quadTo(x - 50f, y + 5f, x - 55f, y + 25f)
        leftWing.lineTo(x - 40f, y + 25f)
        leftWing.quadTo(x - 20f, y + 15f, x - 15f, y + 5f)
        leftWing.close()
        canvas.drawPath(leftWing, mainPaint)
        canvas.drawPath(leftWing, boltPaint)

        val rightWing = Path()
        rightWing.moveTo(x + 18f, y - 8f)
        rightWing.quadTo(x + 50f, y + 5f, x + 55f, y + 25f)
        rightWing.lineTo(x + 40f, y + 25f)
        rightWing.quadTo(x + 20f, y + 15f, x + 15f, y + 5f)
        rightWing.close()
        canvas.drawPath(rightWing, mainPaint)
        canvas.drawPath(rightWing, boltPaint)

        canvas.drawRect(
            RectF(x - 8f, y + 38f, x + 8f, y + 50f),
            mainPaint
        )

        val reactorPaint = Paint().apply {
            color = Color.rgb(100, 200, 255)
        }
        
        val flameLength = 18f + Math.sin(Math.toRadians(wingFlap.toDouble())).toFloat() * 8
        
        canvas.drawCircle(x - 12f, y + 40f, 6f, reactorPaint)
        val leftIonPath = Path()
        leftIonPath.moveTo(x - 12f, y + 40f)
        leftIonPath.lineTo(x - 15f, y + 40f + flameLength)
        leftIonPath.lineTo(x - 9f, y + 40f + flameLength)
        leftIonPath.close()
        canvas.drawPath(leftIonPath, reactorPaint)
        canvas.drawPath(leftIonPath, boltPaint)

        canvas.drawCircle(x + 12f, y + 40f, 6f, reactorPaint)
        val rightIonPath = Path()
        rightIonPath.moveTo(x + 12f, y + 40f)
        rightIonPath.lineTo(x + 15f, y + 40f + flameLength)
        rightIonPath.lineTo(x + 9f, y + 40f + flameLength)
        rightIonPath.close()
        canvas.drawPath(rightIonPath, reactorPaint)
        canvas.drawPath(rightIonPath, boltPaint)

        val boltColor = Paint().apply {
            color = Color.rgb(255, 255, 100)
            strokeWidth = 1.5f
        }
        repeat(2) { i ->
            val angle = (i * 180 + wingFlap) * Math.PI / 180
            val ex = x + Math.cos(angle) * 45
            val ey = y + Math.sin(angle) * 30
            canvas.drawLine(x.toFloat(), y.toFloat(), ex.toFloat(), ey.toFloat(), boltColor)
        }
    }

    fun collidesWith(enemy: StyledEnemy): Boolean {
        val distance = Math.sqrt(
            Math.pow((x - enemy.x).toDouble(), 2.0) +
            Math.pow((y - enemy.y).toDouble(), 2.0)
        )
        
        if (distance < 45) {
            isHit = true
            hitTimer = 0
        }
        
        return distance < 45
    }

    fun shoot(): StyledBullet {
        return StyledBullet(x, y - height / 2)
    }
}
