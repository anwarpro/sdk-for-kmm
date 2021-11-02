package io.appwrite.models

/**
 * File
 */
data class File(
    /**
     * File ID.
     *
     */
    val id: String,

    /**
     * File permissions.
     *
     */
    val permissions: Permissions,

    /**
     * File name.
     *
     */
    val name: String,

    /**
     * File creation date in Unix timestamp.
     *
     */
    val dateCreated: Long,

    /**
     * File MD5 signature.
     *
     */
    val signature: String,

    /**
     * File mime type.
     *
     */
    val mimeType: String,

    /**
     * File original size in bytes.
     *
     */
    val sizeOriginal: Long
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun from(map: Map<String, Any>) = File(
            id = map["\$id"] as String,
            permissions = Permissions.from(map = map["\$permissions"] as Map<String, Any>),
            name = map["name"] as String,
            dateCreated = map["dateCreated"] as Long,
            signature = map["signature"] as String,
            mimeType = map["mimeType"] as String,
            sizeOriginal = map["sizeOriginal"] as Long
        )
    }

    fun toMap(): Map<String, Any> = mapOf(
        "\$id" to id as Any,
        "\$permissions" to permissions.toMap() as Any,
        "name" to name as Any,
        "dateCreated" to dateCreated as Any,
        "signature" to signature as Any,
        "mimeType" to mimeType as Any,
        "sizeOriginal" to sizeOriginal as Any
    )
}