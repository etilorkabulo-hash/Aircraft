package com.aircraftwar.pro

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var gameView: GameView
    private var lastBackPressTime = 0L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuration plein écran immersive
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            
            decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // Bloquer l'orientation en portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        gameView = GameView(this)
        setContentView(gameView)
        
        // Afficher un message de bienvenue
        gameView.showWelcomeMessage()
    }
    
    override fun onPause() {
        super.onPause()
        gameView.pauseGame()
    }
    
    override fun onResume() {
        super.onResume()
        gameView.resumeGame()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            gameView.requestFocus()
        }
    }
    
    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        
        when {
            gameView.isGameInProgress() -> {
                gameView.pauseGame()
            }
            currentTime - lastBackPressTime < 2000 -> {
                super.onBackPressed()
            }
            else -> {
                lastBackPressTime = currentTime
                gameView.showToastMessage("Press again to exit")
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                // Bloquer les touches volume
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        gameView.destroy()
    }
}
