package com.aircraftwar.pro

import android.graphics.*
import kotlin.math.*
import kotlin.random.Random

abstract class StyledEnemy(
    var x: Float, var y: Float,
    open var width: Float = 80f, open var height: Float = 80f,
    var health: Int, val points: Int, var speed: Float
) {
    var isAlive = true
    protected var animationTime = 0f
    protected var flashAlpha = 0f
    
    protected var movePattern = MovePattern.STRAIGHT
    protected var attackPattern = AttackPattern.NORMAL
    
    enum class MovePattern { STRAIGHT, ZIGZAG, CIRCULAR, SINE_WAVE, CHASE, TELEPORT }
    enum class AttackPattern { NORMAL, BURST, SPREAD, AIMED, SURROUND }
    
    abstract fun update(deltaTime: Float)
    abstract fun draw(canvas: Canvas)
    open fun shoot(): List<StyledBullet> = emptyList()
    
    fun takeDamage(damage: Int) {
        health -= damage
        flashAlpha = 1f
        if (health <= 0) { health = 0; isAlive = false }
    }
}

class ScoutEnemy(x: Float, y: Float, level: Int) : StyledEnemy(
    x, y, 60f, 60f, 40 + level * 10, 100, 4f + level * 0.5f
) {
    private val bodyPaint = Paint()
    private val wingPaint = Paint()
    private var wingAngle = 0f
    
    init {
        movePattern = MovePattern.ZIGZAG
        bodyPaint.isAntiAlias = true
        wingPaint.isAntiAlias = true
    }
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        wingAngle = sin(animationTime * 10f) * 15f
        x += sin(animationTime * 3f) * speed * 2f
        y += speed
        if (flashAlpha > 0) flashAlpha -= deltaTime * 3f
    }
    
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x + width/2, y + height/2)
        
        bodyPaint.color = if (flashAlpha > 0) Color.WHITE else Color.rgb(255, 100, 50)
        
        canvas.save()
        canvas.rotate(wingAngle)
        wingPaint.color = Color.rgb(200, 50, 30)
        val leftWing = Path().apply { moveTo(-10f, -20f); lineTo(-35f, 10f); lineTo(-10f, 20f); close() }
        val rightWing = Path().apply { moveTo(10f, -20f); lineTo(35f, 10f); lineTo(10f, 20f); close() }
        canvas.drawPath(leftWing, wingPaint)
        canvas.drawPath(rightWing, wingPaint)
        canvas.restore()
        
        val bodyPath = Path().apply {
            moveTo(0f, -25f); lineTo(20f, 5f); lineTo(15f, 25f)
            lineTo(-15f, 25f); lineTo(-20f, 5f); close()
        }
        canvas.drawPath(bodyPath, bodyPaint)
        
        bodyPaint.color = Color.YELLOW
        canvas.drawCircle(0f, 0f, 8f, bodyPaint)
        
        canvas.restore()
    }
    
    override fun shoot(): List<StyledBullet> {
        return if (Random.nextFloat() < 0.02f) listOf(
            PlasmaBullet(x + width/2, y + height, 0f, 10f, false)
        ) else emptyList()
    }
}

class FighterEnemy(x: Float, y: Float, level: Int) : StyledEnemy(
    x, y, 90f, 85f, 60 + level * 15, 200, 3f + level * 0.3f
) {
    private val bodyPaint = Paint()
    private var shootTimer = 0f
    
    init {
        movePattern = MovePattern.SINE_WAVE
        bodyPaint.isAntiAlias = true
    }
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        shootTimer += deltaTime
        x += sin(animationTime * 2f) * speed * 1.5f
        y += speed
        if (flashAlpha > 0) flashAlpha -= deltaTime * 3f
    }
    
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x + width/2, y + height/2)
        
        bodyPaint.color = if (flashAlpha > 0) Color.WHITE else Color.rgb(200, 0, 100)
        bodyPaint.style = Paint.Style.FILL
        
        val bodyPath = Path().apply {
            moveTo(0f, -35f); lineTo(20f, -10f); lineTo(30f, 20f)
            lineTo(15f, 35f); lineTo(-15f, 35f); lineTo(-30f, 20f)
            lineTo(-20f, -10f); close()
        }
        canvas.drawPath(bodyPath, bodyPaint)
        
        bodyPaint.color = Color.YELLOW
        canvas.drawCircle(0f, -5f, 10f, bodyPaint)
        
        bodyPaint.color = Color.RED
        canvas.drawCircle(-20f, 30f, 5f, bodyPaint)
        canvas.drawCircle(20f, 30f, 5f, bodyPaint)
        
        canvas.restore()
    }
    
    override fun shoot(): List<StyledBullet> {
        if (shootTimer >= 2f) {
            shootTimer = 0f
            return listOf(
                PlasmaBullet(x + width/2 - 15f, y + height, -1f, 8f, false),
                PlasmaBullet(x + width/2 + 15f, y + height, 1f, 8f, false)
            )
        }
        return emptyList()
    }
}

