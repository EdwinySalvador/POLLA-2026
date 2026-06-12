package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.MatchEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiParserService {
    private const val TAG = "GeminiParserService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    interface GeminiApi {
        @POST("v1beta/models/gemini-3.5-flash:generateContent")
        suspend fun generateContent(
            @Query("key") key: String,
            @Body body: okhttp3.RequestBody
        ): ResponseBody
    }

    private val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    data class ParsedPrediction(
        val matchId: Int,
        val goals1: Int,
        val goals2: Int
    )

    data class GeminiResponse(
        val candidates: List<Candidate>?
    ) {
        data class Candidate(val content: Content?)
        data class Content(val parts: List<Part>?)
        data class Part(val text: String?)
    }

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (originalBitmap == null) return null

            // Resize maintaining aspect ratio (max height/width of 1024)
            val maxDimension = 1024
            val width = originalBitmap.width
            val height = originalBitmap.height
            val resizedBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val (newWidth, newHeight) = if (ratio > 1) {
                    maxDimension to (maxDimension / ratio).toInt()
                } else {
                    (maxDimension * ratio).toInt() to maxDimension
                }
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error matching image URI to Base64", e)
            null
        }
    }

    suspend fun parsePredictionsFromImage(
        context: Context,
        imageUri: Uri,
        matches: List<MatchEntity>
    ): List<ParsedPrediction> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured or placeholder.")
            throw IllegalStateException("API Key de Gemini no configurada. Por favor, añádela en la sección de Secretos de AI Studio.")
        }

        val base64Image = uriToBase64(context, imageUri)
            ?: throw IllegalArgumentException("No se pudo cargar o procesar la imagen seleccionada.")

        // Compact list of matches as lookup table
        val matchesTable = matches.joinToString("\n") { match ->
            "[Partido #${match.id}] ${match.team1} vs ${match.team2} (${match.groupCode})"
        }

        val prompt = """
            Eres un asistente experto en procesar planillas de predicciones (polla de fútbol familiar) para el Mundial 2026.
            Se te proporcionará una foto o imagen de un papel impreso o digital donde un jugador escribió a mano o rellenó los resultados esperados (goles de cada equipo).
            Tu tarea es analizar visualmente la imagen para identificar los equipos de cada partido y extraer sus respectivos goles pronosticados.
            Luego, debes relacionar cada partido visualizado con su ID oficial del listado de abajo.
            
            Debes retornar ÚNICAMENTE un objeto JSON válido con la clave principal "predictions" que sea una lista de objetos, donde cada uno contenga precisamente los campos: "matchId" (Integer), "goals1" (Integer) y "goals2" (Integer).
            NO agregues bloques markdown como ```json ... ```, ni comentarios, ni texto introductorio o final. Solo retorna el JSON plano.

            Es sumamente importante que:
            - Solo incluyas los partidos que logres identificar con claridad razonable.
            - Si un partido no tiene predicción o no se lee bien, NO lo incluyas en la lista.
            - "goals1" es para el Equipo 1 (izquierda) y "goals2" es para el Equipo 2 (derecha).

            Lista oficial de los partidos con sus IDs:
            $matchesTable
        """.trimIndent()

        val jsonRequest = """
            {
              "contents": [
                {
                  "parts": [
                    { "text": "${escapeJson(prompt)}" },
                    {
                      "inlineData": {
                        "mimeType": "image/jpeg",
                        "data": "$base64Image"
                      }
                    }
                  ]
                }
              ],
              "generationConfig": {
                "responseMimeType": "application/json"
              }
            }
        """.trimIndent()

        val requestBody = jsonRequest.toRequestBody("application/json".toMediaType())
        val responseBody = api.generateContent(apiKey, requestBody)
        val responseString = responseBody.string()

        // Parse with Moshi
        val adapter = moshi.adapter(GeminiResponse::class.java)
        val result = adapter.fromJson(responseString)

        val rawText = result?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Gemini no retornó contenido de texto útil.")

        // Clean potentially returned markdown block if Gemini didn't strictly follow format
        val jsonText = rawText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        // Parse the inner predictions list
        val predictionsAdapter = moshi.adapter(ParsedPredictionsContainer::class.java)
        val parsedContainer = predictionsAdapter.fromJson(jsonText)
            ?: throw IllegalStateException("La respuesta de la IA no tiene el formato de predicciones requerido.")

        return parsedContainer.predictions
    }

    // Helper class for parsing result list
    data class ParsedPredictionsContainer(
        val predictions: List<ParsedPrediction>
    )
}
