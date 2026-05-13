package com.aircraftwar.pro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private lateinit var scoreText: TextView
    private lateinit var statsText: TextView
    private lateinit var backBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultText = findViewById(R.id.result_text)
        scoreText = findViewById(R.id.score_text)
        statsText = findViewById(R.id.stats_text)
        backBtn = findViewById(R.id.back_btn)

        val score = intent.getIntExtra("score", 0)
        val result = intent.getStringExtra("result") ?: "DÉFAITE"
        val gameTime = intent.getIntExtra("gameTime", 0)
        val enemiesTotalKilled = intent.getIntExtra("enemiesTotalKilled", 0)
        val mapLevel = intent.getIntExtra("mapLevel", 0)

        resultText.text = result
        scoreText.text = "🎯 Score: ${String.format("%,d", score)}"
        
        val mapName = when (mapLevel) {
            0 -> "🌌 SPACE"
            1 -> "⚡ NEON"
            2 -> "🔥 FIRE"
            3 -> "⛈️ STORM"
            else -> "UNKNOWN"
        }
        
        statsText.text = "⏱️ Durée: ${gameTime}s | 🎯 Ennemis: $enemiesTotalKilled | 🗺️ Map: $mapName"

        backBtn.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
    }
}
