package com.aircraftwar.pro

import android.graphics.*
import kotlin.math.*
import kotlin.random.Random

class GameMap {
    
    private var currentBiome = Biome.NEUTRAL_SPACE
    private var nextBiome = Biome.NEUTRAL_SPACE
    private var biomeTransitionProgress = 0f
    private var timeInCurrentBiome = 0f
    private var mapTime = 0f
    private var difficulty = 1
    
    private val nebulaParticles = mutableListOf<NebulaParticle>()
    private val asteroidBelt = mutableListOf<Asteroid>()
    private val lightningBolts = mutableListOf<Lightning>()
    private val rainDrops = mutableListOf<RainDrop>()
    private val lavaBubbles = mutableListOf<LavaBubble>()
    private val iceCrystals = mutableListOf<IceCrystal>()
    private val stars = mutableListOf<Star>()
    
    private var backgroundColor = Color.BLACK
    private var gridColor = Color.argb(30, 255, 255, 255)
    private var nebulaColor1 = Color.TRANSPARENT
    private var nebulaColor2 = Color.TRANSPARENT
    
    private var screenWidth = 0f
    private var screenHeight = 0f
    
    private val backgroundPaint = Paint()
    private val gridPaint = Paint()
    private val nebulaPaint = Paint()
    private val asteroidPaint = Paint()
    private val lightningPaint = Paint()
    
    private var currentEvent = MapEvent.NONE
    private var eventTimer = 0f
    private var eventDuration = 0f
    private var currentWeather = WeatherEffect.NONE
    
    enum class Biome {
        NEUTRAL_SPACE, NEBULA_ZONE, ASTEROID_FIELD, STORM_SECTOR,
        ICE_NEBULA, VOLCANIC_ZONE, QUANTUM_REALM, DARK_MATTER, SUPERNOVA_AFTERMATH
    }
    
    enum class MapEvent {
        NONE, METEOR_SHOWER, SOLAR_FLARE, GRAVITY_WELL, TIME_DILATION,
        BLACK_HOLE, WORMHOLE, POWER_SURGE
    }
    
    enum class WeatherEffect {
        NONE, COSMIC_RAIN, SOLAR_WIND, SPACE_FOG, AURORA_BOREALIS,
        PLASMA_STORM, CRYSTAL_SHARDS, DARK_MATTER_CLOUDS
    }
    
    fun init(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
        
        for (i in 0..80) {
            nebulaParticles.add(createNebulaParticle())
            stars.add(Star(
                Random.nextFloat() * screenWidth,
                Random.nextFloat() * screenHeight,
                Random.nextFloat() * 2f + 0.5f,
                Random.nextFloat() * 0.5f + 0.1f,
                Random.nextInt(100, 255)
            ))
        }
        
        updateBiomeColors()
    }
    
    fun update(deltaTime: Float, gameDifficulty: Int) {
        mapTime += deltaTime
        difficulty = gameDifficulty
        
        timeInCurrentBiome += deltaTime
        if (timeInCurrentBiome > getBiomeDuration()) {
            transitionToNextBiome()
        }
        
        if (biomeTransitionProgress < 1f) {
            biomeTransitionProgress += deltaTime * 0.5f
        }
        
        updateStars(deltaTime)
        updateEnvironment(deltaTime)
        updateWeather(deltaTime)
        updateEvents(deltaTime)
        updateHazards(deltaTime)
    }
    
    private fun updateStars(deltaTime: Float) {
        for (star in stars) {
            star.y += star.speed
            star.alpha = (sin(mapTime * 2f + star.x) * 50 + star.baseAlpha).toInt()
            if (star.y > screenHeight) {
                star.y = -5f
                star.x = Random.nextFloat() * screenWidth
            }
        }
    }
    