class HeavyEnemy(x: Float, y: Float, level: Int) : StyledEnemy(
    x, y, 110f, 100f, 100 + level * 20, 300, 2f + level * 0.2f
) {
    private val armorPaint = Paint()
    private var turretAngle = 0f
    
    init { armorPaint.isAntiAlias = true }
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        y += speed
        turretAngle = sin(animationTime) * 30f
        if (flashAlpha > 0) flashAlpha -= deltaTime * 3f
    }
    
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x + width/2, y + height/2)
        
        armorPaint.color = if (flashAlpha > 0) Color.WHITE else Color.rgb(100, 100, 100)
        armorPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(-45f, -40f, 45f, 40f, 10f, 10f, armorPaint)
        
        canvas.save()
        canvas.rotate(turretAngle)
        armorPaint.color = Color.rgb(150, 150, 150)
        canvas.drawRect(-15f, -30f, 15f, 0f, armorPaint)
        armorPaint.color = Color.rgb(80, 80, 80)
        canvas.drawRect(-8f, -50f, 8f, -10f, armorPaint)
        canvas.restore()
        
        armorPaint.color = Color.rgb(0, 100, 255)
        canvas.drawCircle(-35f, -20f, 10f, armorPaint)
        canvas.drawCircle(35f, -20f, 10f, armorPaint)
        
        canvas.restore()
    }
    
    override fun shoot(): List<StyledBullet> {
        return listOf(ExplosiveBullet(x + width/2, y + height, 0f, 5f, false))
    }
}

class BossEnemy(x: Float, y: Float, level: Int) : StyledEnemy(
    x, y, 150f, 150f, 500 + level * 50, 1000, 1f
) {
    private val bossPaint = Paint()
    private var phaseAngle = 0f
    private var shootTimer = 0f
    
    init { bossPaint.isAntiAlias = true }
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        shootTimer += deltaTime
        phaseAngle += deltaTime
        
        if (y < 50f) y += speed
        x = 540f - width/2 + sin(animationTime * 0.5f) * 100f
        
        if (flashAlpha > 0) flashAlpha -= deltaTime * 3f
    }
    
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x + width/2, y + height/2)
        
        bossPaint.style = Paint.Style.FILL
        bossPaint.shader = RadialGradient(
            0f, 0f, 75f,
            if (flashAlpha > 0) Color.WHITE else Color.rgb(200, 0, 0),
            Color.rgb(100, 0, 0), Shader.TileMode.CLAMP
        )
        canvas.drawCircle(0f, 0f, 75f, bossPaint)
        bossPaint.shader = null
        
        bossPaint.color = Color.rgb(150, 150, 150)
        for (i in 0..5) {
            val angle = (i * 60f + animationTime * 30f) * PI / 180f
            canvas.drawCircle(cos(angle).toFloat() * 60f, sin(angle).toFloat() * 60f, 15f, bossPaint)
        }
        
        bossPaint.shader = RadialGradient(0f, 0f, 30f, Color.YELLOW, Color.RED, Shader.TileMode.CLAMP)
        canvas.drawCircle(0f, 0f, 30f, bossPaint)
        bossPaint.shader = null
        
        val healthPercent = health / 550f
        bossPaint.color = Color.RED
        canvas.drawRect(-60f, -85f, 60f, -75f, bossPaint)
        bossPaint.color = Color.GREEN
        canvas.drawRect(-60f, -85f, -60f + 120f * healthPercent, -75f, bossPaint)
        
        canvas.restore()
    }
    
    override fun shoot(): List<StyledBullet> {
        if (shootTimer >= 1.5f) {
            shootTimer = 0f
            return (0..7).map { i ->
                val angle = (i * 45f + phaseAngle * 50f) * PI / 180f
                PlasmaBullet(
                    x + width/2 + cos(angle).toFloat() * 40f,
                    y + height/2 + sin(angle).toFloat() * 40f,
                    (cos(angle) * 4f).toFloat(), (sin(angle) * 4f).toFloat(), false
                )
            }
        }
        return emptyList()
    }
}
