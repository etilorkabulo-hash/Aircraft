package com.aircraftwar.pro

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class LeaderboardActivity : AppCompatActivity() {

    private val api = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_leaderboard)

        val textView = findViewById<TextView>(R.id.leaderboardText)

        loadLeaderboard(textView)
    }

    private fun loadLeaderboard(textView: TextView) {

        api.getLeaderboard { result ->

            runOnUiThread {

                val jsonArray = JSONArray(result)
                val builder = StringBuilder()

                builder.append("🏆 CLASSEMENT\n\n")

                for (i in 0 until jsonArray.length()) {

                    val player = jsonArray.getJSONObject(i)

                    val name = player.getString("name")
                    val score = player.getInt("score")

                    builder.append("${i + 1}. $name - $score pts\n")
                }

                textView.text = builder.toString()
            }
        }
    }
}