    private fun transitionToNextBiome() {
        val biomes = Biome.values()
        nextBiome = biomes[Random.nextInt(biomes.size)]
        while (nextBiome == currentBiome) {
            nextBiome = biomes[Random.nextInt(biomes.size)]
        }
        
        currentBiome = nextBiome
        biomeTransitionProgress = 0f
        timeInCurrentBiome = 0f
        
        clearEnvironment()
        generateBiomeObjects()
        updateBiomeColors()
        
        currentEvent = getRandomEventForBiome(currentBiome)
        eventTimer = 0f
        eventDuration = Random.nextFloat() * 10f + 5f
    }
    
    private fun getBiomeDuration(): Float = when {
        difficulty >= 10 -> 8f
        difficulty >= 5 -> 12f
        else -> 20f
    }
    
    private fun updateBiomeColors() {
        when (currentBiome) {
            Biome.NEUTRAL_SPACE -> {
                backgroundColor = Color.rgb(8, 8, 25)
                gridColor = Color.argb(20, 100, 100, 255)
                nebulaColor1 = Color.argb(15, 50, 50, 150)
                nebulaColor2 = Color.argb(15, 30, 30, 100)
            }
            Biome.NEBULA_ZONE -> {
                backgroundColor = Color.rgb(15, 3, 35)
                gridColor = Color.argb(25, 200, 100, 255)
                nebulaColor1 = Color.argb(20, 150, 50, 200)
                nebulaColor2 = Color.argb(20, 100, 30, 150)
            }
            Biome.ASTEROID_FIELD -> {
                backgroundColor = Color.rgb(25, 18, 8)
                gridColor = Color.argb(15, 200, 150, 100)
                nebulaColor1 = Color.argb(10, 100, 80, 60)
                nebulaColor2 = Color.argb(10, 80, 60, 40)
            }
            Biome.STORM_SECTOR -> {
                backgroundColor = Color.rgb(12, 12, 35)
                gridColor = Color.argb(30, 150, 200, 255)
                nebulaColor1 = Color.argb(20, 100, 150, 255)
                nebulaColor2 = Color.argb(20, 50, 100, 200)
            }
            Biome.ICE_NEBULA -> {
                backgroundColor = Color.rgb(18, 25, 45)
                gridColor = Color.argb(25, 150, 200, 255)
                nebulaColor1 = Color.argb(20, 200, 230, 255)
                nebulaColor2 = Color.argb(20, 150, 200, 255)
            }
            Biome.VOLCANIC_ZONE -> {
                backgroundColor = Color.rgb(35, 8, 3)
                gridColor = Color.argb(30, 255, 100, 50)
                nebulaColor1 = Color.argb(25, 255, 50, 0)
                nebulaColor2 = Color.argb(25, 200, 30, 0)
            }
            Biome.QUANTUM_REALM -> {
                backgroundColor = Color.rgb(3, 3, 12)
                gridColor = Color.argb(35, 0, 255, 255)
                nebulaColor1 = Color.argb(30, 0, 200, 255)
                nebulaColor2 = Color.argb(30, 100, 0, 255)
            }
            Biome.DARK_MATTER -> {
                backgroundColor = Color.rgb(3, 3, 8)
                gridColor = Color.argb(10, 50, 50, 50)
                nebulaColor1 = Color.argb(10, 30, 0, 50)
                nebulaColor2 = Color.argb(10, 20, 0, 40)
            }
            Biome.SUPERNOVA_AFTERMATH -> {
                backgroundColor = Color.rgb(45, 25, 8)
                gridColor = Color.argb(40, 255, 200, 100)
                nebulaColor1 = Color.argb(35, 255, 150, 50)
                nebulaColor2 = Color.argb(35, 200, 100, 30)
            }
        }
    }
    
    private fun generateBiomeObjects() {
        asteroidBelt.clear()
        lightningBolts.clear()
        iceCrystals.clear()
        lavaBubbles.clear()
        
        when (currentBiome) {
            Biome.ASTEROID_FIELD -> {
                for (i in 0..25) asteroidBelt.add(createAsteroid())
            }
            Biome.STORM_SECTOR -> {
                lightningBolts.add(Lightning())
            }
            Biome.ICE_NEBULA -> {
                for (i in 0..35) iceCrystals.add(createIceCrystal())
            }
            Biome.VOLCANIC_ZONE -> {
                for (i in 0..20) lavaBubbles.add(createLavaBubble())
            }
            Biome.QUANTUM_REALM -> currentWeather = WeatherEffect.AURORA_BOREALIS
            Biome.DARK_MATTER -> currentWeather = WeatherEffect.DARK_MATTER_CLOUDS
            Biome.SUPERNOVA_AFTERMATH -> currentWeather = WeatherEffect.PLASMA_STORM
            else -> currentWeather = WeatherEffect.NONE
        }
    }
    
