package com.aircraftwar.pro

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiService {

    private val client = OkHttpClient()

    private val baseUrl = "https://aircraft-war-server.onrender.com"

    /**
     * 🔐 REGISTER
     */
    fun register(name: String, callback: (String) -> Unit) {

        val json = JSONObject()
        json.put("name", name)

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/register")
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
                    val id = jsonResponse.getString("id")

                    callback(id)
                }

                response.close()
            }
        })
    }

    /**
     * 🔐 LOGIN
     */
    fun login(name: String, callback: (String) -> Unit) {

        val json = JSONObject()
        json.put("name", name)

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/login")
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
                    val id = jsonResponse.getString("id")

                    callback(id)
                }

                response.close()
            }
        })
    }

    /**
     * 🏆 SCORE
     */
    fun sendScore(id: String, score: Int) {

        val json = JSONObject()
        json.put("id", id)
        json.put("score", score)

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/score")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }

    /**
     * 📊 LEADERBOARD
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
                response.close()

                callback(result ?: "[]")
            }
        })
    }
}
