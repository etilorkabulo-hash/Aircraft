package com.aircraftwar.pro

import android.graphics.*
import kotlin.math.*

abstract class StyledBullet(
    var x: Float,
    var y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val damage: Int,
    val isPlayerBullet: Boolean
) {
    var isActive = true
    protected var animationTime = 0f
    protected var alpha = 255
    protected var currentSize = 1f
    
    abstract fun update(deltaTime: Float)
    abstract fun draw(canvas: Canvas)
    
    fun isOffScreen(screenWidth: Float, screenHeight: Float): Boolean {
        return x < -50 || x > screenWidth + 50 || y < -50 || y > screenHeight + 50
    }
}

// Plasma Bullet - Energy ball with trail
class PlasmaBullet(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 25, isPlayer) {
    
    private val trailPositions = mutableListOf<Pair<Float, Float>>()
    private val maxTrailLength = 10
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        
        // Update trail
        trailPositions.add(Pair(x, y))
        if (trailPositions.size > maxTrailLength) {
            trailPositions.removeAt(0)
        }
        
        x += velocityX
        y += velocityY
        
        // Pulse size
        currentSize = sin(animationTime * 10f) * 0.2f + 1f
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        
        // Draw trail
        paint.style = Paint.Style.FILL
        for (i in trailPositions.indices) {
            val alpha = (i.toFloat() / trailPositions.size * 100).toInt()
            val size = 3f + (i.toFloat() / trailPositions.size * 5f)
            
            if (isPlayerBullet) {
                paint.color = Color.argb(alpha, 0, 100 + i * 15, 255)
                paint.shader = RadialGradient(
                    trailPositions[i].first, trailPositions[i].second, size,
                    Color.argb(alpha, 0, 200, 255),
                    Color.argb(0, 0, 100, 255),
                    Shader.TileMode.CLAMP
                )
            } else {
                paint.color = Color.argb(alpha, 255, 100 + i * 10, 0)
                paint.shader = RadialGradient(
                    trailPositions[i].first, trailPositions[i].second, size,
                    Color.argb(alpha, 255, 100, 0),
                    Color.argb(0, 200, 50, 0),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(trailPositions[i].first, trailPositions[i].second, size, paint)
        }
        
        // Main bullet
        paint.shader = null
        if (isPlayerBullet) {
            paint.color = Color.rgb(0, 200, 255)
            paint.shader = RadialGradient(
                x, y, 8f * currentSize,
                Color.rgb(0, 255, 255),
                Color.rgb(0, 100, 255),
                Shader.TileMode.CLAMP
            )
        } else {
            paint.color = Color.rgb(255, 50, 0)
            paint.shader = RadialGradient(
                x, y, 8f * currentSize,
                Color.rgb(255, 150, 0),
                Color.rgb(255, 0, 0),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(x, y, 8f * currentSize, paint)
        
        // Core glow
        paint.shader = null
        paint.color = if (isPlayerBullet) Color.WHITE else Color.YELLOW
        paint.alpha = 200
        canvas.drawCircle(x, y, 3f * currentSize, paint)
    }
}

// Laser Bullet - Thin beam
class LaserBullet(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 35, isPlayer) {
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        x += velocityX
        y += velocityY
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        
        // Outer glow
        paint.strokeWidth = 6f
        paint.color = if (isPlayerBullet) 
            Color.argb(100, 255, 100, 100) 
        else 
            Color.argb(100, 255, 50, 0)
        canvas.drawLine(x, y, x - velocityX * 2, y - velocityY * 2, paint)
        
        // Core beam
        paint.strokeWidth = 2f
        paint.color = if (isPlayerBullet) Color.RED else Color.rgb(255, 100, 0)
        canvas.drawLine(x, y, x - velocityX * 3, y - velocityY * 3, paint)
        
        // Center
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(x, y, 2f, paint)
    }
}

// Missile Bullet - Rotating projectile
class MissileBullet(
    x: Float, y: Float, var targetX: Float, var targetY: Float, isPlayer: Boolean
) : StyledBullet(x, y, 0f, -8f, 50, isPlayer) {
    
    private var rotation = 0f
    private var smokeParticles = mutableListOf<Triple<Float, Float, Float>>()
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        
        // Homing behavior
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        
        if (distance > 0) {
            val speed = 8f
            x += (dx / distance) * speed * 0.5f
            y += (dy / distance) * speed * 0.5f
        }
        y += velocityY
        
        rotation = atan2(dy, dx) * 180f / PI.toFloat() + 90f
        
        // Smoke trail
        if (animationTime % 0.1f < 0.05f) {
            smokeParticles.add(Triple(x, y + 20f, 10f))
        }
        
        smokeParticles.forEach { it ->
            smokeParticles[smokeParticles.indexOf(it)] = Triple(it.first, it.second + 2f, it.third * 0.95f)
        }
        smokeParticles.removeAll { it.third < 1f }
    }
    
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)
        
        val paint = Paint()
        paint.isAntiAlias = true
        
        // Smoke trail
        paint.style = Paint.Style.FILL
        for (particle in smokeParticles) {
            paint.color = Color.argb(100, 150, 150, 150)
            paint.alpha = (particle.third * 25.5f).toInt()
            canvas.drawCircle(particle.first - x, particle.second - y, particle.third, paint)
        }
        
