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

    // 🔗 SERVEUR (sécurisé)
    private var api: ApiService? = null
    private var playerId: String? = null

    // 🏆 SCORE
    private var score: Int = 0
    private var frameCounter = 0

    @Volatile
    private var isRunning = false

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    /**
     * Initialise connexion serveur
     */
    fun init(apiService: ApiService, id: String) {
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
        gameThread.interrupt()

        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * DRAW
     */
    private fun render(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        gameMap.draw(canvas)
    }

    /**
     * TOUCH INPUT
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        if (event.action == MotionEvent.ACTION_MOVE ||
            event.action == MotionEvent.ACTION_DOWN
        ) {
            gameMap.movePlayer(event.x.toInt(), event.y.toInt())
        }

        return true
    }

    /**
     * GAME OVER
     */
    fun gameOver() {
        val service = api ?: return
        val id = playerId ?: return

        service.sendScore(id, score)
    }

    /**
     * THREAD PRINCIPAL
     */
    private inner class GameThread(
        private val surfaceHolder: SurfaceHolder
    ) : Thread() {

        override fun run() {

            val frameTime = 1000L / 60L

            while (isRunning && !isInterrupted) {

                var canvas: Canvas? = null

                try {
                    canvas = surfaceHolder.lockCanvas() ?: continue

                    synchronized(surfaceHolder) {

                        // 🔄 UPDATE GAME
                        gameMap.update()

                        // 🏆 SCORE (1 point / seconde)
                        frameCounter++
                        if (frameCounter >= 60) {
                            score++
                            frameCounter = 0
                        }

                        // 🎨 DRAW
                        render(canvas)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    canvas?.let {
                        surfaceHolder.unlockCanvasAndPost(it)
                    }
                }

                try {
                    sleep(frameTime)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }
}
