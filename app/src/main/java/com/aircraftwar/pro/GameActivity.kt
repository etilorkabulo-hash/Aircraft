package com.aircraftwar.pro

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private val api = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // ✅ CORRECTION ICI
        val playerId = intent.getStringExtra("playerId") ?: ""

        val gameView = GameView(this)

        gameView.init(api, playerId)

        setContentView(gameView)
    }
}
