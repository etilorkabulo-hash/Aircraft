package com.aircraftwar.pro

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import kotlin.math.*
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    companion object {
        const val MAX_LIVES = 3
        const val INVINCIBILITY_DURATION = 3f
        const val COMBO_TIMEOUT = 2.5f
        const val DIFFICULTY_INCREASE_TIME = 25f
    }
    
    // Core systems
    private val gameMap = GameMap()
    private lateinit var player: StyledPlayer
    private val enemies = mutableListOf<StyledEnemy>()
    private val bullets = mutableListOf<StyledBullet>()
    private val powerUps = mutableListOf<PowerUpItem>()
    private val explosions = mutableListOf<ExplosionEffect>()
    private val particles = mutableListOf<BackgroundParticle>()
    private val damageNumbers = mutableListOf<DamageNumber>()
    
    // Game state
    private var gameState = GameState.LOADING
    private var score = 0L
    private var highScore = 0L
    private var level = 1
    private var lives = MAX_LIVES
    private var comboCount = 0
    private var maxCombo = 0
    private var comboMultiplier = 1f
    private var waveNumber = 0
    private var totalEnemiesKilled = 0
    private var totalShotsFired = 0
    private var accuracyRate = 0f
    private var gameTime = 0f
    
    // Timing
    private var lastUpdateTime = System.nanoTime()
    private var enemySpawnTimer = 0f
    private var powerUpSpawnTimer = 0f
    private var difficultyTimer = 0f
    private var bossSpawnCounter = 0
    private var invincibilityTimer = 0f
    
    // Screen
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var scaleFactor = 1f
    
    // Touch handling
    private var touchX = 0f
    private var touchY = 0f
    private var isTouching = false
    private var touchStartTime = 0L
    private var lastTapTime = 0L
    private var doubleTapDetected = false
    
    // Game thread
    private var gameThread: GameThread? = null
    @Volatile private var isRunning = false
    
    // Paint objects cache
    private val textPaint = Paint()
    private val hudPaint = Paint()
    private val backgroundPaint = Paint()
    private val particlePaint = Paint()
    
    // Achievement system
    private val unlockedAchievements = mutableSetOf<Achievement>()
    private var pendingAchievements = mutableListOf<Achievement>()
    
    // Screen shake
    private var shakeIntensity = 0f
    private var shakeDuration = 0f
    private var shakeOffsetX = 0f
    private var shakeOffsetY = 0f
    
    enum class GameState {
        LOADING, MENU, PLAYING, PAUSED, GAME_OVER, BOSS_FIGHT, TRANSITION
    }
    
    enum class Achievement(val displayName: String, val description: String, val points: Int) {
        FIRST_KILL("First Blood", "Destroy your first enemy", 10),
        COMBO_5("Combo Novice", "Achieve a 5x combo", 25),
        COMBO_20("Combo Master", "Achieve a 20x combo", 100),
        COMBO_50("Combo God", "Achieve a 50x combo", 500),
        SCORE_1000("Point Collector", "Score 1,000 points", 20),
        SCORE_10000("Score Hunter", "Score 10,000 points", 100),
        SCORE_100000("Score Legend", "Score 100,000 points", 1000),
        SURVIVOR_60("Survivor", "Survive for 60 seconds", 50),
        SURVIVOR_300("Veteran", "Survive for 5 minutes", 250),
        SURVIVOR_600("Immortal", "Survive for 10 minutes", 1000),
        BOSS_SLAYER("Boss Slayer", "Defeat your first boss", 100),
        PERFECT_WAVE("Perfect Wave", "Clear a wave without taking damage", 200),
        WEAPON_MASTER("Weapon Master", "Use all weapon types", 150),
        SKIN_COLLECTOR("Skin Collector", "Unlock all skins", 300)
    }
    
    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
        
        // Initialize paints
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true
        textPaint.typeface = Typeface.DEFAULT_BOLD
        
        hudPaint.isAntiAlias = true
        hudPaint.textAlign = Paint.Align.LEFT
        hudPaint.typeface = Typeface.MONOSPACE
        
        particlePaint.isAntiAlias = true
        particlePaint.style = Paint.Style.FILL
        
        // Initialize particles
        for (i in 0 until 100) {
            particles.add(createBackgroundParticle())
        }
        
        // Load high score
        loadHighScore()
    }
    
    private fun createBackgroundParticle(): BackgroundParticle {
        return BackgroundParticle(
            x = Random.nextFloat() * 2000f,
            y = Random.nextFloat() * 4000f,
            size = Random.nextFloat() * 3f + 1f,
            speed = Random.nextFloat() * 1f + 0.2f,
            alpha = Random.nextInt(50, 200),
            twinkleSpeed = Random.nextFloat() * 2f + 0.5f
        )
    }
    
    private fun loadHighScore() {
        val prefs = context.getSharedPreferences("aircraft_war_pro", Context.MODE_PRIVATE)
        highScore = prefs.getLong("high_score", 0)
    }
    
    private fun saveHighScore() {
        val prefs = context.getSharedPreferences("aircraft_war_pro", Context.MODE_PRIVATE)
        prefs.edit().putLong("high_score", highScore).apply()
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        scaleFactor = min(screenWidth / 720f, screenHeight / 1280f)
        
        // Initialize player position
        player = StyledPlayer(
            screenWidth / 2 - 60f * scaleFactor,
            screenHeight - 180f * scaleFactor,
            120f * scaleFactor,
            140f * scaleFactor
        )
        
        gameMap.init(screenWidth, screenHeight)
        gameState = GameState.MENU
        
        // Start game loop
        gameThread = GameThread(holder)
        gameThread?.start()
        isRunning = true
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        scaleFactor = min(screenWidth / 720f, screenHeight / 1280f)
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        gameThread?.stopThread()
    }
    
    fun showWelcomeMessage() {
        // TODO: Show welcome toast
    }
    
    fun showToastMessage(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    fun pauseGame() {
        if (gameState == GameState.PLAYING || gameState == GameState.BOSS_FIGHT) {
            gameState = GameState.PAUSED
        }
    }
    
    fun resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING
        }
    }
    
    fun isGameInProgress(): Boolean {
        return gameState == GameState.PLAYING || gameState == GameState.BOSS_FIGHT
    }
    
    fun destroy() {
        isRunning = false
        saveHighScore()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                touchX = event.x
                touchY = event.y
                touchStartTime = System.currentTimeMillis()
                
                // Double tap detection
                val tapTime = System.currentTimeMillis()
                if (tapTime - lastTapTime < 300) {
                    doubleTapDetected = true
                    handleDoubleTap()
                }
                lastTapTime = tapTime
                
                when (gameState) {
                    GameState.MENU -> startNewGame()
                    GameState.GAME_OVER -> {
                        if (System.currentTimeMillis() - touchStartTime > 500) {
                            gameState = GameState.MENU
                        }
                    }
                    GameState.PAUSED -> resumeGame()
                    else -> {}
                }
            }
            MotionEvent.ACTION_MOVE -> {
                touchX = event.x
                touchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
                doubleTapDetected = false
            }
        }
        return true
    }
    
    private fun handleDoubleTap() {
        when (gameState) {
            GameState.PLAYING -> {
                // Activate special ability
                if (player.shield >= 50) {
                    screenShake(0.5f, 0.3f)
                    // Clear nearby bullets
                    bullets.removeAll { bullet ->
                        !bullet.isPlayerBullet && 
                        distance(bullet.x, bullet.y, player.getCenterX(), player.getCenterY()) < 200f
                    }
                    player.shield = 0
                }
            }
            else -> {}
        }
    }
    
    private fun startNewGame() {
        // Reset all stats
        score = 0
        level = 1
        lives = MAX_LIVES
        comboCount = 0
        maxCombo = 0
        comboMultiplier = 1f
        waveNumber = 0
        totalEnemiesKilled = 0
        totalShotsFired = 0
        accuracyRate = 0f
        gameTime = 0f
        enemySpawnTimer = 0f
        powerUpSpawnTimer = 0f
        difficultyTimer = 0f
        bossSpawnCounter = 0
        invincibilityTimer = INVINCIBILITY_DURATION
        
        // Clear game objects
        enemies.clear()
        bullets.clear()
        powerUps.clear()
        explosions.clear()
        damageNumbers.clear()
        pendingAchievements.clear()
        
        // Reset player
        player.reset()
        player.x = screenWidth / 2 - 60f * scaleFactor
        player.y = screenHeight - 180f * scaleFactor
        
        gameState = GameState.PLAYING
    }
    
    fun update() {
        if (gameState != GameState.PLAYING && gameState != GameState.BOSS_FIGHT) return
        
        val currentTime = System.nanoTime()
        val deltaTime = ((currentTime - lastUpdateTime) / 1_000_000_000f).coerceAtMost(0.1f)
        lastUpdateTime = currentTime
        
        gameTime += deltaTime
        
        // Update screen shake
        updateScreenShake(deltaTime)
        
        // Update invincibility
        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaTime
            player.isInvincible = true
        } else {
            player.isInvincible = false
        }
        
        // Update game systems
        gameMap.update(deltaTime, level)
        updatePlayer(deltaTime)
        updateEnemies(deltaTime)
        updateBullets(deltaTime)
        updatePowerUps(deltaTime)
        updateExplosions(deltaTime)
        updateParticles(deltaTime)
        updateDamageNumbers(deltaTime)
        updateCombo(deltaTime)
        
        // Spawning
        spawnEnemies(deltaTime)
        spawnPowerUps(deltaTime)
        updateDifficulty(deltaTime)
        
        // Collision detection
        checkCollisions()
        
        // Check boss fights
        checkBossFight()
        
        // Check achievements
        checkAchievements()
        
        // Check game over
        if (player.health <= 0) {
            lives--
            if (lives <= 0) {
                gameOver()
            } else {
                // Respawn player
                player.health = 100
                player.shield = 50
                invincibilityTimer = INVINCIBILITY_DURATION
                screenShake(1f, 0.5f)
            }
        }
        
        // Update high score
        if (score > highScore) {
            highScore = score
            saveHighScore()
        }
    }
    
    private fun updateScreenShake(deltaTime: Float) {
        if (shakeDuration > 0) {
            shakeDuration -= deltaTime
            shakeOffsetX = (Random.nextFloat() - 0.5f) * shakeIntensity * 20f
            shakeOffsetY = (Random.nextFloat() - 0.5f) * shakeIntensity * 20f
        } else {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
        }
    }
    
    private fun screenShake(intensity: Float, duration: Float) {
        shakeIntensity = intensity
        shakeDuration = duration
    }
    
    private fun updatePlayer(deltaTime: Float) {
        if (isTouching && gameState == GameState.PLAYING) {
            val targetX = touchX - player.width / 2
            val targetY = touchY - player.height / 2
            
            // Smooth movement with easing
            player.x += (targetX - player.x) * 0.12f
            player.y += (targetY - player.y) * 0.12f
            
            // Auto-fire with improved logic
            fireWeapons(deltaTime)
        }
        
        // Constrain to screen bounds
        player.x = player.x.coerceIn(-20f, screenWidth - player.width + 20f)
        player.y = player.y.coerceIn(-20f, screenHeight - player.height + 20f)
        
        player.update(deltaTime)
    }
    
    private fun fireWeapons(deltaTime: Float) {
        when (player.primaryWeapon) {
            StyledPlayer.WeaponType.PLASMA_CANNON -> {
                if (totalShotsFired % 5 == 0L) {
                    bullets.add(PlasmaBullet(player.getCenterX() - 15f, player.y + 10f, 0f, -20f, true))
                    bullets.add(PlasmaBullet(player.getCenterX() + 15f, player.y + 10f, 0f, -20f, true))
                    totalShotsFired += 2
                }
            }
            StyledPlayer.WeaponType.LASER_BEAM -> {
                if (totalShotsFired % 3 == 0L) {
                    bullets.add(LaserBullet(player.getCenterX(), player.y, 0f, -25f, true))
                    totalShotsFired++
                }
            }
            StyledPlayer.WeaponType.SPREAD_SHOT -> {
                if (totalShotsFired % 8 == 0L) {
                    for (i in -1..1) {
                        bullets.add(SpreadBullet(player.getCenterX() + i * 20f, player.y + 20f, i * 1.5f, -15f, true))
                    }
                    totalShotsFired += 3
                }
            }
            StyledPlayer.WeaponType.RAILGUN -> {
                if (totalShotsFired % 15 == 0L) {
                    bullets.add(ExplosiveBullet(player.getCenterX(), player.y, 0f, -30f, true))
                    totalShotsFired++
                    screenShake(0.3f, 0.1f)
                }
            }
            StyledPlayer.WeaponType.PARTICLE_BEAM -> {
                if (totalShotsFired % 2 == 0L) {
                    bullets.add(ParticleBeam(
                        player.getCenterX() + Random.nextFloat() * 10f - 5f,
                        player.y + 15f,
                        0f, -12f, true
                    ))
                    totalShotsFired++
                }
            }
            StyledPlayer.WeaponType.MISSILE_LAUNCHER -> {
                val nearestEnemy = enemies.minByOrNull {
                    distance(it.x + it.width/2, it.y + it.height/2, player.getCenterX(), player.getCenterY())
                }
                if (totalShotsFired % 20 == 0L && nearestEnemy != null) {
                    bullets.add(MissileBullet(
                        player.getCenterX() - 15f, player.y,
                        nearestEnemy.x + nearestEnemy.width/2,
                        nearestEnemy.y + nearestEnemy.height/2,
                        true
                    ))
                    bullets.add(MissileBullet(
                        player.getCenterX() + 15f, player.y,
                        nearestEnemy.x + nearestEnemy.width/2,
                        nearestEnemy.y + nearestEnemy.height/2,
                        true
                    ))
                    totalShotsFired += 2
                }
            }
        }
    }
    
    private fun updateEnemies(deltaTime: Float) {
        val enemiesToRemove = mutableListOf<StyledEnemy>()
        
        for (enemy in enemies) {
            enemy.update(deltaTime)
            
            // Enemy shooting
            val enemyBullets = enemy.shoot()
            if (enemyBullets.isNotEmpty()) {
                bullets.addAll(enemyBullets)
            }
            
            // Remove if off screen or dead
            if (!enemy.isAlive || enemy.y > screenHeight + 200f) {
                enemiesToRemove.add(enemy)
                
                if (!enemy.isAlive) {
                    // Add score and effects
                    score += (enemy.points * comboMultiplier).toLong()
                    totalEnemiesKilled++
                    
                    // Create explosion
                    explosions.add(ExplosionEffect(
                        enemy.x + enemy.width / 2,
                        enemy.y + enemy.height / 2,
                        if (enemy is BossEnemy) 3f else 1f
                    ))
                    
                    // Add damage number
                    damageNumbers.add(DamageNumber(
                        enemy.x + enemy.width / 2,
                        enemy.y,
                        "+${(enemy.points * comboMultiplier).toInt()}",
                        Color.YELLOW
                    ))
                    
                    // Screen shake for bosses
                    if (enemy is BossEnemy) {
                        screenShake(1.5f, 1f)
                    }
                }
            }
        }
        
        enemies.removeAll(enemiesToRemove)
    }
    
    private fun updateBullets(deltaTime: Float) {
        bullets.forEach { it.update(deltaTime) }
        bullets.removeAll { !it.isActive || it.isOffScreen(screenWidth, screenHeight) }
    }
    
    private fun updatePowerUps(deltaTime: Float) {
        powerUps.forEach {
            it.y += 3f * scaleFactor
            it.animationTime += deltaTime
        }
        powerUps.removeAll { it.y > screenHeight + 100f }
    }
    
    private fun updateExplosions(deltaTime: Float) {
        explosions.forEach { it.update(deltaTime) }
        explosions.removeAll { it.isFinished() }
    }
    
    private fun updateParticles(deltaTime: Float) {
        for (particle in particles) {
            particle.y += particle.speed
            particle.animationTime += deltaTime * particle.twinkleSpeed
            particle.currentAlpha = (sin(particle.animationTime) * 50 + particle.alpha).toInt()
            
            if (particle.y > screenHeight + 10f) {
                particle.y = -10f
                particle.x = Random.nextFloat() * screenWidth
            }
        }
    }
    
    private fun updateDamageNumbers(deltaTime: Float) {
        damageNumbers.forEach { it.update(deltaTime) }
        damageNumbers.removeAll { it.alpha <= 0 }
    }
    
    private fun updateCombo(deltaTime: Float) {
        if (comboCount > 0) {
            comboTimer -= deltaTime
            if (comboTimer <= 0) {
                comboCount = 0
                comboMultiplier = 1f
            }
        }
    }
    
    private 
