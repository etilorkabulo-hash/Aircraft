package com.aircraftwar.pro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class GameMap(private val width: Int, private val height: Int) {

    val enemies = mutableListOf<StyledEnemy>()
    val bullets = mutableListOf<StyledBullet>()
    private var enemySpawnTimer = 0
    private var mapTimer = 0
    
    var currentMapLevel = 0
    var mapDifficulty = 1.0f
    var backgroundColor = Color.BLACK
    var gridColor = Color.argb(50, 100, 100, 100)
    var hasParticles = false
    
    private val particles = mutableListOf<Particle>()

    fun update() {
        mapTimer++
        
        if (mapTimer > 1200) {
            mapTimer = 0
            currentMapLevel = (currentMapLevel + 1) % 4
            mapDifficulty += 0.2f
            generateMapTheme()
        }

        enemies.forEach { it.update(height) }
        enemies.removeAll { it.y > height }

        bullets.forEach { it.update() }
        bullets.removeAll { it.y < 0 }

        particles.forEach { it.update() }
        particles.removeAll { it.isDead() }

        enemySpawnTimer++
        val spawnRate = when (currentMapLevel) {
            0 -> 3
            1 -> 2
            2 -> 1
            else -> 1
        }
        
        if (Random.nextInt(100) < spawnRate) {
            addEnemy(StyledEnemy(Random.nextFloat() * width, currentMapLevel))
        }
    }

    fun generateMapTheme() {
        when (currentMapLevel) {
            0 -> {
                backgroundColor = Color.rgb(10, 10, 30)
                gridColor = Color.argb(30, 100, 150, 255)
                hasParticles = true
                generateStars(50)
            }
            1 -> {
                backgroundColor = Color.rgb(20, 10, 40)
                gridColor = Color.argb(80, 255, 50, 200)
                hasParticles = true
                generateNeonLines()
            }
            2 -> {
                backgroundColor = Color.rgb(40, 15, 10)
                gridColor = Color.argb(60, 255, 100, 0)
                hasParticles = true
                generateFireParticles(30)
            }
            3 -> {
                backgroundColor = Color.rgb(30, 30, 40)
                gridColor = Color.argb(100, 150, 150, 200)
                hasParticles = true
                generateStormClouds()
            }
        }
    }

    private fun generateStars(count: Int) {
        particles.clear()
        repeat(count) {
            particles.add(Particle(
                Random.nextFloat() * width,
                Random.nextFloat() * height,
                Color.WHITE,
                2f
            ))
        }
    }

    private fun generateNeonLines() {
        particles.clear()
        repeat(15) {
            particles.add(Particle(
                Random.nextFloat() * width,
                Random.nextFloat() * height,
                Color.rgb(255, 50, 200),
                3f
            ))
        }
    }

    private fun generateFireParticles(count: Int) {
        particles.clear()
        repeat(count) {
            particles.add(Particle(
                Random.nextFloat() * width,
                Random.nextFloat() * height,
                Color.rgb(255, 100 + Random.nextInt(155), 0),
                2.5f
            ))
        }
    }

    private fun generateStormClouds() {
        particles.clear()
        repeat(20) {
            particles.add(Particle(
                Random.nextFloat() * width,
                Random.nextFloat() * height,
                Color.argb(200, 180, 180, 200),
                4f
            ))
        }
    }

    fun drawBackground(canvas: Canvas) {
        canvas.drawColor(backgroundColor)

        val paint = Paint().apply {
            color = gridColor
            strokeWidth = 1f
        }

        when (currentMapLevel) {
            0 -> drawStarGrid(canvas, paint)
            1 -> drawNeonGrid(canvas, paint)
            2 -> drawFireGrid(canvas, paint)
            3 -> drawStormGrid(canvas, paint)
        }

        if (hasParticles) {
            particles.forEach { it.draw(canvas) }
        }
    }

    private fun drawStarGrid(canvas: Canvas, paint: Paint) {
        val spacing = 80
        for (x in 0..width step spacing) {
            for (y in 0..height step spacing) {
                canvas.drawCircle(x.toFloat(), y.toFloat(), 1.5f, paint)
            }
        }
    }

    private fun drawNeonGrid(canvas: Canvas, paint: Paint) {
        val spacing = 100
        for (i in -height..width step spacing) {
            canvas.drawLine(
                (i).toFloat(), 0f,
                (i + height).toFloat(), height.toFloat(),
                paint
            )
        }
    }

    private fun drawFireGrid(canvas: Canvas, paint: Paint) {
        val spacing = 120
        for (y in 0..height step spacing) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        }
    }

    private fun drawStormGrid(canvas: Canvas, paint: Paint) {
        val spacing = 60
        for (y in 0..height step spacing) {
            var x = 0f
            while (x < width) {
                canvas.drawLine(x, y.toFloat(), x + 30, (y + 20).toFloat(), paint)
                x += 30f
            }
        }
    }

    fun addEnemy(enemy: StyledEnemy) {
        enemies.add(enemy)
    }

    fun addBullet(bullet: StyledBullet) {
        bullets.add(bullet)
    }

    fun getMapName(): String {
        return when (currentMapLevel) {
            0 -> "🌌 SPACE"
            1 -> "⚡ NEON"
            2 -> "🔥 FIRE"
            3 -> "⛈️ STORM"
            else -> "UNKNOWN"
        }
    }
}

data class Particle(
    var x: Float,
    var y: Float,
    val color: Int,
    val size: Float,
    var vx: Float = Random.nextFloat() * 2 - 1,
    var vy: Float = Random.nextFloat() * 2 - 1,
    var lifetime: Int = 300
) {
    fun update() {
        x += vx
        y += vy
        lifetime--
    }

    fun isDead(): Boolean = lifetime <= 0

    fun draw(canvas: Canvas) {
        val alpha = (lifetime * 255 / 300).coerceIn(0, 255)
        val paint = Paint().apply {
            color = android.graphics.Color.argb(alpha, 
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color)
            )
        }
        canvas.drawCircle(x, y, size, paint)
    }
}
