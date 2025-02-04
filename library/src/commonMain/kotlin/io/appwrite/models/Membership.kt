package io.appwrite.models

import io.appwrite.json.ListAnyValueSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Membership
 */
@Serializable
data class Membership(
    /**
     * Membership ID.
     */
    @SerialName("\$id")
    val id: String,

    /**
     * Membership creation date in ISO 8601 format.
     */
    @SerialName("\$createdAt")
    val createdAt: String,

    /**
     * Membership update date in ISO 8601 format.
     */
    @SerialName("\$updatedAt")
    val updatedAt: String,

    /**
     * User ID.
     */
    @SerialName("userId")
    val userId: String,

    /**
     * User name.
     */
    @SerialName("userName")
    val userName: String,

    /**
     * User email address.
     */
    @SerialName("userEmail")
    val userEmail: String,

    /**
     * Team ID.
     */
    @SerialName("teamId")
    val teamId: String,

    /**
     * Team name.
     */
    @SerialName("teamName")
    val teamName: String,

    /**
     * Date, the user has been invited to join the team in ISO 8601 format.
     */
    @SerialName("invited")
    val invited: String,

    /**
     * Date, the user has accepted the invitation to join the team in ISO 8601 format.
     */
    @SerialName("joined")
    val joined: String,

    /**
     * User confirmation status, true if the user has joined the team or false otherwise.
     */
    @SerialName("confirm")
    val confirm: Boolean,

    /**
     * User list of roles
     */
    @Serializable(with = ListAnyValueSerializer::class)
    @SerialName("roles")
    val roles: List<@Contextual Any>,

    ) {
    fun toMap(): Map<String, Any> = mapOf(
        "\$id" to id as Any,
        "\$createdAt" to createdAt as Any,
        "\$updatedAt" to updatedAt as Any,
        "userId" to userId as Any,
        "userName" to userName as Any,
        "userEmail" to userEmail as Any,
        "teamId" to teamId as Any,
        "teamName" to teamName as Any,
        "invited" to invited as Any,
        "joined" to joined as Any,
        "confirm" to confirm as Any,
        "roles" to roles as Any,
    )

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun from(
            map: Map<String, Any>,
        ) = Membership(
            id = map["\$id"] as String,
            createdAt = map["\$createdAt"] as String,
            updatedAt = map["\$updatedAt"] as String,
            userId = map["userId"] as String,
            userName = map["userName"] as String,
            userEmail = map["userEmail"] as String,
            teamId = map["teamId"] as String,
            teamName = map["teamName"] as String,
            invited = map["invited"] as String,
            joined = map["joined"] as String,
            confirm = map["confirm"] as Boolean,
            roles = map["roles"] as List<Any>,
        )
    }
}