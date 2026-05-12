package com.aircraftwar.pro

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ApiService {

    private val client = OkHttpClient()

    // 🔗 CHANGE ICI avec ton lien Render
    private val baseUrl = "https://aircraft-war-server.onrender.com"

    /**
     * 👤 Créer un joueur
     * Retourne l'ID unique du serveur
     */
    fun createPlayer(name: String, callback: (Long) -> Unit) {

        val json = JSONObject()
        json.put("name", name)

        val body = RequestBody.create(
            MediaType.get("application/json"),
            json.toString()
        )

        val request = Request.Builder()
            .url("$baseUrl/player")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                val result = response.body?.string()

                if (result != null) {

                    val jsonResponse = JSONObject(result)
                    val id = jsonResponse.getLong("id")

                    callback(id)
                }
            }
        })
    }

    /**
     * 🏆 Envoyer score au serveur
     */
    fun sendScore(id: Long, score: Int) {

        val json = JSONObject()
        json.put("id", id)
        json.put("score", score)

        val body = RequestBody.create(
            MediaType.get("application/json"),
            json.toString()
        )

        val request = Request.Builder()
            .url("$baseUrl/score")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // rien à faire ici pour l'instant
            }
        })
    }

    /**
     * 📊 Récupérer leaderboard (optionnel mais utile)
     */
    fun getLeaderboard(callback: (String) -> Unit) {

        val request = Request.Builder()
            .url("$baseUrl/leaderboard")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                val result = response.body?.string()
                callback(result ?: "[]")
            }
        })
    }
}