    private fun clearEnvironment() {
        asteroidBelt.clear()
        lightningBolts.clear()
        iceCrystals.clear()
        lavaBubbles.clear()
        rainDrops.clear()
        currentWeather = WeatherEffect.NONE
    }
    
    private fun createNebulaParticle() = NebulaParticle(
        x = Random.nextFloat() * screenWidth,
        y = Random.nextFloat() * screenHeight,
        size = Random.nextFloat() * 120f + 60f,
        speedX = Random.nextFloat() * 0.3f - 0.15f,
        speedY = Random.nextFloat() * 0.3f - 0.15f,
        color = Color.argb(15, Random.nextInt(50, 150), Random.nextInt(30, 100), Random.nextInt(100, 200)),
        currentSize = 0f
    )
    
    private fun createAsteroid() = Asteroid(
        x = Random.nextFloat() * screenWidth,
        y = Random.nextFloat() * screenHeight,
        size = Random.nextFloat() * 35f + 25f,
        speed = Random.nextFloat() * 2f + 0.5f,
        rotationSpeed = Random.nextFloat() * 3f - 1.5f,
        rotation = 0f
    )
    
    private fun createIceCrystal() = IceCrystal(
        x = Random.nextFloat() * screenWidth,
        y = Random.nextFloat() * screenHeight,
        size = Random.nextFloat() * 15f + 8f,
        opacity = Random.nextInt(100, 255),
        sparklePhase = Random.nextFloat() * 360f
    )
    
    private fun createLavaBubble() = LavaBubble(
        x = Random.nextFloat() * screenWidth,
        y = screenHeight + Random.nextFloat() * 50f,
        size = Random.nextFloat() * 12f + 6f,
        speed = Random.nextFloat() * -3f - 1f,
        pulseRate = Random.nextFloat() * 2f + 1f,
        pulsePhase = 0f,
        currentSize = 0f
    )
    
    private fun updateEnvironment(deltaTime: Float) {
        for (particle in nebulaParticles) {
            particle.x += particle.speedX
            particle.y += particle.speedY
            
            if (particle.x < -particle.size) particle.x = screenWidth + particle.size
            if (particle.x > screenWidth + particle.size) particle.x = -particle.size
            if (particle.y < -particle.size) particle.y = screenHeight + particle.size
            if (particle.y > screenHeight + particle.size) particle.y = -particle.size
            
            particle.currentSize = particle.size + sin(mapTime * 0.5f) * 15f
        }
        
        if (nebulaParticles.size < 80) {
            nebulaParticles.add(createNebulaParticle())
        }
    }
    
    private fun updateWeather(deltaTime: Float) {
        if (currentWeather == WeatherEffect.COSMIC_RAIN && Random.nextInt(100) < 30) {
            rainDrops.add(RainDrop(
                x = Random.nextFloat() * screenWidth,
                y = -10f,
                speed = Random.nextFloat() * 8f + 4f,
                length = Random.nextFloat() * 25f + 15f
            ))
        }
        
        for (drop in rainDrops) {
            drop.y += drop.speed
        }
        rainDrops.removeAll { it.y > screenHeight + it.length }
        
        if (currentWeather == WeatherEffect.PLASMA_STORM && Random.nextInt(100) < 20) {
            lightningBolts.add(Lightning())
        }
    }
    
    private fun updateEvents(deltaTime: Float) {
        eventTimer += deltaTime
        if (eventTimer >= eventDuration && currentEvent != MapEvent.NONE) {
            currentEvent = MapEvent.NONE
            eventTimer = 0f
        }
    }
    
