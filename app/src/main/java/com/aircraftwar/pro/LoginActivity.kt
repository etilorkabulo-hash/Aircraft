package com.aircraftwar.pro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*

class LoginActivity : AppCompatActivity() {

    private val api = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        registerBtn.setOnClickListener {

            val name = nameInput.text.toString()

            api.register(name) { id ->

                runOnUiThread {
                    startGame(id)
                }
            }
        }

        loginBtn.setOnClickListener {

            val name = nameInput.text.toString()

            api.login(name) { id ->

                runOnUiThread {
                    startGame(id)
                }
            }
        }
    }

    /**
     * 🎮 Lancer le jeu avec ID joueur
     */
    private fun startGame(playerId: String) {

        val intent = Intent(this, GameActivity::class.java)

        intent.putExtra("playerId", playerId)

        startActivity(intent)
        finish()
    }
}
