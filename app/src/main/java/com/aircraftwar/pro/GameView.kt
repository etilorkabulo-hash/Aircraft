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
    private var isRunning = true

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread = GameThread(holder, this)
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

    fun draw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        gameMap.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_MOVE -> {
                    gameMap.movePlayer(it.x.toInt(), it.y.toInt())
                }
            }
        }
        return true
    }

    private inner class GameThread(
        private val surfaceHolder: SurfaceHolder,
        private val gameView: GameView
    ) : Thread() {
        override fun run() {
            while (isRunning) {
                val canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    try {
                        gameView.draw(canvas)
                        gameMap.update()
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
                Thread.sleep(16)
            }
        }
    }
}
