package io.appwrite

import io.appwrite.models.Session
import io.appwrite.services.Account

object Appwrite {
    lateinit var client: Client
    lateinit var account: Account

    init {
        client = Client()
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("64d715cd9e92b3bd6d29")

        account = Account(client)
    }

    suspend fun onLogin(
        email: String,
        password: String,
    ): Session {
        return account.createEmailSession(
            email,
            password,
        )
    }

    suspend fun onLogout() {
        account.deleteSession("current")
    }
}