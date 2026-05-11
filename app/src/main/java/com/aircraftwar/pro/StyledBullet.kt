package com.aircraftwar.pro

import android.graphics.*
import kotlin.math.*
import kotlin.random.Random

abstract class StyledBullet(
    var x: Float, var y: Float,
    val velocityX: Float, val velocityY: Float,
    val damage: Int, val isPlayerBullet: Boolean
) {
    var isActive = true
    protected var animationTime = 0f
    
    abstract fun update(deltaTime: Float)
    abstract fun draw(canvas: Canvas)
    
    fun isOffScreen(screenWidth: Float, screenHeight: Float): Boolean {
        return x < -50 || x > screenWidth + 50 || y < -50 || y > screenHeight + 50
    }
}

class PlasmaBullet(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 25, isPlayer) {
    
    private val trailPositions = mutableListOf<Pair<Float, Float>>()
    private var currentSize = 1f
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        trailPositions.add(Pair(x, y))
        if (trailPositions.size > 10) trailPositions.removeAt(0)
        x += velocityX
        y += velocityY
        currentSize = sin(animationTime * 10f) * 0.2f + 1f
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        
        for (i in trailPositions.indices) {
            val alpha = (i.toFloat() / trailPositions.size * 100).toInt()
            paint.color = if (isPlayerBullet)
                Color.argb(alpha, 0, 100 + i * 15, 255)
            else
                Color.argb(alpha, 255, 100 + i * 10, 0)
            canvas.drawCircle(trailPositions[i].first, trailPositions[i].second, 3f + i * 0.5f, paint)
        }
        
        paint.shader = RadialGradient(
            x, y, 8f * currentSize,
            if (isPlayerBullet) Color.rgb(0, 255, 255) else Color.rgb(255, 150, 0),
            if (isPlayerBullet) Color.rgb(0, 100, 255) else Color.rgb(255, 0, 0),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(x, y, 8f * currentSize, paint)
        
        paint.shader = null
        paint.color = if (isPlayerBullet) Color.WHITE else Color.YELLOW
        paint.alpha = 200
        canvas.drawCircle(x, y, 3f * currentSize, paint)
    }
}

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
        
        paint.strokeWidth = 6f
        paint.color = if (isPlayerBullet) Color.argb(100, 255, 100, 100) else Color.argb(100, 255, 50, 0)
        canvas.drawLine(x, y, x - velocityX * 2, y - velocityY * 2, paint)
        
        paint.strokeWidth = 2f
        paint.color = if (isPlayerBullet) Color.RED else Color.rgb(255, 100, 0)
        canvas.drawLine(x, y, x - velocityX * 3, y - velocityY * 3, paint)
        
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(x, y, 2f, paint)
    }
}

class MissileBullet(
    x: Float, y: Float, var targetX: Float, var targetY: Float, isPlayer: Boolean
) : StyledBullet(x, y, 0f, -8f, 50, isPlayer) {
    
    private var rotation = 0f
    private val smokeParticles = mutableListOf<Triple<Float, Float, Float>>()
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        
        if (distance > 0) {
            x += (dx / distance) * 8f * 0.5f
            y += (dy / distance) * 8f * 0.5f
        }
        y += velocityY
        
        rotation = atan2(dy, dx) * 180f / PI.toFloat() + 90f
        
        if (animationTime % 0.1f < 0.05f) {
            smokeParticles.add(Triple(x, y + 20f, 10f))
        }
        smokeParticles.replaceAll { Triple(it.first, it.second + 2f, it.third * 0.95f) }
        smokeParticles.removeAll { it.third < 1f }
    }
    
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)
        
        val paint = Paint()
        paint.isAntiAlias = true
        
        for (particle in smokeParticles) {
            paint.color = Color.argb(100, 150, 150, 150)
            paint.alpha = (particle.third * 25.5f).toInt()
            canvas.drawCircle(particle.first - x, particle.second - y, particle.third, paint)
        }
        
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            -5f, -15f, 5f, 15f,
            intArrayOf(Color.RED, Color.rgb(200, 0, 0)),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        val bodyPath = Path().apply {
            moveTo(0f, -15f); lineTo(-5f, 10f); lineTo(5f, 10f); close()
        }
        canvas.drawPath(bodyPath, paint)
        
        paint.shader = null
        paint.color = Color.rgb(150, 0, 0)
        canvas.drawRect(-8f, 5f, -3f, 12f, paint)
        canvas.drawRect(3f, 5f, 8f, 12f, paint)
        
        paint.color = Color.YELLOW
        canvas.drawCircle(0f, -10f, 5f, paint)
        
        paint.color = Color.argb(200, 255, 100, 0)
        canvas.drawCircle(0f, 12f, 6f, paint)
        
        canvas.restore()
    }
}