    private fun updateHazards(deltaTime: Float) {
        for (asteroid in asteroidBelt) {
            asteroid.y += asteroid.speed
            asteroid.rotation += asteroid.rotationSpeed
            if (asteroid.y > screenHeight + asteroid.size) {
                asteroid.y = -asteroid.size
                asteroid.x = Random.nextFloat() * screenWidth
            }
        }
        
        for (crystal in iceCrystals) {
            crystal.y += 1f
            crystal.sparklePhase += 0.05f
            crystal.opacity = (100 + sin(crystal.sparklePhase) * 100).toInt()
            if (crystal.y > screenHeight + crystal.size) {
                crystal.y = -crystal.size
                crystal.x = Random.nextFloat() * screenWidth
            }
        }
        
        for (bubble in lavaBubbles) {
            bubble.y += bubble.speed
            bubble.pulsePhase += bubble.pulseRate * deltaTime
            bubble.currentSize = bubble.size + sin(bubble.pulsePhase) * 4f
            if (bubble.y < -bubble.size) {
                bubble.y = screenHeight + Random.nextFloat() * 50f
                bubble.x = Random.nextFloat() * screenWidth
            }
        }
        
        for (lightning in lightningBolts) {
            lightning.duration -= deltaTime
        }
        lightningBolts.removeAll { it.duration <= 0 }
        
        if (currentBiome == Biome.STORM_SECTOR && lightningBolts.size < 2 && Random.nextInt(100) < 40) {
            lightningBolts.add(Lightning())
        }
    }
    
    private fun getRandomEventForBiome(biome: Biome): MapEvent = when (biome) {
        Biome.ASTEROID_FIELD -> if (Random.nextBoolean()) MapEvent.METEOR_SHOWER else MapEvent.NONE
        Biome.STORM_SECTOR -> if (Random.nextBoolean()) MapEvent.SOLAR_FLARE else MapEvent.NONE
        Biome.QUANTUM_REALM -> if (Random.nextBoolean()) MapEvent.TIME_DILATION else MapEvent.WORMHOLE
        Biome.DARK_MATTER -> if (Random.nextBoolean()) MapEvent.GRAVITY_WELL else MapEvent.BLACK_HOLE
        Biome.SUPERNOVA_AFTERMATH -> if (Random.nextBoolean()) MapEvent.POWER_SURGE else MapEvent.SOLAR_FLARE
        else -> MapEvent.NONE
    }
    
    fun draw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        
        // Draw nebula
        for (particle in nebulaParticles) {
            nebulaPaint.color = particle.color
            nebulaPaint.alpha = 25
            canvas.drawCircle(particle.x, particle.y, particle.currentSize, nebulaPaint)
        }
        
        // Draw grid
        drawGrid(canvas)
        
        // Draw stars
        for (star in stars) {
            gridPaint.color = Color.argb(star.alpha, 255, 255, 255)
            canvas.drawCircle(star.x, star.y, star.size, gridPaint)
        }
        
        // Draw biome elements
        when (currentBiome) {
            Biome.ASTEROID_FIELD -> drawAsteroids(canvas)
            Biome.STORM_SECTOR -> drawLightning(canvas)
            Biome.ICE_NEBULA -> drawIceCrystals(canvas)
            Biome.VOLCANIC_ZONE -> drawLavaBubbles(canvas)
            else -> {}
        }
        
        // Draw weather
        drawWeather(canvas)
        
        // Draw events
        drawEvents(canvas)
        
