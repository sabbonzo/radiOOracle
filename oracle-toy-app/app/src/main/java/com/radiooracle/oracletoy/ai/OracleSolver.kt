package com.radiooracle.oracletoy.ai

import android.util.Base64
import com.radiooracle.oracletoy.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Verdict returned by the solver for a captured quiz frame. */
data class OracleVerdict(
    val answer: String,
    val correct: Boolean?,
    val confidence: Double,   // 0.0 .. 1.0
)

/**
 * Sends a captured camera frame to the Claude Messages API (vision) and asks it
 * to solve the quiz shown in the image, returning a small JSON verdict.
 *
 * The API key comes from BuildConfig (injected from local.properties, never
 * committed). Shipping a key inside an APK is acceptable for a personal build;
 * for anything wider, put a thin backend proxy between the app and the API.
 */
class OracleSolver(
    private val apiKey: String = BuildConfig.CLAUDE_API_KEY,
) {
    private val http = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun solve(jpeg: ByteArray): OracleVerdict = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "CLAUDE_API_KEY non configurata (local.properties)" }

        val imageB64 = Base64.encodeToString(jpeg, Base64.NO_WRAP)
        val body = buildRequestJson(imageB64).toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(body)
            .build()

        http.newCall(request).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("Solver HTTP ${resp.code}: $raw")
            parseVerdict(raw)
        }
    }

    private fun buildRequestJson(imageB64: String): JSONObject {
        val imageBlock = JSONObject()
            .put("type", "image")
            .put(
                "source",
                JSONObject()
                    .put("type", "base64")
                    .put("media_type", "image/jpeg")
                    .put("data", imageB64),
            )
        val textBlock = JSONObject()
            .put("type", "text")
            .put(
                "text",
                "Risolvi il quiz/enigma mostrato nell'immagine. " +
                    "Rispondi SOLO con un oggetto JSON di forma " +
                    "{\"answer\": string, \"correct\": boolean|null, \"confidence\": number 0..1}. " +
                    "Nessun altro testo.",
            )
        val content = JSONArray().put(imageBlock).put(textBlock)
        val message = JSONObject().put("role", "user").put("content", content)

        // Structured output so the first text block is guaranteed valid JSON.
        val schema = JSONObject()
            .put("type", "object")
            .put(
                "properties",
                JSONObject()
                    .put("answer", JSONObject().put("type", "string"))
                    .put("correct", JSONObject().put("type", arrayOfNull()))
                    .put("confidence", JSONObject().put("type", "number")),
            )
            .put("required", JSONArray().put("answer").put("confidence"))
            .put("additionalProperties", false)

        return JSONObject()
            .put("model", "claude-opus-4-8")
            .put("max_tokens", 512)
            .put(
                "output_config",
                JSONObject().put(
                    "format",
                    JSONObject().put("type", "json_schema").put("schema", schema),
                ),
            )
            .put("messages", JSONArray().put(message))
    }

    private fun arrayOfNull(): JSONArray = JSONArray().put("boolean").put("null")

    private fun parseVerdict(responseJson: String): OracleVerdict {
        val root = JSONObject(responseJson)
        val content = root.getJSONArray("content")
        // Find the first text block; with structured output it holds valid JSON.
        var text: String? = null
        for (i in 0 until content.length()) {
            val block = content.getJSONObject(i)
            if (block.optString("type") == "text") {
                text = block.getString("text")
                break
            }
        }
        val parsed = JSONObject(text ?: error("Risposta senza testo"))
        return OracleVerdict(
            answer = parsed.optString("answer"),
            correct = if (parsed.isNull("correct")) null else parsed.optBoolean("correct"),
            confidence = parsed.optDouble("confidence", 0.0),
        )
    }
}
