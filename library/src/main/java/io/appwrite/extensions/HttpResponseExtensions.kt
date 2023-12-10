package io.appwrite.extensions

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

suspend fun <T> HttpResponse.parseResponse(responseType: Class<T>?, converter: ((Any) -> T)?): T {
    val json = Json { ignoreUnknownKeys = true }

    when {
        responseType == ByteArray::class.java -> {
            return converter?.invoke(body()) ?: body<ByteArray>() as T
        }

        else -> {
            if (bodyAsText().isEmpty()) return "" as T

            val result = json.parseToJsonElement(bodyAsText())
            Log.d("Response body", result.toString())
            return converter?.invoke(result.jsonElementToMap()) ?: result as T
        }
    }
}