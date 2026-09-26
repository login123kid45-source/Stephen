package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(
        systemPrompt: String,
        conversationHistory: List<Pair<String, String>>, // role ("user" / "model"), content
        userMessage: String,
        customApiKey: String = "",
        enableWebSearch: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = when {
            customApiKey.isNotBlank() -> customApiKey.trim()
            try { BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" } catch (e: Exception) { false } -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No valid Gemini API key configured. Using local Kara intelligence engine."))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            // Helper to execute request with or without tools
            fun executeCall(withSearch: Boolean): Pair<Int, String> {
                val jsonRoot = JSONObject()

                // System instruction
                val systemObj = JSONObject()
                val systemParts = JSONArray().put(JSONObject().put("text", systemPrompt))
                systemObj.put("parts", systemParts)
                jsonRoot.put("systemInstruction", systemObj)

                // Tools for web search grounding
                if (withSearch) {
                    val toolsArray = JSONArray()
                    val searchTool = JSONObject().put("google_search", JSONObject())
                    toolsArray.put(searchTool)
                    jsonRoot.put("tools", toolsArray)
                }

                // Contents array
                val contentsArray = JSONArray()
                for ((role, text) in conversationHistory.takeLast(6)) {
                    val turn = JSONObject()
                    turn.put("role", if (role == "user") "user" else "model")
                    turn.put("parts", JSONArray().put(JSONObject().put("text", text)))
                    contentsArray.put(turn)
                }
                val currentTurn = JSONObject()
                currentTurn.put("role", "user")
                currentTurn.put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
                contentsArray.put(currentTurn)

                jsonRoot.put("contents", contentsArray)

                val genConfig = JSONObject()
                genConfig.put("temperature", 0.95)
                genConfig.put("maxOutputTokens", 800)
                jsonRoot.put("generationConfig", genConfig)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonRoot.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()
                return Pair(response.code, responseBody)
            }

            var (code, body) = if (enableWebSearch) {
                val callResult = executeCall(withSearch = true)
                // If 400 with search tools, retry without tools
                if (callResult.first != 200) {
                    executeCall(withSearch = false)
                } else {
                    callResult
                }
            } else {
                executeCall(withSearch = false)
            }

            if (code != 200) {
                Log.e("GeminiService", "API error: $code $body")
                return@withContext Result.failure(Exception("Gemini HTTP $code: $body"))
            }

            val respJson = JSONObject(body)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    var reply = parts.getJSONObject(0).optString("text", "").trim()

                    // Extract web grounding search metadata if present
                    val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
                    val searchQueries = groundingMetadata?.optJSONArray("webSearchQueries")
                    if (searchQueries != null && searchQueries.length() > 0) {
                        val queryList = mutableListOf<String>()
                        for (i in 0 until searchQueries.length()) {
                            val q = searchQueries.optString(i)
                            if (q.isNotBlank()) queryList.add(q)
                        }
                        if (queryList.isNotEmpty()) {
                            reply += "\n\n🌐 *Web Sources: ${queryList.take(2).joinToString(", ")}*"
                        }
                    }

                    if (reply.isNotBlank()) {
                        return@withContext Result.success(reply)
                    }
                }
            }

            Result.failure(Exception("Empty candidate returned from Gemini"))
        } catch (e: Exception) {
            Log.e("GeminiService", "Exception in generateResponse", e)
            Result.failure(e)
        }
    }
}
