package com.aircraftwar.pro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var playBtn: Button
    private lateinit var leaderboardBtn: Button
    private lateinit var competitionBtn: Button
    private lateinit var settingsBtn: Button
    private lateinit var usernameText: TextView
    private lateinit var levelText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        initializeViews()
        setupUserInfo()
        setupButtonListeners()
    }

    private fun initializeViews() {
        playBtn = findViewById(R.id.play_btn)
        leaderboardBtn = findViewById(R.id.leaderboard_btn)
        competitionBtn = findViewById(R.id.competition_btn)
        settingsBtn = findViewById(R.id.settings_btn)
        usernameText = findViewById(R.id.username_text)
        levelText = findViewById(R.id.level_text)
    }

    private fun setupUserInfo() {
        val prefs = getSharedPreferences("AircraftWarPro", MODE_PRIVATE)
        val username = prefs.getString("username", "Pilote") ?: "Pilote"
        
        usernameText.text = "Bienvenue, $username"
        levelText.text = "Niveau: Novice"
    }

    private fun setupButtonListeners() {
        playBtn.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        leaderboardBtn.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        competitionBtn.setOnClickListener {
            startActivity(Intent(this, CompetitionActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        settingsBtn.setOnClickListener {
            // TODO: Paramètres
        }
    }
}
