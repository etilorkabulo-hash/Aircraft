package com.aircraftwar.pro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

class StyledBullet(var x: Float, var y: Float, val mapLevel: Int = 0) {

    private val speed = 18f
    private val paint = Paint()
    private var trail = mutableListOf<Pair<Float, Float>>()
    private var lifetime = 200

    fun update() {
        y -= speed
        lifetime--
        
        trail.add(Pair(x, y))
        if (trail.size > 20) {
            trail.removeAt(0)
        }
    }

    fun draw(canvas: Canvas) {
        when (mapLevel) {
            0 -> drawSpaceMissile(canvas)
            1 -> drawNeonMissile(canvas)
            2 -> drawFireMissile(canvas)
            3 -> drawStormMissile(canvas)
        }
    }

    private fun drawSpaceMissile(canvas: Canvas) {
        val missilePaint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.FILL
        }

        val headPath = Path()
        headPath.moveTo(x, y - 12f)
        headPath.lineTo(x - 4f, y + 2f)
        headPath.lineTo(x + 4f, y + 2f)
        headPath.close()
        canvas.drawPath(headPath, missilePaint)

        canvas.drawRect(x - 3f, y + 2f, x + 3f, y + 16f, missilePaint)

        canvas.drawRect(x - 6f, y + 12f, x - 3f, y + 16f, missilePaint)
        canvas.drawRect(x + 3f, y + 12f, x + 6f, y + 16f, missilePaint)

        val auraPaint = Paint().apply {
            color = Color.argb(100, 0, 255, 255)
        }
        canvas.drawCircle(x, y + 2f, 8f, auraPaint)

        drawTrail(canvas, Color.CYAN)
    }

    private fun drawNeonMissile(canvas: Canvas) {
        val outlinePaint = Paint().apply {
            color = Color.rgb(255, 0, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        
        val fillPaint = Paint().apply {
            color = Color.rgb(150, 0, 150)
            style = Paint.Style.FILL
        }

        val projectilePath = Path()
        projectilePath.moveTo(x, y - 14f)
        projectilePath.lineTo(x - 5f, y + 5f)
        projectilePath.lineTo(x - 2f, y + 14f)
        projectilePath.lineTo(x + 2f, y + 14f)
        projectilePath.lineTo(x + 5f, y + 5f)
        projectilePath.close()
        canvas.drawPath(projectilePath, fillPaint)
        canvas.drawPath(projectilePath, outlinePaint)

        val corePaint = Paint().apply {
            color = Color.rgb(0, 255, 255)
        }
        canvas.drawCircle(x, y, 3f, corePaint)

        val aura1 = Paint().apply {
            color = Color.argb(150, 255, 0, 255)
        }
        canvas.drawCircle(x, y, 10f, aura1)
    }

    private fun drawFireMissile(canvas: Canvas) {
        val firePaint = Paint().apply {
            color = Color.rgb(255, 150, 0)
            style = Paint.Style.FILL
        }
        
        val intensePaint = Paint().apply {
            color = Color.rgb(255, 200, 0)
        }

        val headPath = Path()
        headPath.moveTo(x, y - 14f)
        headPath.lineTo(x - 5f, y + 3f)
        headPath.lineTo(x + 5f, y + 3f)
        headPath.close()
        canvas.drawPath(headPath, intensePaint)

        canvas.drawRect(x - 4f, y + 3f, x + 4f, y + 16f, firePaint)

        canvas.drawRect(x - 7f, y + 10f, x - 4f, y + 16f, firePaint)
        canvas.drawRect(x + 4f, y + 10f, x + 7f, y + 16f, firePaint)

        val flamePath = Path()
        flamePath.moveTo(x - 4f, y + 16f)
        flamePath.lineTo(x - 6f, y + 28f)
        flamePath.lineTo(x, y + 22f)
        flamePath.lineTo(x + 6f, y + 28f)
        flamePath.lineTo(x + 4f, y + 16f)
        flamePath.close()
        canvas.drawPath(flamePath, intensePaint)

        drawTrail(canvas, Color.rgb(255, 100, 0))
    }

    private fun drawStormMissile(canvas: Canvas) {
        val electricPaint = Paint().apply {
            color = Color.rgb(100, 200, 255)
            style = Paint.Style.FILL
        }
        
        val boltPaint = Paint().apply {
            color = Color.rgb(255, 255, 100)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        val projectilePath = Path()
        projectilePath.moveTo(x, y - 13f)
        projectilePath.lineTo(x + 5f, y - 5f)
        projectilePath.lineTo(x + 5f, y + 8f)
        projectilePath.lineTo(x, y + 14f)
        projectilePath.lineTo(x - 5f, y + 8f)
        projectilePath.lineTo(x - 5f, y - 5f)
        projectilePath.close()
        canvas.drawPath(projectilePath, electricPaint)
        canvas.drawPath(projectilePath, boltPaint)

        val corePaint = Paint().apply {
            color = Color.rgb(255, 255, 150)
        }
        canvas.drawCircle(x, y, 2f, corePaint)

        repeat(3) { i ->
            val angle = (i * 120) * Math.PI / 180
            val ex = x + Math.cos(angle) * 12
            val ey = y + Math.sin(angle) * 12
            canvas.drawLine(x.toFloat(), y.toFloat(), ex.toFloat(), ey.toFloat(), boltPaint)
        }
    }

    private fun drawTrail(canvas: Canvas, color: Int) {
        val trailPaint = Paint().apply { 
            this.color = color
        }
        
        for ((index, point) in trail.withIndex()) {
            val alpha = (index * 255 / trail.size.coerceAtLeast(1)).coerceIn(0, 255)
            trailPaint.alpha = alpha
            canvas.drawCircle(point.first, point.second, 2f, trailPaint)
        }
    }

    fun collidesWith(enemy: StyledEnemy): Boolean {
        val distance = Math.sqrt(
            Math.pow((x - enemy.x).toDouble(), 2.0) +
            Math.pow((y - enemy.y).toDouble(), 2.0)
        )
        return distance < 35.0
    }
}
