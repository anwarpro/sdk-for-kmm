package io.appwrite

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

class IOSPlatform : Platform {
    override val name = PlatformName.IOS
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Darwin) {
    config(this)
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}