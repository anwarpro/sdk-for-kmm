package io.appwrite

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

interface Platform {
    val name: PlatformName
}

expect fun getPlatform(): Platform

enum class PlatformName {
    Android, IOS, DESKTOP, WEB
}

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient