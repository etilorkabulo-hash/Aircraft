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

    suspend fun register(
        username: String
    ): AuthResponse? {

        return try {

            val cleanUsername =
                username.trim()

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

            val response =
                client.newCall(request).execute()

            val bodyString =
                response.body?.string()

            Log.d(
                "REGISTER_CODE",
                response.code.toString()
            )

            Log.d(
                "REGISTER_BODY",
                bodyString ?: "null"
            )

            if (response.isSuccessful) {

                if (bodyString == null) {

                    throw Exception(
                        "Réponse vide du serveur"
                    )
                }

                val jsonResponse =
                    JSONObject(bodyString)

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

            e.printStackTrace()

            throw Exception(
                e.message ?: "Erreur inconnue"
            )
        }
    }

    suspend fun login(
        username: String
    ): AuthResponse? {

        return register(username)
    }

    // =========================
    // SCORE
    // =========================

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
                        "application/json"
                            .toMediaType()
                    )
                )
                .build()

            val response =
                client.newCall(request).execute()

            val bodyString =
                response.body?.string()

            Log.d(
                "SCORE_CODE",
                response.code.toString()
            )

            Log.d(
                "SCORE_BODY",
                bodyString ?: "null"
            )

            response.isSuccessful

        } catch (e: Exception) {

            e.printStackTrace()
            false
        }
    }

    // =========================
    // LEADERBOARD
    // =========================

    suspend fun getLeaderboard():
            List<Map<String, Any>>? {

        return try {

            val request = Request.Builder()
                .url("$baseUrl/leaderboard")
                .get()
                .build()

            val response =
                client.newCall(request).execute()

            val bodyString =
                response.body?.string()

            Log.d(
                "LEADERBOARD_CODE",
                response.code.toString()
            )

            Log.d(
                "LEADERBOARD_BODY",
                bodyString ?: "null"
            )

            if (
                response.isSuccessful &&
                bodyString != null
            ) {

                val jsonArray =
                    JSONObject(bodyString)
                        .getJSONArray("leaderboard")

                val leaderboard =
                    mutableListOf<Map<String, Any>>()

                for (i in 0 until jsonArray.length()) {

                    val item =
                        jsonArray.getJSONObject(i)

                    leaderboard.add(
                        mapOf(
                            "playerId" to
                                    item.getString("playerId"),

                            "username" to
                                    item.getString("username"),

                            "score" to
                                    item.getInt("score")
                        )
                    )
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

    // =========================
    // MATCHMAKING
    // =========================

    suspend fun joinMatch(
        playerId: String
    ): String? {

        return try {

            val json = JSONObject().apply {
                put("playerId", playerId)
            }

            val request = Request.Builder()
                .url("$baseUrl/match/join")
                .post(
                    json.toString().toRequestBody(
                        "application/json"
                            .toMediaType()
                    )
                )
                .build()

            val response =
                client.newCall(request).execute()

            val bodyString =
                response.body?.string()

            Log.d(
                "MATCH_CODE",
                response.code.toString()
            )

            Log.d(
                "MATCH_BODY",
                bodyString ?: "null"
            )

            if (
                response.isSuccessful &&
                bodyString != null
            ) {

                JSONObject(bodyString)
                    .getString("matchId")

            } else {

                null
            }

        } catch (e: Exception) {

            e.printStackTrace()
            null
        }
    }

    suspend fun sendMatchScore(
        matchId: String,
        playerId: String,
        score: Int
    ): Boolean {

        return try {

            val json = JSONObject().apply {

                put("matchId", matchId)
                put("playerId", playerId)
                put("score", score)
            }

            val request = Request.Builder()
                .url("$baseUrl/match/score")
                .post(
                    json.toString().toRequestBody(
                        "application/json"
                            .toMediaType()
                    )
                )
                .build()

            val response =
                client.newCall(request).execute()

            val bodyString =
                response.body?.string()

            Log.d(
                "MATCH_SCORE_CODE",
                response.code.toString()
            )

            Log.d(
                "MATCH_SCORE_BODY",
                bodyString ?: "null"
            )

            response.isSuccessful

        } catch (e: Exception) {

            e.printStackTrace()
            false
        }
    }

    suspend fun getMatchResult(
        matchId: String
    ): Map<String, Any>? {

        return try {

            val request = Request.Builder()
                .url("$baseUrl/match/result/$matchId")
                .get()
                .build()

            val response =
                client.newCall(request).execute()

            val bodyString =
                response.body?.string()

            Log.d(
                "MATCH_RESULT_CODE",
                response.code.toString()
            )

            Log.d(
                "MATCH_RESULT_BODY",
                bodyString ?: "null"
            )

            if (
                response.isSuccessful &&
                bodyString != null
            ) {

                val jsonResponse =
                    JSONObject(bodyString)

                mapOf(

                    "matchId" to
                            jsonResponse.getString("matchId"),

                    "winner" to
                            jsonResponse.getString("winner"),

                    "loser" to
                            jsonResponse.getString("loser"),

                    "winnerScore" to
                            jsonResponse.getInt("winnerScore"),

                    "loserScore" to
                            jsonResponse.getInt("loserScore")
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
