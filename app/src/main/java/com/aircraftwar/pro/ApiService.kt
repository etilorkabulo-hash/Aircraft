package com.aircraftwar.pro

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AuthResponse(
    val playerId: String,
    val username: String
)

class ApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl =
        "https://aircraft-war-serveur.onrender.com/api"

    suspend fun register(username: String): AuthResponse? {

        return try {

            val cleanUsername = username.trim()

            if (cleanUsername.length < 3) {
                throw Exception(
                    "Le pseudo doit contenir au moins 3 caractères"
                )
            }

            val json = JSONObject().apply {
                put("username", cleanUsername)
            }

            val request = Request.Builder()
                .url("$baseUrl/player")
                .post(
                    json.toString().toRequestBody(
                        "application/json; charset=utf-8"
                            .toMediaType()
                    )
                )
                .build()

            val response = client.newCall(request).execute()

            val bodyString = response.body?.string()

            Log.d("API_CODE", response.code.toString())
            Log.d("API_BODY", bodyString ?: "null")

            if (response.isSuccessful) {

                if (bodyString == null) {
                    throw Exception("Réponse vide du serveur")
                }

                val jsonResponse = JSONObject(bodyString)

                AuthResponse(
                    jsonResponse.getString("playerId"),
                    jsonResponse.getString("username")
                )

            } else {

                throw Exception(
                    "Erreur ${response.code} : $bodyString"
                )
            }

        } catch (e: Exception) {

            Log.e("REGISTER_ERROR", e.message ?: "Erreur inconnue")

            throw Exception(
                e.message ?: "Erreur inconnue"
            )
        }
    }

    suspend fun login(username: String): AuthResponse? {
        return register(username)
    }

    suspend fun sendScore(
        playerId: String,
        score: Int
    ): Boolean {

        return try {

            val json = JSONObject().apply {
                put("playerId", playerId)
                put("score", score)
            }

            val request = Request.Builder()
                .url("$baseUrl/score")
                .post(
                    json.toString().toRequestBody(
                        "application/json".toMediaType()
                    )
                )
                .build()

            val response = client.newCall(request).execute()

            val bodyString = response.body?.string()

            Log.d("SCORE_CODE", response.code.toString())
            Log.d("SCORE_BODY", bodyString ?: "null")

            response.isSuccessful

        } catch (e: Exception) {

            e.printStackTrace()
            false
        }
    }
}
