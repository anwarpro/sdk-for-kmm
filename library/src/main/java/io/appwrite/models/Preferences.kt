package io.appwrite.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Preferences
 */
@Serializable
data class Preferences<T>(
    /**
     * Additional properties
     */
    @SerialName("data")
    val data: T
) {
    fun toMap(): Map<String, Any> = mapOf(
        "data" to data as Map<*, *>
    )

    companion object {
        operator fun invoke(
            data: Map<String, Any>
        ) = Preferences<Map<String, Any>>(
            data
        )

        @Suppress("UNCHECKED_CAST")
        fun <T> from(
            map: Map<String, Any>
        ) = Preferences<T>(
            data = map as T
        )
    }
}