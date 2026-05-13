package com.aircraftwar.pro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CompetitionActivity : AppCompatActivity() {

    private lateinit var joinBtn: Button
    private lateinit var statusText: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var apiService: ApiService
    private var playerId = ""
    private var matchId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition)

        joinBtn = findViewById(R.id.join_btn)
        statusText = findViewById(R.id.status_text)
        loadingBar = findViewById(R.id.loading_bar)
        apiService = ApiService()

        val prefs = getSharedPreferences("AircraftWarPro", MODE_PRIVATE)
        playerId = prefs.getString("playerId", "") ?: ""

        joinBtn.setOnClickListener { joinMatch() }
    }

    private fun joinMatch() {
        joinBtn.isEnabled = false
        loadingBar.visibility = android.view.View.VISIBLE
        statusText.text = "🔍 Recherche d'un adversaire..."

        GlobalScope.launch(Dispatchers.Main) {
            try {
                matchId = apiService.joinMatch(playerId) ?: ""
                if (matchId.isNotEmpty()) {
                    statusText.text = "⚔️ Match trouvé! Préparation..."
                    Thread.sleep(2000)
                    startActivity(Intent(this@CompetitionActivity, GameActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@CompetitionActivity, "Erreur de matchmaking", Toast.LENGTH_SHORT).show()
                    joinBtn.isEnabled = true
                    loadingBar.visibility = android.view.View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@CompetitionActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                joinBtn.isEnabled = true
                loadingBar.visibility = android.view.View.GONE
            }
        }
    }
}
