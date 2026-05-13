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

    // =========================
    // AUTH
    // =========================

    suspend fun register(username: String): AuthResponse? {

        return try {

            val cleanUsername = username.trim()

            Log.d("DEBUG_USERNAME", cleanUsername)

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
            val body = response.body?.string()

            Log.d("REGISTER_CODE", response.code.toString())
            Log.d("REGISTER_BODY", body ?: "null")

            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code} : $body")
            }

            if (body == null) {
                throw Exception("Réponse vide serveur")
            }

            val jsonResponse = JSONObject(body)

            return AuthResponse(
                jsonResponse.getString("playerId"),
                jsonResponse.getString("username")
            )

        } catch (e: Exception) {

            Log.e("REGISTER_ERROR", e.toString())

            throw Exception("REGISTER ERROR: $e")
        }
    }

    suspend fun login(username: String): AuthResponse? {
        return register(username)
    }

    // =========================
    // SCORE
    // =========================

    suspend fun sendScore(playerId: String, score: Int): Boolean {

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

            Log.d("SCORE_CODE", response.code.toString())

            response.isSuccessful

        } catch (e: Exception) {
            Log.e("SCORE_ERROR", e.toString())
            false
        }
    }

    // =========================
    // LEADERBOARD
    // =========================

    suspend fun getLeaderboard(): List<Map<String, Any>>? {

        return try {

            val request = Request.Builder()
                .url("$baseUrl/leaderboard")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            Log.d("LEADERBOARD_CODE", response.code.toString())
            Log.d("LEADERBOARD_BODY", body ?: "null")

            if (!response.isSuccessful || body == null) return null

            val jsonArray =
                JSONObject(body).getJSONArray("leaderboard")

            val list = mutableListOf<Map<String, Any>>()

            for (i in 0 until jsonArray.length()) {

                val item = jsonArray.getJSONObject(i)

                list.add(
                    mapOf(
                        "playerId" to item.getString("playerId"),
                        "username" to item.getString("username"),
                        "score" to item.getInt("score")
                    )
                )
            }

            list

        } catch (e: Exception) {

            Log.e("LEADERBOARD_ERROR", e.toString())
            null
        }
    }

    // =========================
    // MATCH
    // =========================

    suspend fun joinMatch(playerId: String): String? {

        return try {

            val json = JSONObject().apply {
                put("playerId", playerId)
            }

            val request = Request.Builder()
                .url("$baseUrl/match/join")
                .post(
                    json.toString().toRequestBody(
                        "application/json".toMediaType()
                    )
                )
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            Log.d("MATCH_CODE", response.code.toString())
            Log.d("MATCH_BODY", body ?: "null")

            if (!response.isSuccessful || body == null) return null

            JSONObject(body).getString("matchId")

        } catch (e: Exception) {

            Log.e("MATCH_ERROR", e.toString())
            null
        }
    }
}