class SpreadBullet(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 15, isPlayer) {
    
    private var currentSize = 1f
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        x += velocityX
        y += velocityY
        currentSize = sin(animationTime * 8f) * 0.3f + 0.7f
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        
        val path = Path()
        for (i in 0..5) {
            val angle = (i * 72f + animationTime * 50f) * PI / 180f
            val outerX = x + cos(angle).toFloat() * 6f * currentSize
            val outerY = y + sin(angle).toFloat() * 6f * currentSize
            val innerX = x + cos(angle + PI/5).toFloat() * 3f * currentSize
            val innerY = y + sin(angle + PI/5).toFloat() * 3f * currentSize
            
            if (i == 0) path.moveTo(outerX, outerY) else path.lineTo(outerX, outerY)
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
        
        paint.shader = null
        paint.color = Color.argb(100, 255, 255, 255)
        canvas.drawCircle(x, y, 4f * currentSize, paint)
    }
}

class ParticleBeam(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 20, isPlayer) {
    
    private val particles = mutableListOf<Pair<Float, Float>>()
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        x += velocityX
        y += velocityY
        
        if (particles.size < 5) {
            particles.add(Pair(x + Random.nextFloat() * 4f - 2f, y + Random.nextFloat() * 4f - 2f))
        }
        particles.replaceAll { Pair(it.first + velocityX * 0.5f, it.second + velocityY * 0.5f) }
        particles.removeAll { abs(it.first - x) > 20f || abs(it.second - y) > 20f }
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        
        for (particle in particles) {
            paint.color = if (isPlayerBullet) Color.argb(150, 100, 0, 255) else Color.argb(150, 255, 0, 100)
            canvas.drawCircle(particle.first, particle.second, 2f, paint)
        }
        
        paint.shader = RadialGradient(
            x, y, 6f,
            if (isPlayerBullet) Color.rgb(200, 100, 255) else Color.rgb(255, 100, 200),
            if (isPlayerBullet) Color.rgb(100, 0, 200) else Color.rgb(200, 0, 100),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(x, y, 6f, paint)
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.shader = null
        paint.color = Color.argb((sin(animationTime * 10f) * 100 + 155).toInt(), 255, 255, 255)
        canvas.drawCircle(x, y, 8f + sin(animationTime * 5f) * 3f, paint)
    }
}

class ExplosiveBullet(
    x: Float, y: Float, velX: Float, velY: Float, isPlayer: Boolean
) : StyledBullet(x, y, velX, velY, 40, isPlayer) {
    
    private val fragments = mutableListOf<Fragment>()
    private var hasExploded = false
    
    class Fragment(var x: Float, var y: Float, var velX: Float, var velY: Float, var alpha: Int, var size: Float)
    
    override fun update(deltaTime: Float) {
        animationTime += deltaTime
        
        if (!hasExploded) {
            x += velocityX
            y += velocityY
            if (animationTime > 1.5f) {
                hasExploded = true
                for (i in 0..11) {
                    val angle = (i * 30f) * PI / 180f
                    fragments.add(Fragment(x, y, (cos(angle) * 5f).toFloat(), (sin(angle) * 5f).toFloat(), 255, 4f))
                }
            }
        } else {
            fragments.forEach { it.x += it.velX; it.y += it.velY; it.alpha -= 5; it.size *= 0.95f }
            fragments.removeAll { it.alpha <= 0 }
        }
    }
    
    override fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        
        if (!hasExploded) {
            paint.shader = RadialGradient(
                x, y, 12f, Color.rgb(255, 200, 0), Color.rgb(255, 100, 0), Shader.TileMode.CLAMP
            )
            canvas.drawCircle(x, y, 12f, paint)
            
            paint.shader = null
            paint.color = Color.argb((sin(animationTime * 20f) * 100 + 155).toInt(), 255, 0, 0)
            canvas.drawCircle(x, y, 15f, paint)
        } else {
            for (fragment in fragments) {
                paint.color = Color.argb(fragment.alpha, Random.nextInt(200, 255), Random.nextInt(100, 200), 0)
                canvas.drawCircle(fragment.x, fragment.y, fragment.size, paint)
            }
        }
    }
}
