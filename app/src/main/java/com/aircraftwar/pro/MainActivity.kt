package com.aircraftwar.pro

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val api = ApiService()
    private var playerId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // NOM TEMPORAIRE (plus tard tu ajoutes un EditText)
        val name = "Player_" + System.currentTimeMillis()

        // 1. Créer joueur sur serveur
        api.createPlayer(name) { id ->

            runOnUiThread {

                playerId = id

                // 2. Créer GameView
                val gameView = GameView(this)

                // 3. Passer l’ID au jeu
                gameView.playerId = playerId
                gameView.api = api

                setContentView(gameView)
            }
        }
    }
}