        // Draw transition
        if (biomeTransitionProgress < 1f) {
            val alpha = (sin(biomeTransitionProgress * PI) * 200).toInt()
            if (alpha > 0) {
                val transitionPaint = Paint()
                transitionPaint.color = Color.argb(alpha, 255, 255, 255)
                canvas.drawRect(0f, 0f, screenWidth, screenHeight, transitionPaint)
            }
        }
    }
    
    private fun drawGrid(canvas: Canvas) {
        gridPaint.color = gridColor
        gridPaint.strokeWidth = 1f
        val gridSize = 100f
        val offset = mapTime * 15f % gridSize
        
        var x = offset
        while (x < screenWidth) {
            canvas.drawLine(x, 0f, x, screenHeight, gridPaint)
            x += gridSize
        }
        
        var y = offset
        while (y < screenHeight) {
            canvas.drawLine(0f, y, screenWidth, y, gridPaint)
            y += gridSize
        }
    }
    
    private fun drawAsteroids(canvas: Canvas) {
        for (asteroid in asteroidBelt) {
            canvas.save()
            canvas.rotate(asteroid.rotation, asteroid.x, asteroid.y)
            
            asteroidPaint.color = Color.rgb(140, 110, 70)
            val path = Path()
            path.moveTo(asteroid.x - asteroid.size/2, asteroid.y - asteroid.size/2)
            path.lineTo(asteroid.x + asteroid.size/2, asteroid.y - asteroid.size/3)
            path.lineTo(asteroid.x + asteroid.size/1.5f, asteroid.y + asteroid.size/3)
            path.lineTo(asteroid.x - asteroid.size/3, asteroid.y + asteroid.size/2)
            path.close()
            canvas.drawPath(path, asteroidPaint)
            
            asteroidPaint.color = Color.rgb(90, 70, 40)
            canvas.drawCircle(asteroid.x, asteroid.y, asteroid.size/5, asteroidPaint)
            
            canvas.restore()
        }
    }
    
    private fun drawLightning(canvas: Canvas) {
        for (lightning in lightningBolts) {
            lightningPaint.color = Color.argb(180, 100, 150, 255)
            lightningPaint.strokeWidth = 3f
            
            val path = Path()
            path.moveTo(lightning.startX, lightning.startY)
            var currentX = lightning.startX
            var currentY = lightning.startY
            
            while (currentY < screenHeight) {
                currentX += Random.nextInt(-30, 30).toFloat()
                currentY += Random.nextInt(20, 60).toFloat()
                path.lineTo(currentX, currentY)
            }
            
            canvas.drawPath(path, lightningPaint)
            
            lightningPaint.color = Color.argb(80, 150, 200, 255)
            lightningPaint.strokeWidth = 8f
            canvas.drawPath(path, lightningPaint)
        }
    }
    
    private fun drawIceCrystals(canvas: Canvas) {
        val paint = Paint()
        for (crystal in iceCrystals) {
            paint.color = Color.argb(crystal.opacity, 200, 230, 255)
            for (i in 0..5) {
                val angle = (i * 60f + crystal.sparklePhase) * PI / 180f
                val innerRadius = crystal.size * 0.3f
                val outerRadius = crystal.size
                
                val x1 = crystal.x + cos(angle).toFloat() * outerRadius
                val y1 = crystal.y + sin(angle).toFloat() * outerRadius
                val x2 = crystal.x + cos(angle + PI/6).toFloat() * innerRadius
                val y2 = crystal.y + sin(angle + PI/6).toFloat() * innerRadius
                
                canvas.drawLine(x1, y1, x2, y2, paint)
            }
        }
    }
    
    private fun drawLavaBubbles(canvas: Canvas) {
        val paint = Paint()
        for (bubble in lavaBubbles) {
            paint.color = Color.rgb(255, (100 + sin(bubble.pulsePhase) * 100).toInt(), 0)
            paint.alpha = 200
            canvas.drawCircle(bubble.x, bubble.y, bubble.currentSize, paint)
            
            paint.color = Color.rgb(255, 255, 100)
            paint.alpha = 100
            canvas.drawCircle(bubble.x, bubble.y, bubble.currentSize * 0.6f, paint)
        }
    }
    
    private fun drawWeather(canvas: Canvas) {
        when (currentWeather) {
            WeatherEffect.COSMIC_RAIN -> {
                val paint = Paint()
                paint.color = Color.argb(150, 100, 150, 255)
                paint.strokeWidth = 2f
                for (drop in rainDrops) {
                    canvas.drawLine(drop.x, drop.y, drop.x, drop.y + drop.length, paint)
                }
            }
            WeatherEffect.AURORA_BOREALIS -> {
                val paint = Paint()
                for (i in 0..5) {
                    val y = screenHeight / 2 + sin(mapTime + i) * 100f
                    paint.color = Color.argb(50, 100 + i * 30, 200 - i * 20, 255)
                    paint.strokeWidth = 20f
                    
                    val path = Path()
                    path.moveTo(0f, y)
                    for (x in 0..screenWidth.toInt() step 10) {
                        val waveY = y + sin(x / 100f + mapTime * 2f + i) * 30f
                        path.lineTo(x.toFloat(), waveY)
                    }
                    canvas.drawPath(path, paint)
                }
            }
            else -> {}
        }
    }
    
    private fun drawEvents(canvas: Canvas) {
        when (currentEvent) {
            MapEvent.METEOR_SHOWER -> {
                val paint = Paint()
                paint.color = Color.rgb(255, 100, 50)
                for (i in 0..8) {
                    val x = Random.nextFloat() * screenWidth
                    val y = Random.nextFloat() * screenHeight / 2
                    canvas.drawCircle(x, y, 15f, paint)
                }
            }
            MapEvent.BLACK_HOLE -> {
                val paint = Paint()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                paint.color = Color.argb(150, 100, 0, 150)
                canvas.drawCircle(screenWidth / 2, screenHeight / 2, eventTimer * 40f, paint)
            }
            else -> {}
        }
    }
    
    fun getCurrentBiome(): Biome = currentBiome
    fun getCurrentEvent(): MapEvent = currentEvent
    fun getCurrentWeather(): WeatherEffect = currentWeather
    
    fun isInHazardZone(x: Float, y: Float, radius: Float): Boolean {
        if (currentBiome == Biome.ASTEROID_FIELD) {
            for (asteroid in asteroidBelt) {
                val dx = x - asteroid.x
                val dy = y - asteroid.y
                if (sqrt(dx * dx + dy * dy) < radius + asteroid.size / 2) return true
            }
        }
        
        if (currentBiome == Biome.VOLCANIC_ZONE) {
            for (bubble in lavaBubbles) {
                val dx = x - bubble.x
                val dy = y - bubble.y
                if (sqrt(dx * dx + dy * dy) < radius + bubble.currentSize) return true
            }
        }
        
        return false
    }
    
    fun getDamageMultiplier(): Float = when (currentBiome) {
        Biome.VOLCANIC_ZONE -> 1.5f
        Biome.SUPERNOVA_AFTERMATH -> 2.0f
        Biome.DARK_MATTER -> 0.7f
        else -> 1.0f
    }
    
    fun getSpeedMultiplier(): Float = when (currentBiome) {
        Biome.ICE_NEBULA -> 0.7f
        Biome.QUANTUM_REALM -> 1.3f
        else -> 1.0f
    }
    
    data class NebulaParticle(
        var x: Float, var y: Float, var size: Float,
        var speedX: Float, var speedY: Float, var color: Int,
        var currentSize: Float
    )
    
    data class Star(
        var x: Float, var y: Float, val size: Float,
        val speed: Float, val baseAlpha: Int, var alpha: Int = baseAlpha
    )
    
    data class Asteroid(
        var x: Float, var y: Float, val size: Float,
        val speed: Float, var rotation: Float, val rotationSpeed: Float
    )
    
    data class Lightning(
        val startX: Float = Random.nextFloat() * 2000f,
        val startY: Float = 0f,
        var duration: Float = Random.nextFloat() * 2f + 0.5f
    )
    
    data class RainDrop(
        var x: Float, var y: Float, val speed: Float, val length: Float
    )
    
    data class LavaBubble(
        var x: Float, var y: Float, val size: Float,
        val speed: Float, val pulseRate: Float,
        var pulsePhase: Float, var currentSize: Float
    )
    
    data class IceCrystal(
        var x: Float, var y: Float, val size: Float,
        var opacity: Int, var sparklePhase: Float
    )
}
