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
    private var comboTimer = 0f  // AJOUTÉ ICI
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
        
        player = StyledPlayer(
            screenWidth / 2 - 60f * scaleFactor,
            screenHeight - 180f * scaleFactor,
            120f * scaleFactor,
            140f * scaleFactor
        )
        
        gameMap.init(screenWidth, screenHeight)
        gameState = GameState.MENU
        
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
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Welcome to Aircraft War Pro!", Toast.LENGTH_SHORT).show()
        }
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
        if (gameState == GameState.PLAYING && player.shield >= 50) {
            screenShake(0.5f, 0.3f)
            bullets.removeAll { bullet ->
                !bullet.isPlayerBullet &&
                distance(bullet.x, bullet.y, player.getCenterX(), player.getCenterY()) < 200f
            }
            player.shield = 0
        }
    }
    
    private fun startNewGame() {
        score = 0
        level = 1
        lives = MAX_LIVES
        comboCount = 0
        maxCombo = 0
        comboMultiplier = 1f
        comboTimer = 0f
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
        
        enemies.clear()
        bullets.clear()
        powerUps.clear()
        explosions.clear()
        damageNumbers.clear()
        pendingAchievements.clear()
        
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
        
        updateScreenShake(deltaTime)
        
        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaTime
            player.isInvincible = true
        } else {
            player.isInvincible = false
        }
        
        gameMap.update(deltaTime, level)
        updatePlayer(deltaTime)
        updateEnemies(deltaTime)
        updateBullets(deltaTime)
        updatePowerUps(deltaTime)
        updateExplosions(deltaTime)
        updateParticles(deltaTime)
        updateDamageNumbers(deltaTime)
        updateCombo(deltaTime)
        
        spawnEnemies(deltaTime)
        spawnPowerUps(deltaTime)
        updateDifficulty(deltaTime)
        
        checkCollisions()
        checkBossFight()
        checkAchievements()
        
        if (player.health <= 0) {
            lives--
            if (lives <= 0) {
                gameOver()
            } else {
                player.health = 100
                player.shield = 50
                invincibilityTimer = INVINCIBILITY_DURATION
                screenShake(1f, 0.5f)
            }
        }
        
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
            
            player.x += (targetX - player.x) * 0.12f
            player.y += (targetY - player.y) * 0.12f
            
            fireWeapons(deltaTime)
        }
        
        player.x = player.x.coerceIn(-20f, screenWidth - player.width + 20f)
        player.y = player.y.coerceIn(-20f, screenHeight - player.height + 20f)
        
        player.update(deltaTime)
    }
    
    private fun fireWeapons(deltaTime: Float) {
        when (player.primaryWeapon) {
            StyledPlayer.WeaponType.PLASMA_CANNON -> {
                if (totalShotsFired % 5 == 0) {
                    bullets.add(PlasmaBullet(player.getCenterX() - 15f, player.y + 10f, 0f, -20f, true))
                    bullets.add(PlasmaBullet(player.getCenterX() + 15f, player.y + 10f, 0f, -20f, true))
                    totalShotsFired += 2
                }
            }
            StyledPlayer.WeaponType.LASER_BEAM -> {
                if (totalShotsFired % 3 == 0) {
                    bullets.add(LaserBullet(player.getCenterX(), player.y, 0f, -25f, true))
                    totalShotsFired++
                }
            }
            StyledPlayer.WeaponType.SPREAD_SHOT -> {
                if (totalShotsFired % 8 == 0) {
                    for (i in -1..1) {
                        bullets.add(SpreadBullet(player.getCenterX() + i * 20f, player.y + 20f, i * 1.5f, -15f, true))
                    }
                    totalShotsFired += 3
                }
            }
            StyledPlayer.WeaponType.RAILGUN -> {
                if (totalShotsFired % 15 == 0) {
                    bullets.add(ExplosiveBullet(player.getCenterX(), player.y, 0f, -30f, true))
                    totalShotsFired++
                    screenShake(0.3f, 0.1f)
                }
            }
            StyledPlayer.WeaponType.PARTICLE_BEAM -> {
                if (totalShotsFired % 2 == 0) {
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
                if (totalShotsFired % 20 == 0 && nearestEnemy != null) {
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
            
            val enemyBullets = enemy.shoot()
            if (enemyBullets.isNotEmpty()) {
                bullets.addAll(enemyBullets)
            }
            
            if (!enemy.isAlive || enemy.y > screenHeight + 200f) {
                enemiesToRemove.add(enemy)
                
                if (!enemy.isAlive) {
                    score += (enemy.points * comboMultiplier).toLong()
                    totalEnemiesKilled++
                    
                    explosions.add(ExplosionEffect(
                        enemy.x + enemy.width / 2,
                        enemy.y + enemy.height / 2,
                        if (enemy is BossEnemy) 3f else 1f
                    ))
                    
                    damageNumbers.add(DamageNumber(
                        enemy.x + enemy.width / 2,
                        enemy.y,
                        "+${(enemy.points * comboMultiplier).toInt()}",
                        Color.YELLOW
                    ))
                    
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
    
    private fun spawnEnemies(deltaTime: Float) {
        enemySpawnTimer += deltaTime
        
        val baseInterval = when {
            gameState == GameState.BOSS_FIGHT -> 3f
            else -> (2.5f - level * 0.1f).coerceAtLeast(0.3f)
        }
        
        if (enemySpawnTimer >= baseInterval) {
            enemySpawnTimer = 0f
            
            val x = Random.nextFloat() * (screenWidth - 100f * scaleFactor)
            
            val enemy = when {
                bossSpawnCounter >= 10 && enemies.none { it is BossEnemy } -> {
                    bossSpawnCounter = 0
                    BossEnemy(screenWidth / 2 - 75f * scaleFactor, -150f * scaleFactor, level)
                }
                level >= 8 && Random.nextFloat() < 0.15f -> {
                    HeavyEnemy(x, -100f * scaleFactor, level)
                }
                level >= 4 && Random.nextFloat() < 0.4f -> {
                    FighterEnemy(x, -100f * scaleFactor, level)
                }
                else -> {
                    ScoutEnemy(x, -100f * scaleFactor, level)
                }
            }
            
            enemies.add(enemy)
            bossSpawnCounter++
        }
    }
    
    private fun spawnPowerUps(deltaTime: Float) {
        powerUpSpawnTimer += deltaTime
        
        if (powerUpSpawnTimer >= 10f && powerUps.size < 4) {
            powerUpSpawnTimer = 0f
            
            val x = Random.nextFloat() * (screenWidth - 40f * scaleFactor)
            val type = when (Random.nextInt(10)) {
                in 0..3 -> PowerUpType.HEALTH
                in 4..6 -> PowerUpType.SHIELD
                7 -> PowerUpType.WEAPON_UPGRADE
                8 -> PowerUpType.SCORE_MULTIPLIER
                9 -> PowerUpType.EXTRA_LIFE
                else -> PowerUpType.HEALTH
            }
            
            powerUps.add(PowerUpItem(x, -50f * scaleFactor, type))
        }
    }
    
    private fun updateDifficulty(deltaTime: Float) {
        difficultyTimer += deltaTime
        
        if (difficultyTimer >= DIFFICULTY_INCREASE_TIME) {
            difficultyTimer = 0f
            level = (level + 1).coerceAtMost(20)
            waveNumber++
        }
    }
    
    private fun checkCollisions() {
        for (bullet in bullets) {
            if (!bullet.isPlayerBullet || !bullet.isActive) continue
            
            for (enemy in enemies) {
                if (checkCollision(bullet, enemy)) {
                    bullet.isActive = false
                    enemy.takeDamage(bullet.damage)
                    
                    if (!enemy.isAlive) {
                        comboCount++
                        maxCombo = max(maxCombo, comboCount)
                        comboTimer = COMBO_TIMEOUT
                        
                        comboMultiplier = when {
                            comboCount >= 50 -> 5f
                            comboCount >= 20 -> 3f
                            comboCount >= 10 -> 2f
                            comboCount >= 5 -> 1.5f
                            else -> 1f
                        }
                        
                        damageNumbers.add(DamageNumber(
                            enemy.x + enemy.width/2,
                            enemy.y,
                            "${bullet.damage * comboMultiplier.toInt()}",
                            if (comboMultiplier > 2f) Color.rgb(255, 100, 0) else Color.YELLOW
                        ))
                    }
                    break
                }
            }
        }
        
        if (invincibilityTimer <= 0) {
            for (bullet in bullets) {
                if (bullet.isPlayerBullet || !bullet.isActive) continue
                
                if (checkCollision(bullet, player)) {
                    bullet.isActive = false
                    player.takeDamage(bullet.damage)
                    
                    damageNumbers.add(DamageNumber(
                        player.getCenterX(),
                        player.getCenterY(),
                        "-${bullet.damage}",
                        Color.RED
                    ))
                    
                    screenShake(0.5f, 0.2f)
                }
            }
        }
        
        for (powerUp in powerUps) {
            if (checkCollision(powerUp, player)) {
                applyPowerUp(powerUp)
                powerUp.collected = true
            }
        }
        powerUps.removeAll { it.collected }
        
        if (invincibilityTimer <= 0 && gameMap.isInHazardZone(
            player.getCenterX(), player.getCenterY(), 30f * scaleFactor
        )) {
            player.takeDamage(10)
        }
    }
    
    private fun checkCollision(obj1: Any, obj2: Any): Boolean {
        return when {
            obj1 is StyledBullet && obj2 is StyledEnemy -> {
                rectCollision(
                    obj1.x - 5f, obj1.y - 5f, 10f, 10f,
                    obj2.x, obj2.y, obj2.width, obj2.height
                )
            }
            obj1 is StyledBullet && obj2 is StyledPlayer -> {
                rectCollision(
                    obj1.x - 5f, obj1.y - 5f, 10f, 10f,
                    obj2.x, obj2.y, obj2.width, obj2.height
                )
            }
            obj1 is PowerUpItem && obj2 is StyledPlayer -> {
                rectCollision(
                    obj1.x, obj1.y, 40f * scaleFactor, 40f * scaleFactor,
                    obj2.x, obj2.y, obj2.width, obj2.height
                )
            }
            else -> false
        }
    }
    
    private fun rectCollision(
        x1: Float, y1: Float, w1: Float, h1: Float,
        x2: Float, y2: Float, w2: Float, h2: Float
    ): Boolean {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2
    }
    
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
    
    private fun applyPowerUp(powerUp: PowerUpItem) {
        when (powerUp.type) {
            PowerUpType.HEALTH -> {
                player.heal(30)
                damageNumbers.add(DamageNumber(
                    player.getCenterX(), player.getCenterY(), "+30 HP", Color.GREEN
                ))
            }
            PowerUpType.SHIELD -> {
                player.addShield(40)
                damageNumbers.add(DamageNumber(
                    player.getCenterX(), player.getCenterY(), "+40 SHIELD", Color.CYAN
                ))
            }
            PowerUpType.WEAPON_UPGRADE -> {
                val weapons = StyledPlayer.WeaponType.values()
                player.primaryWeapon = weapons[Random.nextInt(weapons.size)]
                player.activatePowerUp(StyledPlayer.PowerUpEffect.WEAPON_OVERCHARGE, 15000)
                damageNumbers.add(DamageNumber(
                    player.getCenterX(), player.getCenterY(), "WEAPON UP!", Color.MAGENTA
                ))
            }
            PowerUpType.SCORE_MULTIPLIER -> {
                comboCount += 5
                comboTimer = 3f
                damageNumbers.add(DamageNumber(
                    player.getCenterX(), player.getCenterY(), "SCORE x${comboMultiplier.toInt()}!", Color.YELLOW
                ))
            }
            PowerUpType.EXTRA_LIFE -> {
                lives = (lives + 1).coerceAtMost(MAX_LIVES + 2)
                damageNumbers.add(DamageNumber(
                    player.getCenterX(), player.getCenterY(), "+1 LIFE", Color.rgb(255, 200, 0)
                ))
            }
        }
    }
    
    private fun checkBossFight() {
        if (enemies.any { it is BossEnemy } && gameState == GameState.PLAYING) {
            gameState = GameState.BOSS_FIGHT
        } else if (enemies.none { it is BossEnemy } && gameState == GameState.BOSS_FIGHT) {
            gameState = GameState.PLAYING
            level++
        }
    }
    
    private fun checkAchievements() {
        val newAchievements = mutableListOf<Achievement>()
        
        if (totalEnemiesKilled >= 1) newAchievements.add(Achievement.FIRST_KILL)
        if (comboCount >= 5) newAchievements.add(Achievement.COMBO_5)
        if (maxCombo >= 20) newAchievements.add(Achievement.COMBO_20)
        if (maxCombo >= 50) newAchievements.add(Achievement.COMBO_50)
        if (score >= 1000) newAchievements.add(Achievement.SCORE_1000)
        if (score >= 10000) newAchievements.add(Achievement.SCORE_10000)
        if (score >= 100000) newAchievements.add(Achievement.SCORE_100000)
        if (gameTime >= 60) newAchievements.add(Achievement.SURVIVOR_60)
        if (gameTime >= 300) newAchievements.add(Achievement.SURVIVOR_300)
        if (gameTime >= 600) newAchievements.add(Achievement.SURVIVOR_600)
        
        for (achievement in newAchievements) {
            if (unlockedAchievements.add(achievement)) {
                pendingAchievements.add(achievement)
                score += achievement.points
            }
        }
    }
    
    private fun gameOver() {
        gameState = GameState.GAME_OVER
        saveHighScore()
    }
    
    fun draw(canvas: Canvas?) {
        if (canvas == null) return
        
        canvas.save()
        
        if (shakeDuration > 0) {
            canvas.translate(shakeOffsetX, shakeOffsetY)
        }
        
        when (gameState) {
            GameState.MENU -> drawMenu(canvas)
            GameState.PLAYING, GameState.BOSS_FIGHT -> drawGameplay(canvas)
            GameState.PAUSED -> {
                drawGameplay(canvas)
                drawPauseOverlay(canvas)
            }
            GameState.GAME_OVER -> drawGameOver(canvas)
            GameState.TRANSITION -> drawTransition(canvas)
            else -> {}
        }
        
        canvas.restore()
    }
    
    private fun drawMenu(canvas: Canvas) {
        drawAnimatedBackground(canvas)
        
        val titlePaint = Paint(textPaint)
        titlePaint.textSize = 90f * scaleFactor
        
        for (i in 3 downTo 1) {
            titlePaint.color = Color.argb(30 / i, 0, 150 + i * 30, 255)
            titlePaint.textSize = (90f + i * 5f) * scaleFactor
            canvas.drawText("AIRCRAFT", screenWidth / 2, screenHeight * 0.25f, titlePaint)
        }
        
        titlePaint.color = Color.rgb(0, 200, 255)
        titlePaint.textSize = 90f * scaleFactor
        canvas.drawText("AIRCRAFT", screenWidth / 2, screenHeight * 0.25f, titlePaint)
        
        titlePaint.color = Color.rgb(255, 80, 0)
        canvas.drawText("WAR PRO", screenWidth / 2, screenHeight * 0.35f, titlePaint)
        
        textPaint.textSize = 20f * scaleFactor
        textPaint.color = Color.GRAY
        canvas.drawText("v2.0 ULTIMATE", screenWidth / 2, screenHeight * 0.42f, textPaint)
        
        textPaint.textSize = 30f * scaleFactor
        textPaint.color = Color.YELLOW
        canvas.drawText("HIGH SCORE: $highScore", screenWidth / 2, screenHeight * 0.72f, textPaint)
        
        val pulseAlpha = (sin(System.currentTimeMillis() * 0.003) * 50 + 150).toInt()
        val buttonPaint = Paint()
        buttonPaint.color = Color.argb(pulseAlpha, 0, 150, 255)
        buttonPaint.isAntiAlias = true
        
        val buttonX = screenWidth / 2
        val buttonY = screenHeight * 0.85f
        val buttonRadius = 80f * scaleFactor
        
        buttonPaint.maskFilter = BlurMaskFilter(20f * scaleFactor, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(buttonX, buttonY, buttonRadius, buttonPaint)
        buttonPaint.maskFilter = null
        
        buttonPaint.color = Color.rgb(0, 100, 200)
        canvas.drawCircle(buttonX, buttonY, buttonRadius * 0.8f, buttonPaint)
        
        textPaint.textSize = 35f * scaleFactor
        textPaint.color = Color.WHITE
        canvas.drawText("START", buttonX, buttonY + 12f * scaleFactor, textPaint)
        
        textPaint.textSize = 25f * scaleFactor
        textPaint.color = Color.argb(150, 255, 255, 255)
        canvas.drawText("Touch & Drag to Move", screenWidth / 2, screenHeight * 0.95f, textPaint)
    }
    
    private fun drawAnimatedBackground(canvas: Canvas) {
        canvas.drawColor(Color.rgb(5, 5, 20))
        
        for (particle in particles) {
            particlePaint.color = Color.argb(particle.currentAlpha, 255, 255, 255)
            canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint)
        }
        
        val starPaint = Paint()
        starPaint.color = Color.WHITE
        for (i in 0 until 50) {
            val x = (sin(i * 123.4 + System.currentTimeMillis() * 0.0005) * 0.5 + 0.5) * screenWidth
            val y = (cos(i * 56.7 + System.currentTimeMillis() * 0.0003) * 0.5 + 0.5) * screenHeight
            val alpha = (sin(System.currentTimeMillis() * 0.002 + i) * 100 + 155).toInt()
            starPaint.alpha = alpha
            canvas.drawCircle(x.toFloat(), y.toFloat(), 2f * scaleFactor, starPaint)
        }
    }
    
    private fun drawGameplay(canvas: Canvas) {
        gameMap.draw(canvas)
        
        for (particle in particles) {
            particlePaint.color = Color.argb(particle.currentAlpha, 255, 255, 255)
            canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint)
        }
        
        for (powerUp in powerUps) {
            powerUp.draw(canvas, scaleFactor)
        }
        
        for (enemy in enemies) {
            enemy.draw(canvas)
        }
        
        for (bullet in bullets) {
            bullet.draw(canvas)
        }
        
        for (explosion in explosions) {
            explosion.draw(canvas)
        }
        
        player.draw(canvas)
        
        for (damage in damageNumbers) {
            damage.draw(canvas, textPaint)
        }
        
        drawHUD(canvas)
        drawAchievements(canvas)
    }
    
    private fun drawHUD(canvas: Canvas) {
        val hudScale = scaleFactor
        
        hudPaint.textSize = 40f * hudScale
        hudPaint.color = Color.WHITE
        canvas.drawText("SCORE", 20f, 40f * hudScale, hudPaint)
        
        hudPaint.textSize = 35f * hudScale
        hudPaint.color = Color.YELLOW
        canvas.drawText("$score", 20f, 80f * hudScale, hudPaint)
        
        hudPaint.textSize = 40f * hudScale
        hudPaint.color = Color.WHITE
        canvas.drawText("LIVES", screenWidth - 150f, 40f * hudScale, hudPaint)
        
        hudPaint.color = Color.RED
        canvas.drawText("❤️".repeat(lives), screenWidth - 150f, 80f * hudScale, hudPaint)
        
        hudPaint.textSize = 30f * hudScale
        hudPaint.color = Color.rgb(255, 200, 0)
        canvas.drawText("LEVEL $level", 20f, screenHeight - 60f, hudPaint)
        
        if (comboCount > 1) {
            val comboSize = (30f + comboCount * 2f).coerceAtMost(80f) * hudScale
            textPaint.textSize = comboSize
            
            val comboColor = when {
                comboCount >= 50 -> Color.rgb(255, 0, 255)
                comboCount >= 20 -> Color.rgb(255, 0, 0)
                comboCount >= 10 -> Color.rgb(255, 100, 0)
                else -> Color.rgb(255, 200, 0)
            }
            
            textPaint.color = comboColor
            val comboText = "${comboCount}x COMBO!"
            val comboX = screenWidth / 2
            val comboY = screenHeight * 0.15f
            
            textPaint.setShadowLayer(20f * hudScale, 0f, 0f, comboColor)
            canvas.drawText(comboText, comboX, comboY, textPaint)
            textPaint.clearShadowLayer()
            canvas.drawText(comboText, comboX, comboY, textPaint)
        }
        
        drawHealthBar(canvas, screenWidth - 250f, 20f, 230f, 30f, player.health / 100f, Color.GREEN)
        
        if (player.shield > 0) {
            drawHealthBar(canvas, screenWidth - 250f, 55f, 230f, 15f, player.shield / 50f, Color.CYAN)
        }
    }
    
    private fun drawHealthBar(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, percent: Float, color: Int) {
        val paint = Paint()
        
        paint.color = Color.argb(150, 50, 50, 50)
        canvas.drawRoundRect(x, y, x + width, y + height, 5f, 5f, paint)
        
        val gradient = LinearGradient(x, y, x + width, y, color, Color.argb(100, Color.red(color), Color.green(color), Color.blue(color)), Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawRoundRect(x, y, x + width * percent, y + height, 5f, 5f, paint)
        paint.shader = null
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        canvas.drawRoundRect(x, y, x + width, y + height, 5f, 5f, paint)
        paint.style = Paint.Style.FILL
    }
    
    private fun drawPauseOverlay(canvas: Canvas) {
        val overlayPaint = Paint()
        overlayPaint.color = Color.argb(200, 0, 0, 0)
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, overlayPaint)
        
        textPaint.textSize = 80f * scaleFactor
        textPaint.color = Color.WHITE
        canvas.drawText("PAUSED", screenWidth / 2, screenHeight / 2 - 50f, textPaint)
        
        textPaint.textSize = 35f * scaleFactor
        canvas.drawText("Tap to Resume", screenWidth / 2, screenHeight / 2 + 50f, textPaint)
        
        val buttonPaint = Paint()
        buttonPaint.color = Color.rgb(0, 150, 0)
        val buttonLeft = screenWidth / 2 - 100f
        val buttonTop = screenHeight / 2 + 80f
        val buttonRight = screenWidth / 2 + 100f
        val buttonBottom = screenHeight / 2 + 140f
        canvas.drawRoundRect(buttonLeft, buttonTop, buttonRight, buttonBottom, 20f, 20f, buttonPaint)
        
        textPaint.textSize = 30f * scaleFactor
        textPaint.color = Color.WHITE
        canvas.drawText("RESUME", screenWidth / 2, buttonTop + 42f, textPaint)
    }
    
    private fun drawGameOver(canvas: Canvas) {
        drawGameplay(canvas)
        
        val overlayPaint = Paint()
        overlayPaint.color = Color.argb(220, 0, 0, 0)
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, overlayPaint)
        
        textPaint.textSize = 80f * scaleFactor
        textPaint.color = Color.RED
        canvas.drawText("GAME OVER", screenWidth / 2, screenHeight * 0.2f, textPaint)
        
        textPaint.textSize = 35f * scaleFactor
        textPaint.color = Color.WHITE
        
        val statsY = screenHeight * 0.35f
        val statsSpacing = 55f * scaleFactor
        
        canvas.drawText("SCORE: $score", screenWidth / 2, statsY, textPaint)
        canvas.drawText("LEVEL: $level", screenWidth / 2, statsY + statsSpacing, textPaint)
        canvas.drawText("ENEMIES KILLED: $totalEnemiesKilled", screenWidth / 2, statsY + statsSpacing * 2, textPaint)
        canvas.drawText("MAX COMBO: ${maxCombo}x", screenWidth / 2, statsY + statsSpacing * 3, textPaint)
        canvas.drawText("SURVIVED: ${gameTime.toInt()}s", screenWidth / 2, statsY + statsSpacing * 4, textPaint)
        
        if (score >= highScore && score > 0) {
            textPaint.textSize = 45f * scaleFactor
            textPaint.color = Color.rgb(255, 200, 0)
            canvas.drawText("★ NEW HIGH SCORE! ★", screenWidth / 2, statsY + statsSpacing * 5, textPaint)
        }
        
        if (pendingAchievements.isNotEmpty()) {
            textPaint.textSize = 30f * scaleFactor
            textPaint.color = Color.rgb(0, 255, 0)
            canvas.drawText("Achievements Unlocked: ${pendingAchievements.size}", screenWidth / 2, statsY + statsSpacing * 6, textPaint)
        }
        
        val pulseAlpha = (sin(System.currentTimeMillis() * 0.003) * 50 + 150).toInt()
        textPaint.textSize = 30f * scaleFactor
        textPaint.color = Color.argb(pulseAlpha, 255, 255, 255)
        canvas.drawText("TAP TO CONTINUE", screenWidth / 2, screenHeight * 0.9f, textPaint)
    }
    
    private fun drawTransition(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        
        textPaint.textSize = 60f * scaleFactor
        textPaint.color = Color.WHITE
        canvas.drawText("LEVEL $level", screenWidth / 2, screenHeight / 2, textPaint)
    }
    
    private fun drawAchievements(canvas: Canvas) {
        if (pendingAchievements.isNotEmpty()) {
            val achievement = pendingAchievements.first()
            val achievementY = screenHeight * 0.3f
            
            val bgPaint = Paint()
            bgPaint.color = Color.argb(200, 0, 0, 0)
            canvas.drawRoundRect(
                screenWidth / 2 - 200f, achievementY - 40f,
                screenWidth / 2 + 200f, achievementY + 40f,
                20f, 20f, bgPaint
            )
            
            textPaint.textSize = 25f * scaleFactor
            textPaint.color = Color.rgb(0, 255, 0)
            canvas.drawText("ACHIEVEMENT UNLOCKED!", screenWidth / 2, achievementY - 10f, textPaint)
            
            textPaint.textSize = 20f * scaleFactor
            textPaint.color = Color.WHITE
            canvas.drawText(achievement.displayName, screenWidth / 2, achievementY + 20f, textPaint)
            
            if (System.currentTimeMillis() % 3000 < 16) {
                pendingAchievements.removeAt(0)
            }
        }
    }
    
    inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        private var running = false
        
        fun stopThread() {
            running = false
            try {
                join(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        
        override fun run() {
            running = true
            var lastFrameTime = System.nanoTime()
            val targetFrameTime = 1_000_000_000L / 60
            
            while (running && !isInterrupted) {
                val currentTime = System.nanoTime()
                val elapsed = currentTime - lastFrameTime
                
                if (elapsed >= targetFrameTime) {
                    var canvas: Canvas? = null
                    try {
                        canvas = surfaceHolder.lockCanvas()
                        if (canvas != null) {
                            update()
                            draw(canvas)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            canvas?.let { surfaceHolder.unlockCanvasAndPost(it) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    lastFrameTime = currentTime
                } else {
                    try {
                        val sleepTime = (targetFrameTime - elapsed) / 1_000_000
                        if (sleepTime > 0) sleep(sleepTime)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
        }
    }
    
    data class PowerUpItem(
        var x: Float,
        var y: Float,
        val type: PowerUpType,
        var collected: Boolean = false,
        var animationTime: Float = 0f
    ) {
        fun draw(canvas: Canvas, scale: Float) {
            val paint = Paint()
            paint.isAntiAlias = true
            
            val centerX = x + 20f * scale
            val centerY = y + 20f * scale
            val radius = 20f * scale
            val pulse = sin(animationTime * 3f) * 0.2f + 1f
            
            when (type) {
                PowerUpType.HEALTH -> {
                    paint.shader = RadialGradient(
                        centerX, centerY, radius * pulse,
                        Color.GREEN, Color.rgb(0, 100, 0),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawCircle(centerX, centerY, radius * pulse, paint)
                    
                    paint.shader = null
                    paint.color = Color.WHITE
                    paint.strokeWidth = 3f * scale
                    canvas.drawLine(centerX - 8f * scale, centerY, centerX + 8f * scale, centerY, paint)
                    canvas.drawLine(centerX, centerY - 8f * scale, centerX, centerY + 8f * scale, paint)
                }
                PowerUpType.SHIELD -> {
                    paint.shader = RadialGradient(
                        centerX, centerY, radius * pulse,
                        Color.CYAN, Color.rgb(0, 100, 150),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawCircle(centerX, centerY, radius * pulse, paint)
                }
                PowerUpType.WEAPON_UPGRADE -> {
                    paint.shader = RadialGradient(
                        centerX, centerY, radius * pulse,
                        Color.MAGENTA, Color.rgb(100, 0, 100),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawCircle(centerX, centerY, radius * pulse, paint)
                    
                    paint.shader = null
                    paint.color = Color.WHITE
                    val path = Path()
                    for (i in 0..4) {
                        val angle = (i * 72f - 90f) * PI / 180f
                        val x1 = centerX + cos(angle).toFloat() * 12f * scale
                        val y1 = centerY + sin(angle).toFloat() * 12f * scale
                        if (i == 0) path.moveTo(x1, y1) else path.lineTo(x1, y1)
                    }
                    path.close()
                    canvas.drawPath(path, paint)
                }
                PowerUpType.SCORE_MULTIPLIER -> {
                    paint.shader = RadialGradient(
                        centerX, centerY, radius * pulse,
                        Color.YELLOW, Color.rgb(200, 100, 0),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawCircle(centerX, centerY, radius * pulse, paint)
                    
                    paint.shader = null
                    paint.color = Color.WHITE
                    paint.textSize = 20f * scale
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("x2", centerX, centerY + 7f * scale, paint)
                }
                PowerUpType.EXTRA_LIFE -> {
                    paint.shader = RadialGradient(
                        centerX, centerY, radius * pulse,
                        Color.rgb(255, 100, 100), Color.rgb(150, 0, 0),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawCircle(centerX, centerY, radius * pulse, paint)
                    
                    paint.shader = null
                    paint.color = Color.WHITE
                    paint.textSize = 25f * scale
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("♥", centerX, centerY + 8f * scale, paint)
                }
            }
        }
    }
    
    enum class PowerUpType {
        HEALTH, SHIELD, WEAPON_UPGRADE, SCORE_MULTIPLIER, EXTRA_LIFE
    }
    
    data class ExplosionEffect(
        var x: Float,
        var y: Float,
        var scale: Float = 1f
    ) {
        private var time = 0f
        private val duration = 0.8f / scale
        private val particles = mutableListOf<ExplosionParticle>()
        private var initialized = false
        
        class ExplosionParticle(
            var x: Float, var y: Float,
            var velX: Float, var velY: Float,
            var alpha: Int, var size: Float,
            var color: Int
        )
        
        fun update(deltaTime: Float) {
            time += deltaTime
            
            if (!initialized) {
                initialized = true
                for (i in 0 until (25 * scale).toInt()) {
                    val angle = (i * 14.4f) * PI / 180f
                    val speed = Random.nextFloat() * 8f * scale + 3f
                    particles.add(ExplosionParticle(
                        x, y,
                        (cos(angle) * speed).toFloat(),
                        (sin(angle) * speed).toFloat(),
                        255,
                        Random.nextFloat() * 8f * scale + 4f,
                        Color.rgb(
                            Random.nextInt(200, 255),
                            Random.nextInt(50, 200),
                            0
                        )
                    ))
                }
            }
            
            particles.forEach { particle ->
                particle.x += particle.velX * (1f - time / duration)
                particle.y += particle.velY * (1f - time / duration)
                particle.alpha = (255 * (1f - time / duration)).toInt().coerceIn(0, 255)
                particle.size *= 0.97f
            }
        }
        
        fun isFinished(): Boolean = time >= duration
        
        fun draw(canvas: Canvas) {
            val paint = Paint()
            paint.isAntiAlias = true
            
            for (particle in particles) {
                paint.color = Color.argb(
                    particle.alpha,
                    Color.red(particle.color),
                    Color.green(particle.color),
                    Color.blue(particle.color)
                )
                canvas.drawCircle(particle.x, particle.y, particle.size, paint)
            }
            
            if (time < 0.2f) {
                paint.color = Color.argb(
                    (255 * (1f - time / 0.2f)).toInt(),
                    255, 255, 255
                )
                canvas.drawCircle(x, y, 50f * scale * (1f - time / 0.2f), paint)
            }
        }
    }
    
    data class BackgroundParticle(
        var x: Float,
        var y: Float,
        val size: Float,
        val speed: Float,
        val alpha: Int,
        val twinkleSpeed: Float,
        var animationTime: Float = 0f,
        var currentAlpha: Int = alpha
    )
    
    data class DamageNumber(
        var x: Float,
        var y: Float,
        val text: String,
        val color: Int,
        var alpha: Int = 255,
        var yOffset: Float = 0f
    ) {
        fun update(deltaTime: Float) {
            yOffset -= 2f * deltaTime * 60f
            alpha = (alpha - 3).coerceAtLeast(0)
        }
        
        fun draw(canvas: Canvas, paint: Paint) {
            paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
            paint.textSize = 30f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(text, x, y + yOffset, paint)
        }
    }
}