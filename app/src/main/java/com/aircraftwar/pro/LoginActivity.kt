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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

        initializeViews()
        setupClickListeners()
        apiService = ApiService()
    }

    private fun initializeViews() {
        usernameInput = findViewById(R.id.username_input)
        registerBtn = findViewById(R.id.register_btn)
        loginBtn = findViewById(R.id.login_btn)
        loadingProgressBar = findViewById(R.id.loading_progress)
    }

    private fun setupClickListeners() {
        registerBtn.setOnClickListener { handleRegister() }
        loginBtn.setOnClickListener { handleLogin() }
    }

    private fun handleRegister() {
        val username = usernameInput.text.toString().trim()

        if (!validateInput(username)) {
            Toast.makeText(this, "Veuillez entrer un nom d'utilisateur valide", Toast.LENGTH_SHORT).show()
            return
        }

        performAuthAction(username, isRegister = true)
    }

    private fun handleLogin() {
        val username = usernameInput.text.toString().trim()

        if (!validateInput(username)) {
            Toast.makeText(this, "Veuillez entrer un nom d'utilisateur valide", Toast.LENGTH_SHORT).show()
            return
        }

        performAuthAction(username, isRegister = false)
    }

    private fun validateInput(username: String): Boolean {
        return !TextUtils.isEmpty(username) && username.length >= 3
    }

    private fun performAuthAction(username: String, isRegister: Boolean) {
        showLoading(true)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = apiService.register(username)
                
                if (response != null) {
                    saveUserData(response)
                    showSuccessMessage(isRegister)
                    navigateToMenu()
                } else {
                    showErrorMessage("Erreur de connexion au serveur")
                }
            } catch (e: Exception) {
                showErrorMessage("Erreur: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun saveUserData(response: AuthResponse) {
        val prefs = getSharedPreferences("AircraftWarPro", MODE_PRIVATE)
        prefs.edit().apply {
            putString("playerId", response.playerId)
            putString("username", response.username)
            putLong("lastLoginTime", System.currentTimeMillis())
            apply()
        }
    }

    private fun showSuccessMessage(isRegister: Boolean) {
        val message = if (isRegister) "Inscription réussie!" else "Connexion réussie!"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(isLoading: Boolean) {
        loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        registerBtn.isEnabled = !isLoading
        loginBtn.isEnabled = !isLoading
    }

    private fun navigateToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
