package com.aircraftwar.pro

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.SurfaceHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private val holder: SurfaceHolder = this.holder
    private var thread: Thread? = null
    private var gameRunning = true
    private var gamePaused = false
    private var gameOver = false

    private lateinit var gameMap: GameMap
    private lateinit var player: StyledPlayer
    private lateinit var apiService: ApiService
    
    private var score = 0
    private var playerId = ""
    private var gameTime = 0
    private var enemyCount = 0
    private var bulletsShot = 0
    
    // Statistiques
    private var enemiesTotalKilled = 0

    init {
        holder.addCallback(this)
        apiService = ApiService()
        val prefs = context.getSharedPreferences("AircraftWarPro", Context.MODE_PRIVATE)
        playerId = prefs.getString("playerId", "") ?: ""
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameMap = GameMap(width, height)
        player = StyledPlayer(width / 2f, height - 150f, 0)
        gameMap.generateMapTheme()
        
        thread = Thread(this)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameRunning = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        while (gameRunning) {
            try {
                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    synchronized(holder) {
                        if (!gamePaused) {
                            update()
                        }
                        render(canvas)
                    }
                    holder.unlockCanvasAndPost(canvas)
                }
                Thread.sleep(16) // ~60 FPS
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun update() {
        gameTime++
        
        if (!gameOver) {
            player.update()
            gameMap.update()
            player.mapLevel = gameMap.currentMapLevel

            // Collisions joueur-ennemi
            for (enemy in gameMap.enemies) {
                if (player.collidesWith(enemy)) {
                    gameOver()
                    return
                }
            }

            // Collisions balles-ennemis
            val iterator = gameMap.bullets.iterator()
            while (iterator.hasNext()) {
                val bullet = iterator.next()
                var hit = false
                
                val enemyIterator = gameMap.enemies.iterator()
                while (enemyIterator.hasNext()) {
                    val enemy = enemyIterator.next()
                    if (bullet.collidesWith(enemy)) {
                        score += (10 + gameMap.currentMapLevel * 5)
                        enemiesTotalKilled++
                        hit = true
                        enemyIterator.remove()
                    }
                }
                
                if (hit) {
                    iterator.remove()
                }
            }

            // Tir automatique
            if (gameTime % 12 == 0) {
                gameMap.addBullet(player.shoot())
                bulletsShot++
            }

            enemyCount = gameMap.enemies.size
        }
    }

    private fun render(canvas: Canvas) {
        gameMap.drawBackground(canvas)

        // Score
        val scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText("⭐ ${String.format("%,d", score)}", 30f, 60f, scorePaint)

        // Temps et difficulté
        val infoPaint = Paint().apply {
            color = Color.rgb(100, 200, 255)
            textSize = 36f
        }
        val timeInSeconds = gameTime / 60
        canvas.drawText("${gameMap.getMapName()} | ${timeInSeconds}s | Ennemis: $enemyCount", 
            30f, height - 40f, infoPaint)

        // Dessiner les objets
        player.draw(canvas)
        
        for (enemy in gameMap.enemies) {
            enemy.draw(canvas)
        }

        for (bullet in gameMap.bullets) {
            bullet.draw(canvas)
        }

        // Game Over
        if (gameOver) {
            renderGameOverScreen(canvas)
        }
    }

    private fun renderGameOverScreen(canvas: Canvas) {
        // Fond semi-transparent
        canvas.drawColor(Color.argb(150, 0, 0, 0))

        val gameOverPaint = Paint().apply {
            color = Color.RED
            textSize = 120f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText("GAME OVER", 50f, height / 2f - 100f, gameOverPaint)

        val scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
        }
        canvas.drawText("Score Final: ${String.format("%,d", score)}", 
            100f, height / 2f + 50f, scorePaint)

        val statsPaint = Paint().apply {
            color = Color.rgb(100, 255, 100)
            textSize = 40f
        }
        canvas.drawText("Ennemis tués: $enemiesTotalKilled | Durée: ${gameTime / 60}s", 
            80f, height / 2f + 150f, statsPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            player.moveTo(event.x)
        }
        return true
    }

    private fun gameOver() {
        gameOver = true
        gameRunning = false

        GlobalScope.launch(Dispatchers.Main) {
            apiService.sendScore(playerId, score)
            
            val intent = Intent(context, ResultActivity::class.java)
            intent.putExtra("score", score)
            intent.putExtra("result", "DÉFAITE")
            intent.putExtra("mapLevel", gameMap.currentMapLevel)
            intent.putExtra("gameTime", gameTime / 60)
            intent.putExtra("enemiesTotalKilled", enemiesTotalKilled)
            intent.putExtra("bulletsShot", bulletsShot)
            context.startActivity(intent)
            (context as GameActivity).finish()
        }
    }

    fun pause() {
        gamePaused = true
    }

    fun resume() {
        gamePaused = false
    }
}
