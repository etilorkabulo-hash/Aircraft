package com.aircraftwar.pro

import android.graphics.*
import kotlin.math.*
import kotlin.random.Random

class StyledPlayer(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float
) {
    var health = 100
    var maxHealth = 100
    var shield = 0
    var maxShield = 50
    var score = 0
    var isInvincible = false
    
    private var engineGlow = 0f
    private var wingFlap = 0f
    private var thrusterIntensity = 0f
    private var shieldPulse = 0f
    private var damageFlash = 0f
    private var hoverOffset = 0f
    
    var primaryWeapon = WeaponType.PLASMA_CANNON
    private var weaponCooldown = 0f
    
    var activePowerUps = mutableMapOf<PowerUpEffect, Long>()
    
    private val shipPath = Path()
    private val wingLeftPath = Path()
    private val wingRightPath = Path()
    
    private val hullPaint = Paint()
    private val wingPaint = Paint()
    private val enginePaint = Paint()
    private val shieldPaint = Paint()
    private val cockpitPaint = Paint()
    private val glowPaint = Paint()
    
    private val engineParticles = mutableListOf<EngineParticle>()
    private val shieldParticles = mutableListOf<ShieldParticle>()
    
    private var currentSkin = ShipSkin.NEON_STRIKER
    private var skinColor1 = Color.rgb(0, 255, 255)
    private var skinColor2 = Color.rgb(0, 150, 255)
    private var skinColor3 = Color.rgb(255, 255, 255)
    
    enum class WeaponType {
        PLASMA_CANNON, LASER_BEAM, MISSILE_LAUNCHER, SPREAD_SHOT, RAILGUN, PARTICLE_BEAM
    }
    
    enum class PowerUpEffect {
        SHIELD_BOOST, WEAPON_OVERCHARGE, SPEED_BOOST, INVINCIBILITY, TRIPLE_SHOT, MAGNETIC_FIELD
    }
    
    enum class ShipSkin {
        NEON_STRIKER, STEALTH_PHANTOM, FLAME_PHOENIX, ICE_DRAGON, GOLDEN_EAGLE, VOID_WALKER
    }
    
    data class EngineParticle(
        var x: Float, var y: Float, var size: Float,
        var alpha: Int, var speedX: Float, var speedY: Float
    )
    
    data class ShieldParticle(
        var x: Float, var y: Float, var angle: Float,
        var radius: Float, var alpha: Int
    )
    
    init {
        setupPaints()
        generateShipPaths()
    }
    
    private fun setupPaints() {
        hullPaint.style = Paint.Style.FILL
        hullPaint.isAntiAlias = true
        
        wingPaint.style = Paint.Style.FILL
        wingPaint.isAntiAlias = true
        
        enginePaint.style = Paint.Style.FILL
        enginePaint.isAntiAlias = true
        
        shieldPaint.style = Paint.Style.STROKE
        shieldPaint.strokeWidth = 3f
        shieldPaint.isAntiAlias = true
        
        cockpitPaint.style = Paint.Style.FILL
        cockpitPaint.isAntiAlias = true
        cockpitPaint.shader = RadialGradient(
            0f, 0f, 25f,
            Color.argb(200, 100, 255, 255),
            Color.argb(100, 0, 100, 255),
            Shader.TileMode.CLAMP
        )
        
        glowPaint.style = Paint.Style.FILL
        glowPaint.isAntiAlias = true
        glowPaint.maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
    }
    
    private fun generateShipPaths() {
        shipPath.reset()
        shipPath.moveTo(0f, -60f)
        shipPath.lineTo(30f, -20f)
        shipPath.lineTo(50f, 20f)
        shipPath.lineTo(30f, 60f)
        shipPath.lineTo(-30f, 60f)
        shipPath.lineTo(-50f, 20f)
        shipPath.lineTo(-30f, -20f)
        shipPath.close()
        
        wingLeftPath.reset()
        wingLeftPath.moveTo(-40f, 10f)
        wingLeftPath.lineTo(-80f, 5f)
        wingLeftPath.lineTo(-85f, 30f)
        wingLeftPath.lineTo(-70f, 45f)
        wingLeftPath.lineTo(-40f, 40f)
        wingLeftPath.close()
        
        wingRightPath.reset()
        wingRightPath.moveTo(40f, 10f)
        wingRightPath.lineTo(80f, 5f)
        wingRightPath.lineTo(85f, 30f)
        wingRightPath.lineTo(70f, 45f)
        wingRightPath.lineTo(40f, 40f)
        wingRightPath.close()
    }
    
    fun update(deltaTime: Float) {
        updateAnimations(deltaTime)
        updateParticles(deltaTime)
        updatePowerUps()
        updateSkinColors()
    }
    
    private fun updateAnimations(deltaTime: Float) {
        engineGlow = sin(System.currentTimeMillis() * 0.005f) * 0.3f + 0.7f
        wingFlap = sin(System.currentTimeMillis() * 0.003f) * 2f
        thrusterIntensity = (thrusterIntensity * 0.9f + engineGlow * 0.1f).coerceIn(0.5f, 1.5f)
        shieldPulse = sin(System.currentTimeMillis() * 0.004f) * 0.2f + 0.8f
        
        if (damageFlash > 0) damageFlash -= deltaTime * 2f
        hoverOffset = sin(System.currentTimeMillis() * 0.002f) * 3f
    }
    
    private fun updateParticles(deltaTime: Float) {
        if (engineParticles.size < 20) {
            engineParticles.add(EngineParticle(
                x = x + 60f,
                y = y + 140f + Random.nextFloat() * 10f,
                size = Random.nextFloat() * 8f + 4f,
                alpha = 255,
                speedX = Random.nextFloat() * 4f - 2f,
                speedY = Random.nextFloat() * 5f + 3f
            ))
        }
        
        engineParticles.forEach {
            it.y += it.speedY
            it.x += it.speedX
            it.alpha -= 5
            it.size *= 0.98f
        }
        engineParticles.removeAll { it.alpha <= 0 || it.y > y + 300 }
        
        if (shield > 0 && shieldParticles.size < 10) {
            shieldParticles.add(ShieldParticle(
                x + 60f, y + 70f,
                Random.nextFloat() * 360f,
                Random.nextFloat() * 30f + 60f,
                200
            ))
        }
        
        shieldParticles.forEach {
            it.angle += 2f
            it.alpha -= 3
        }
        shieldParticles.removeAll { it.alpha <= 0 }
    }
    
    private fun updatePowerUps() {
        val currentTime = System.currentTimeMillis()
        activePowerUps.entries.removeAll { (_, endTime) -> currentTime > endTime }
    }
    
    private fun updateSkinColors() {
        val time = System.currentTimeMillis() * 0.001f
        when (currentSkin) {
            ShipSkin.NEON_STRIKER -> {
                skinColor1 = Color.rgb(0, (sin(time * 2f) * 50 + 200).toInt(), (sin(time * 3f) * 50 + 200).toInt())
                skinColor2 = Color.rgb(0, 100, 200)
                skinColor3 = Color.rgb(200, 255, 255)
            }
            ShipSkin.FLAME_PHOENIX -> {
                skinColor1 = Color.rgb(255, (sin(time * 4f) * 50 + 100).toInt(), 0)
                skinColor2 = Color.rgb(200, 50, 0)
                skinColor3 = Color.rgb(255, 200, 50)
            }
            ShipSkin.ICE_DRAGON -> {
                skinColor1 = Color.rgb((sin(time * 2f) * 30 + 100).toInt(), (sin(time * 3f) * 30 + 200).toInt(), 255)
                skinColor2 = Color.rgb(50, 100, 200)
                skinColor3 = Color.rgb(200, 230, 255)
            }
            ShipSkin.GOLDEN_EAGLE -> {
                skinColor1 = Color.rgb(255, 215, 0)
                skinColor2 = Color.rgb(218, 165, 32)
                skinColor3 = Color.rgb(255, 255, 200)
            }
            ShipSkin.STEALTH_PHANTOM -> {
                skinColor1 = Color.rgb(50, 50, 60)
                skinColor2 = Color.rgb(30, 30, 40)
                skinColor3 = Color.rgb(100, 100, 150)
            }
            ShipSkin.VOID_WALKER -> {
                skinColor1 = Color.rgb((sin(time) * 50 + 50).toInt(), 0, (sin(time * 2f) * 50 + 100).toInt())
                skinColor2 = Color.rgb(20, 0, 40)
                skinColor3 = Color.rgb(150, 0, 200)
            }
        }
    }
    
    fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y + hoverOffset)
        
        if (damageFlash > 0) {
            hullPaint.color = Color.argb((damageFlash * 255).toInt(), 255, 100, 100)
            canvas.drawRect(-80f, -80f, 80f, 80f, hullPaint)
        }
        
        drawEngineGlow(canvas)
        drawWings(canvas)
        drawHull(canvas)
        drawCockpit(canvas)
        
        if (shield > 0) drawShield(canvas)
        if (isInvincible) drawInvincibilityEffect(canvas)
        
        canvas.restore()
        drawEngineParticles(canvas)
    }
    
    private fun drawEngineGlow(canvas: Canvas) {
        val thrusterShader = RadialGradient(
            0f, 70f, 30f * thrusterIntensity,
            intArrayOf(Color.argb(200, 0, 150, 255), Color.argb(150, 0, 100, 255), Color.argb(0, 0, 50, 255)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
        )
        glowPaint.shader = thrusterShader
        canvas.drawCircle(0f, 70f, 25f * thrusterIntensity, glowPaint)
        canvas.drawCircle(-20f, 65f, 12f * thrusterIntensity, glowPaint)
        canvas.drawCircle(20f, 65f, 12f * thrusterIntensity, glowPaint)
    }
    
    private fun drawWings(canvas: Canvas) {
        canvas.save()
        canvas.translate(0f, wingFlap)
        
        wingPaint.shader = LinearGradient(-80f, 5f, -40f, 45f, skinColor2, skinColor1, Shader.TileMode.CLAMP)
        canvas.drawPath(wingLeftPath, wingPaint)
        
        wingPaint.shader = LinearGradient(80f, 5f, 40f, 45f, skinColor2, skinColor1, Shader.TileMode.CLAMP)
        canvas.drawPath(wingRightPath, wingPaint)
        
        canvas.restore()
    }
    
    private fun drawHull(canvas: Canvas) {
        hullPaint.shader = LinearGradient(-50f, -60f, 50f, 60f, skinColor1, skinColor2, Shader.TileMode.CLAMP)
        canvas.drawPath(shipPath, hullPaint)
    }
    
    private fun drawCockpit(canvas: Canvas) {
        val path = Path()
        path.moveTo(-15f, -30f)
        path.lineTo(15f, -30f)
        path.lineTo(20f, -5f)
        path.lineTo(-20f, -5f)
        path.close()
        canvas.drawPath(path, cockpitPaint)
    }
    
    private fun drawShield(canvas: Canvas) {
        shieldPaint.shader = RadialGradient(
            0f, 0f, 70f,
            intArrayOf(
                Color.argb((80 + shieldPulse * 100).toInt(), 0, 200, 255),
                Color.argb(40, 0, 150, 255),
                Color.argb(0, 0, 100, 255)
            ),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(0f, 0f, 70f, shieldPaint)
        
        for (particle in shieldParticles) {
            // CORRECTION ICI - Conversion Double vers Float
            val px = cos(particle.angle.toDouble() * PI / 180.0).toFloat() * particle.radius
            val py = sin(particle.angle.toDouble() * PI / 180.0).toFloat() * particle.radius
            
            shieldPaint.shader = null
            shieldPaint.color = Color.argb(particle.alpha, 0, 200, 255)
            shieldPaint.style = Paint.Style.FILL
            canvas.drawCircle(px, py, 3f, shieldPaint)
        }
    }
    
    private fun drawInvincibilityEffect(canvas: Canvas) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.argb((sin(System.currentTimeMillis() * 0.01f) * 100 + 155).toInt(), 255, 255, 255)
        canvas.drawCircle(0f, 0f, 75f, paint)
    }
    
    private fun drawEngineParticles(canvas: Canvas) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        for (particle in engineParticles) {
            paint.color = Color.argb(
                particle.alpha,
                (sin(particle.y * 0.1f).toInt()).coerceIn(0, 100) + 155,
                (sin(particle.x * 0.1f).toInt()).coerceIn(0, 100) + 155,
                255
            )
            canvas.drawCircle(particle.x, particle.y, particle.size, paint)
        }
    }
    
    fun takeDamage(damage: Int) {
        if (!isInvincible) {
            damageFlash = 1f
            if (shield > 0) {
                val absorbed = minOf(damage, shield)
                shield -= absorbed
                if (damage > absorbed) health -= (damage - absorbed)
            } else {
                health -= damage
            }
            health = health.coerceAtLeast(0)
        }
    }
    
    fun heal(amount: Int) { health = (health + amount).coerceAtMost(maxHealth) }
    fun addShield(amount: Int) { shield = (shield + amount).coerceAtMost(maxShield) }
    fun reset() { health = maxHealth; shield = 0; activePowerUps.clear() }
    fun getCenterX(): Float = x + width / 2
    fun getCenterY(): Float = y + height / 2
    
    fun activatePowerUp(effect: PowerUpEffect, duration: Long = 10000) {
        activePowerUps[effect] = System.currentTimeMillis() + duration
    }
    
    fun changeSkin(skin: ShipSkin) {
        currentSkin = skin
        updateSkinColors()
    }
}
