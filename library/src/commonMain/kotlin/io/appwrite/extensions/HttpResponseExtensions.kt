package io.appwrite.extensions

import io.ktor.client.statement.HttpResponse
import kotlin.reflect.KClass

expect suspend fun <T : Any> HttpResponse.parseResponse(
    responseType: KClass<T>?,
    converter: ((Any) -> T)?
): T