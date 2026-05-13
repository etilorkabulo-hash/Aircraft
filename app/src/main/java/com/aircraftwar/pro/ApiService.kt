package com.aircraftwar.pro

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

data class AuthResponse(
    val playerId: String,
    val username: String
)

class ApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // ✅ URL CORRECTE (HTTPS)
    private val baseUrl = "https://aircraft-war-serveur.onrender.com/api"

    suspend fun register(username: String): AuthResponse? {
        return try {
            android.util.Log.d("ApiService", "Enregistrement: $username")
            android.util.Log.d("ApiService", "URL: $baseUrl/player")

            val json = JSONObject().apply {
                put("username", username)
            }

            val request = Request.Builder()
                .url("$baseUrl/player")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            
            android.util.Log.d("ApiService", "Status: ${response.code}")

            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                android.util.Log.d("ApiService", "Réponse: $body")
                
                val jsonResponse = JSONObject(body)
                AuthResponse(
                    jsonResponse.getString("playerId"),
                    jsonResponse.getString("username")
                )
            } else {
                android.util.Log.e("ApiService", "Erreur HTTP: ${response.code} - ${response.message}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Exception: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun login(username: String): AuthResponse? {
        return register(username)
    }

    suspend fun sendScore(playerId: String, score: Int): Boolean {
        return try {
            android.util.Log.d("ApiService", "Envoi score: $score")

            val json = JSONObject().apply {
                put("playerId", playerId)
                put("score", score)
            }

            val request = Request.Builder()
                .url("$baseUrl/score")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Erreur sendScore: ${e.message}")
            false
        }
    }

    suspend fun getLeaderboard(): List<Map<String, Any>>? {
        return try {
            android.util.Log.d("ApiService", "Récupération classement")

            val request = Request.Builder()
                .url("$baseUrl/leaderboard")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val jsonArray = JSONObject(body).getJSONArray("leaderboard")
                val leaderboard = mutableListOf<Map<String, Any>>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    leaderboard.add(mapOf(
                        "playerId" to item.getString("playerId"),
                        "username" to item.getString("username"),
                        "score" to item.getInt("score")
                    ))
                }
                leaderboard
            } else {
                android.util.Log.e("ApiService", "Erreur leaderboard: ${response.code}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Erreur getLeaderboard: ${e.message}")
            null
        }
    }

    suspend fun joinMatch(playerId: String): String? {
        return try {
            android.util.Log.d("ApiService", "Matchmaking")

            val json = JSONObject().apply {
                put("playerId", playerId)
            }

            val request = Request.Builder()
                .url("$baseUrl/match/join")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val matchId = JSONObject(body).optString("matchId", null)
                android.util.Log.d("ApiService", "MatchId: $matchId")
                matchId
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Erreur joinMatch: ${e.message}")
            null
        }
    }

    suspend fun sendMatchScore(matchId: String, playerId: String, score: Int): Boolean {
        return try {
            val json = JSONObject().apply {
                put("matchId", matchId)
                put("playerId", playerId)
                put("score", score)
            }

            val request = Request.Builder()
                .url("$baseUrl/match/score")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Erreur sendMatchScore: ${e.message}")
            false
        }
    }

    suspend fun getMatchResult(matchId: String): Map<String, Any>? {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/match/result/$matchId")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val jsonResponse = JSONObject(body)
                mapOf(
                    "matchId" to jsonResponse.getString("matchId"),
                    "winner" to jsonResponse.getString("winner"),
                    "loser" to jsonResponse.getString("loser"),
                    "winnerScore" to jsonResponse.getInt("winnerScore"),
                    "loserScore" to jsonResponse.getInt("loserScore")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Erreur getMatchResult: ${e.message}")
            null
        }
    }
}
