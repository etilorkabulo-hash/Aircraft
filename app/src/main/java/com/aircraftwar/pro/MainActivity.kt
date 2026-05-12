package com.aircraftwar.pro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val playBtn = findViewById<Button>(R.id.playBtn)

        playBtn.setOnClickListener {

            // 👉 Aller à l'écran login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
