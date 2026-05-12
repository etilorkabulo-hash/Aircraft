package com.aircraftwar.pro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val gameMap = GameMap(context)
    private lateinit var gameThread: GameThread

    // 🔗 SERVEUR
    lateinit var api: ApiService
    var playerId: Long = -1

    // 🏆 SCORE LOCAL
    private var score: Int = 0

    @Volatile
    private var isRunning = false

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    /**
     * Initialise connexion serveur depuis MainActivity
     */
    fun init(apiService: ApiService, id: Long) {
        api = apiService
        playerId = id
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        gameThread = GameThread(holder)
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false

        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun render(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        gameMap.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.action) {
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_DOWN -> {
                gameMap.movePlayer(event.x.toInt(), event.y.toInt())
            }
        }

        return true
    }

    /**
     * À appeler quand joueur perd
     */
    fun gameOver() {

        if (::api.isInitialized && playerId != -1L) {
            api.sendScore(playerId, score)
        }
    }

    /**
     * Thread principal du jeu
     */
    private inner class GameThread(
        private val surfaceHolder: SurfaceHolder
    ) : Thread() {

        override fun run() {

            while (isRunning) {
                var canvas: Canvas? = null

                try {
                    canvas = surfaceHolder.lockCanvas()

                    synchronized(surfaceHolder) {

                        if (canvas != null) {

                            // 🔄 UPDATE GAME
                            gameMap.update()

                            // 🏆 SCORE (exemple simple)
                            score += 1

                            // 🎨 DRAW
                            render(canvas)
                        }
                    }

                } finally {
                    canvas?.let {
                        surfaceHolder.unlockCanvasAndPost(it)
                    }
                }

                try {
                    sleep(16) // ~60 FPS
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
