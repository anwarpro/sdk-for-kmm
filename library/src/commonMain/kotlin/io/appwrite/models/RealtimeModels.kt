package io.appwrite.models

import io.appwrite.json.AnyValueSerializer
import io.ktor.utils.io.core.Closeable
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

data class RealtimeSubscription(
    private val close: () -> Unit
) : Closeable {
    override fun close() = close.invoke()
}

data class RealtimeCallback(
    val channels: Collection<String>,
    val payloadClass: KClass<*>,
    val callback: (RealtimeResponseEvent<*>) -> Unit
)

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = AnyValueSerializer::class)
open class RealtimeResponse(
    val type: String,
    val data: Any
)

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = AnyValueSerializer::class)
data class RealtimeResponseEvent<T>(
    val events: Collection<String>,
    val channels: Collection<String>,
    val timestamp: String,
    var payload: T
)

enum class RealtimeCode(val value: Int) {
    POLICY_VIOLATION(1008),
    UNKNOWN_ERROR(-1)
}