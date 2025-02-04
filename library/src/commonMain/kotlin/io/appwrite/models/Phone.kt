package io.appwrite.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Phone
 */
@Serializable
data class Phone(
    /**
     * Phone code.
     */
    @SerialName("code")
    val code: String,

    /**
     * Country two-character ISO 3166-1 alpha code.
     */
    @SerialName("countryCode")
    val countryCode: String,

    /**
     * Country name.
     */
    @SerialName("countryName")
    val countryName: String,

) {
    fun toMap(): Map<String, Any> = mapOf(
        "code" to code as Any,
        "countryCode" to countryCode as Any,
        "countryName" to countryName as Any,
    )

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun from(
            map: Map<String, Any>,
        ) = Phone(
            code = map["code"] as String,
            countryCode = map["countryCode"] as String,
            countryName = map["countryName"] as String,
        )
    }
}