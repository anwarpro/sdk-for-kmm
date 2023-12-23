package io.appwrite

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class AndroidPlatform : Platform {
    override val name: PlatformName = PlatformName.Android
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(OkHttp) {
    config(this)

    install(WebSockets) {
        this@HttpClient.engine {
            preconfigured = OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .build()
        }
    }

    engine {
        config {
            retryOnConnectionFailure(true)
            connectTimeout(0, TimeUnit.SECONDS)
            trustCerts()
        }
    }
}

private fun OkHttpClient.Builder.trustCerts(selfSigned: Boolean = false) {
    if (selfSigned) {
        val trustAllCerts =
            @Suppress("CustomX509TrustManager")
            object : X509TrustManager {
                @Suppress("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                @Suppress("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }

        val sslContext = SSLContext.getInstance("SSL")
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        sslSocketFactory(sslSocketFactory, trustAllCerts)
    }
}