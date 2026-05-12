package com.aircraftwar.pro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MenuActivity : AppCompatActivity() {

    private lateinit var playerId: String
    private val api = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_menu)

        playerId = intent.getStringExtra("playerId") ?: ""

        val playBtn = findViewById<Button>(R.id.playBtn)
        val leaderboardBtn = findViewById<Button>(R.id.leaderboardBtn)
        val competitionBtn = findViewById<Button>(R.id.competitionBtn)
        val settingsBtn = findViewById<Button>(R.id.settingsBtn)

        // 🎮 JOUER
        playBtn.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("playerId", playerId)
            startActivity(intent)
        }

        // 🏆 CLASSEMENT
        leaderboardBtn.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            intent.putExtra("playerId", playerId)
            startActivity(intent)
        }

        // ⚔️ COMPÉTITION
        competitionBtn.setOnClickListener {
            val intent = Intent(this, CompetitionActivity::class.java)
            intent.putExtra("playerId", playerId)
            startActivity(intent)
        }

        // ⚙️ PARAMÈTRES
        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
