package com.aircraftwar.pro

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var backBtn: ImageButton
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        listView = findViewById(R.id.leaderboard_list)
        backBtn = findViewById(R.id.back_btn)
        apiService = ApiService()

        backBtn.setOnClickListener {
            finish()
        }

        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val leaderboard = apiService.getLeaderboard()
                if (leaderboard != null) {
                    val data = leaderboard.mapIndexed { index, player ->
                        mapOf(
                            "rank" to "#${index + 1}",
                            "username" to player["username"].toString(),
                            "score" to "⭐ ${String.format("%,d", (player["score"] as? Int) ?: 0)}"
                        )
                    }

                    val adapter = SimpleAdapter(
                        this@LeaderboardActivity,
                        data,
                        R.layout.leaderboard_item,
                        arrayOf("rank", "username", "score"),
                        intArrayOf(R.id.rank_text, R.id.username_text, R.id.score_text)
                    )
                    listView.adapter = adapter
                } else {
                    Toast.makeText(this@LeaderboardActivity, "Erreur de chargement", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LeaderboardActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