        // Missile body
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            -5f, -15f, 5f, 15f,
            intArrayOf(Color.RED, Color.rgb(200, 0, 0)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        val bodyPath = Path()
        bodyPath.moveTo(0f, -15f)
        bodyPath.lineTo(-5f, 10f)
        bodyPath.lineTo(5f, 10f)
        bodyPath.close()
        canvas.drawPath(bodyPath, paint)
        
        // Fins
        paint.shader = null
        paint.color = Color.rgb(150, 0, 0)
        canvas.drawRect(-8f, 5f, -3f, 12f, paint)
        canvas.drawRect(3f, 5f, 8f, 12f, paint)
        
        // Warhead
        paint.color = Color.YELLOW
        canvas.drawCircle(0f, -10f, 5f, paint)
        
        // Engine glow
        paint.color = Color.argb(200, 255, 100, 0)
        canvas.drawCircle(0f, 12f, 6f, paint)
        
        canvas.restore()
    }
}

// Spread Bullet - Multiple small projectiles
class SpreadBullet(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 15, isPlayer) {
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        x += velocityX
        y += velocityY
        currentSize = sin(animationTime * 8f) * 0.3f + 0.7f
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        
        // Crystal shape
        val path = Path()
        for (i in 0..5) {
            val angle = (i * 72f + animationTime * 50f) * PI / 180f
            val outerX = x + cos(angle).toFloat() * 6f * currentSize
            val outerY = y + sin(angle).toFloat() * 6f * currentSize
            val innerX = x + cos(angle + PI/5).toFloat() * 3f * currentSize
            val innerY = y + sin(angle + PI/5).toFloat() * 3f * currentSize
            
            if (i == 0) path.moveTo(outerX, outerY)
            else path.lineTo(outerX, outerY)
            
            path.lineTo(innerX, innerY)
        }
        path.close()
        
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            x, y, 8f * currentSize,
            if (isPlayerBullet) Color.rgb(0, 255, 200) else Color.rgb(255, 200, 0),
            if (isPlayerBullet) Color.rgb(0, 100, 150) else Color.rgb(200, 100, 0),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(path, paint)
        
        // Glow
        paint.shader = null
        paint.color = Color.argb(100, 255, 255, 255)
        canvas.drawCircle(x, y, 4f * currentSize, paint)
    }
}

// Particle Beam - Continuous stream
class ParticleBeam(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 20, isPlayer) {
    
    private val particles = mutableListOf<Pair<Float, Float>>()
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        x += velocityX
        y += velocityY
        
        // Particle stream
        if (particles.size < 5) {
            particles.add(Pair(x + Random.nextFloat() * 4f - 2f, y + Random.nextFloat() * 4f - 2f))
        }
        particles.forEachIndexed { index, pair ->
            particles[index] = Pair(pair.first + velocityX * 0.5f, pair.second + velocityY * 0.5f)
        }
        particles.removeAll { abs(it.first - x) > 20f || abs(it.second - y) > 20f }
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        
        // Particle stream
        for (particle in particles) {
            paint.color = if (isPlayerBullet)
                Color.argb(150, 100, 0, 255)
            else
                Color.argb(150, 255, 0, 100)
            canvas.drawCircle(particle.first, particle.second, 2f, paint)
        }
        
        // Main beam core
        paint.shader = RadialGradient(
            x, y, 6f,
            if (isPlayerBullet) Color.rgb(200, 100, 255) else Color.rgb(255, 100, 200),
            if (isPlayerBullet) Color.rgb(100, 0, 200) else Color.rgb(200, 0, 100),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(x, y, 6f, paint)
        
        // Energy rings
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.shader = null
        paint.color = Color.argb(
            (sin(animationTime * 10f) * 100 + 155).toInt(),
            255, 255, 255
        )
        canvas.drawCircle(x, y, 8f + sin(animationTime * 5f) * 3f, paint)
    }
}

// Explosive Bullet - Area damage
class ExplosiveBullet(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 40, isPlayer) {
    
    private val fragments = mutableListOf<Fragment>()
    private var hasExploded = false
    
    class Fragment(
        var x: Float, var y: Float,
        var velX: Float, var velY: Float,
        var alpha: Int, var size: Float
    )
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        
        if (!hasExploded) {
            x += velocityX
            y += velocityY
            
            // Explode after some time
            if (animationTime > 1.5f) {
                hasExploded = true
                // Create fragments
                for (i in 0..11) {
                    val angle = (i * 30f) * PI / 180f
                    fragments.add(Fragment(
                        x, y,
                        (cos(angle) * 5f).toFloat(),
                        (sin(angle) * 5f).toFloat(),
                        255, 4f
                    ))
                }
            }
        } else {
            // Update fragments
            fragments.forEach { fragment ->
                fragment.x += fragment.velX
                fragment.y += fragment.velY
                fragment.alpha -= 5
                fragment.size *= 0.95f
            }
            fragments.removeAll { it.alpha <= 0 }
        }
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        
        if (!hasExploded) {
            // Bomb appearance
            paint.style = Paint.Style.FILL
            paint.shader = RadialGradient(
                x, y, 12f,
                Color.rgb(255, 200, 0),
                Color.rgb(255, 100, 0),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(x, y, 12f, paint)
            
            // Warning flash
            paint.shader = null
            paint.color = Color.argb(
                (sin(animationTime * 20f) * 100 + 155).toInt(),
                255, 0, 0
            )
            canvas.drawCircle(x, y, 15f, paint)
        } else {
            // Draw fragments
            for (fragment in fragments) {
                paint.color = Color.argb(
                    fragment.alpha,
                    Random.nextInt(200, 255),
                    Random.nextInt(100, 200),
                    0
                )
                canvas.drawCircle(fragment.x, fragment.y, fragment.size, paint)
            }
        }
    }
}
