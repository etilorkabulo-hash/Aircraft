package com.aircraftwar.pro

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
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

    // ✅ URL CORRECTE
    private val baseUrl = "http://aircraft-war-serveur.onrender.com/api"

    suspend fun register(username: String): AuthResponse? {
        return try {
            val json = JSONObject().apply {
                put("username", username)
            }

            val request = Request.Builder()
                .url("$baseUrl/player")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val jsonResponse = JSONObject(body)
                AuthResponse(
                    jsonResponse.getString("playerId"),
                    jsonResponse.getString("username")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun login(username: String): AuthResponse? {
        return register(username)
    }

    suspend fun sendScore(playerId: String, score: Int): Boolean {
        return try {
            val json = JSONObject().apply {
                put("playerId", playerId)
                put("score", score)
            }

            val request = Request.Builder()
                .url("$baseUrl/score")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getLeaderboard(): List<Map<String, Any>>? {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/leaderboard")
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
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun joinMatch(playerId: String): String? {
        return try {
            val json = JSONObject().apply {
                put("playerId", playerId)
            }

            val request = Request.Builder()
                .url("$baseUrl/match/join")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                JSONObject(body).getString("matchId")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getMatchResult(matchId: String): Map<String, Any>? {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/match/result/$matchId")
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
            e.printStackTrace()
            null
        }
    }
}
