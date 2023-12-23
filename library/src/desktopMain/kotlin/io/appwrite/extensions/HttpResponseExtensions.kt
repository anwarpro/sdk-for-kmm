package io.appwrite.extensions

import co.touchlab.kermit.Logger
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

actual suspend fun <T : Any> HttpResponse.parseResponse(
    responseType: KClass<T>?,
    converter: ((Any) -> T)?
): T {
    val json = Json { ignoreUnknownKeys = true }

    when {
        responseType == ByteArray::class -> {
            return converter?.invoke(body()) ?: body<ByteArray>() as T
        }

        else -> {
            if (bodyAsText().isEmpty()) return "" as T
            
            val result = json.parseToJsonElement(bodyAsText())
            Logger.withTag("Response body").d { result.toString() }
            return converter?.invoke(result.jsonElementToMap()) ?: result as T
        }
    }
}