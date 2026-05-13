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

class GameView(context: Context) : SurfaceView(context),
    SurfaceHolder.Callback, Runnable {

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

        gameRunning = true
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
            val canvas: Canvas? = holder.lockCanvas()

            if (canvas != null) {
                synchronized(holder) {
                    if (!gamePaused && !gameOver) {
                        update()
                    }
                    drawGame(canvas)
                }
                holder.unlockCanvasAndPost(canvas)
            }

            Thread.sleep(16) // ~60 FPS
        }
    }

    private fun update() {
        gameTime++

        player.update()
        gameMap.update()
        player.mapLevel = gameMap.currentMapLevel

        // Collision joueur
        for (enemy in gameMap.enemies) {
            if (player.collidesWith(enemy)) {
                triggerGameOver()
                return
            }
        }

        // Balles vs ennemis
        val bulletIterator = gameMap.bullets.iterator()
        while (bulletIterator.hasNext()) {
            val bullet = bulletIterator.next()
            var hit = false

            val enemyIterator = gameMap.enemies.iterator()
            while (enemyIterator.hasNext()) {
                val enemy = enemyIterator.next()
                if (bullet.collidesWith(enemy)) {
                    score += 10 + gameMap.currentMapLevel * 5
                    enemiesTotalKilled++
                    enemyIterator.remove()
                    hit = true
                }
            }

            if (hit) bulletIterator.remove()
        }

        // Tir automatique
        if (gameTime % 12 == 0) {
            gameMap.addBullet(player.shoot())
            bulletsShot++
        }

        enemyCount = gameMap.enemies.size
    }

    private fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)

        gameMap.drawBackground(canvas)

        // SCORE
        val scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            isFakeBoldText = true
        }
        canvas.drawText("⭐ $score", 30f, 60f, scorePaint)

        // INFO
        val infoPaint = Paint().apply {
            color = Color.CYAN
            textSize = 36f
        }
        canvas.drawText(
            "${gameMap.getMapName()} | ${gameTime / 60}s | Ennemis: $enemyCount",
            30f,
            height - 40f,
            infoPaint
        )

        player.draw(canvas)

        for (enemy in gameMap.enemies) {
            enemy.draw(canvas)
        }

        for (bullet in gameMap.bullets) {
            bullet.draw(canvas)
        }

        if (gameOver) {
            drawGameOver(canvas)
        }
    }

    private fun drawGameOver(canvas: Canvas) {
        canvas.drawColor(Color.argb(160, 0, 0, 0))

        val paint = Paint().apply {
            color = Color.RED
            textSize = 100f
            isFakeBoldText = true
        }

        canvas.drawText("GAME OVER", 80f, height / 2f, paint)

        val scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
        }

        canvas.drawText(
            "Score: $score",
            100f,
            height / 2f + 120f,
            scorePaint
        )
    }

    private fun triggerGameOver() {
        if (gameOver) return

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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            player.moveTo(it.x)
        }
        return true
    }

    fun pause() {
        gamePaused = true
    }

    fun resume() {
        gamePaused = false
    }
    }
