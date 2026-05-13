package com.aircraftwar.pro

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var registerBtn: Button
    private lateinit var loginBtn: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        apiService = ApiService()

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {

        usernameInput = findViewById(R.id.username_input)
        registerBtn = findViewById(R.id.register_btn)
        loginBtn = findViewById(R.id.login_btn)
        loadingProgressBar = findViewById(R.id.loading_progress)
    }

    private fun setupClickListeners() {

        registerBtn.setOnClickListener {
            handleAuth(isRegister = true)
        }

        loginBtn.setOnClickListener {
            handleAuth(isRegister = false)
        }
    }

    private fun handleAuth(isRegister: Boolean) {

        val username =
            usernameInput.text.toString().trim()

        if (!validateInput(username)) {

            showErrorMessage(
                "Pseudo invalide (3 à 20 caractères)"
            )

            return
        }

        performAuth(username, isRegister)
    }

    private fun validateInput(username: String): Boolean {

        return !TextUtils.isEmpty(username)
                && username.length >= 3
                && username.length <= 20
    }

    private fun performAuth(
        username: String,
        isRegister: Boolean
    ) {

        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {

            try {

                val response =
                    apiService.register(username) // OK pour l'instant (backend simple)

                if (response != null) {

                    saveUserData(response)

                    showSuccess(isRegister)

                    navigateToMenu()

                } else {

                    showErrorMessage(
                        "Serveur: réponse vide"
                    )
                }

            } catch (e: Exception) {

                e.printStackTrace()

                showErrorMessage(
                    e.message ?: "Erreur inconnue"
                )
            } finally {

                showLoading(false)
            }
        }
    }

    private fun saveUserData(response: AuthResponse) {

        val prefs =
            getSharedPreferences(
                "AircraftWarPro",
                MODE_PRIVATE
            )

        prefs.edit().apply {

            putString("playerId", response.playerId)
            putString("username", response.username)
            putLong("lastLoginTime", System.currentTimeMillis())

            apply()
        }
    }

    private fun showSuccess(isRegister: Boolean) {

        Toast.makeText(
            this,
            if (isRegister)
                "Inscription réussie"
            else
                "Connexion réussie",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showErrorMessage(message: String) {

        Toast.makeText(
            this,
            "ERROR: $message",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading(isLoading: Boolean) {

        loadingProgressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        registerBtn.isEnabled = !isLoading
        loginBtn.isEnabled = !isLoading
    }

    private fun navigateToMenu() {

        startActivity(
            Intent(this, MenuActivity::class.java)
        )

        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        finish()
    }
}
