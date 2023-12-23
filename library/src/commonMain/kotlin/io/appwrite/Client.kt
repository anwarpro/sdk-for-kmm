package io.appwrite

import com.helloanwar.library.MyConfig
import io.appwrite.cookies.stores.CustomCookiesStorage
import io.appwrite.exceptions.AppwriteException
import io.appwrite.extensions.parseResponse
import io.appwrite.json.toJsonObject
import io.appwrite.models.Part
import io.appwrite.models.UploadProgress
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

class Client @JvmOverloads constructor(
    var endPoint: String = "https://HOSTNAME/v1",
    var endPointRealtime: String? = null,
    private var selfSigned: Boolean = false
) : CoroutineScope {

    companion object {
        const val CHUNK_SIZE = 5 * 1024 * 1024; // 5MB
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job = Job()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
        prettyPrint = true
        isLenient = true
        coerceInputValues = true
    }

    lateinit var httpClient: HttpClient

    private val defaultHeaders: MutableMap<String, String>

    val config: MutableMap<String, String>

    private val appVersion by lazy {
        MyConfig.APP_VERSION
    }

    init {
        defaultHeaders = mutableMapOf(
            "content-type" to "application/json",
            "origin" to "appwrite-android://${MyConfig.APP_PACKAGE_NAME}",
            "user-agent" to "${MyConfig.APP_PACKAGE_NAME}/${appVersion}, ",
            "x-sdk-name" to "Android",
            "x-sdk-platform" to "client",
            "x-sdk-language" to "android",
            "x-sdk-version" to "4.0.1",
            "x-appwrite-response-format" to "1.4.0"
        )
        config = mutableMapOf()

        setSelfSigned(selfSigned)
    }

    /**
     * Set Project
     *
     * Your project ID
     *
     * @param {string} project
     *
     * @return this
     */
    fun setProject(value: String): Client {
        config["project"] = value
        addHeader("x-appwrite-project", value)
        return this
    }

    /**
     * Set JWT
     *
     * Your secret JSON Web Token
     *
     * @param {string} jwt
     *
     * @return this
     */
    fun setJWT(value: String): Client {
        config["jWT"] = value
        addHeader("x-appwrite-jwt", value)
        return this
    }

    /**
     * Set Locale
     *
     * @param {string} locale
     *
     * @return this
     */
    fun setLocale(value: String): Client {
        config["locale"] = value
        addHeader("x-appwrite-locale", value)
        return this
    }

    /**
     * Set self Signed
     *
     * @param status
     *
     * @return this
     */
    fun setSelfSigned(status: Boolean): Client {
        selfSigned = status

        if (!selfSigned) {
            httpClient = buildClient()
            return this
        }

        try {
            httpClient = buildClient()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return this
    }

    /**
     * Set endpoint and realtime endpoint.
     *
     * @param endpoint
     *
     * @return this
     */
    fun setEndpoint(endPoint: String): Client {
        this.endPoint = endPoint

        if (this.endPointRealtime == null && endPoint.startsWith("http")) {
            this.endPointRealtime = endPoint.replaceFirst("http", "ws")
        }

        return this
    }

    /**
     * Set realtime endpoint
     *
     * @param endpoint
     *
     * @return this
     */
    fun setEndpointRealtime(endPoint: String): Client {
        this.endPointRealtime = endPoint
        return this
    }

    /**
     * Add Header
     *
     * @param key
     * @param value
     *
     * @return this
     */
    fun addHeader(key: String, value: String): Client {
        defaultHeaders[key] = value
        return this
    }

    private fun buildClient(): HttpClient {
        return httpClient {
            install(DefaultRequest) {
                defaultHeaders.forEach {
                    header(it.key, it.value)
                }
            }

            install(HttpCookies) {
                storage = CustomCookiesStorage
            }

            install(ContentNegotiation) {
                json(json = json)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.v { message }
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    /**
     * Send the HTTP request
     *
     * @param method
     * @param path
     * @param headers
     * @param params
     *
     * @return [T]
     */
    @Throws(AppwriteException::class, CancellationException::class)
    suspend fun <T : Any> call(
        method: String,
        path: String,
        headers: Map<String, String> = mapOf(),
        params: Map<String, Any?> = mapOf(),
        responseType: KClass<T>,
        converter: ((Any) -> T)? = null
    ): T {
        val filteredParams = params.filterValues { it != null }

        val response: HttpResponse = httpClient.request((endPoint + path)) {
            this.method = HttpMethod.parse(method)
            this.contentType(ContentType.parse(headers["content-type"] ?: "application/json"))
            this.headers {
                headers.forEach {
                    if (it.key != "content-type") {
                        header(it.key, it.value)
                    }
                }
            }

            if (HttpMethod.parse(method) == HttpMethod.Get) {
                filteredParams.forEach {
                    if (it.value is List<*>) {
                        val list = it.value as List<*>
                        for (index in list.indices) {
                            parameter("${it.key}[]", it.value.toString())
                        }
                    } else {
                        parameter(it.key, it.value.toString())
                    }
                }
            } else {
                if (ContentType.MultiPart.FormData == ContentType.parse(
                        headers["content-type"] ?: "application/json"
                    )
                ) {
                    val formData = MultiPartFormDataContent(
                        formData {
                            filteredParams.forEach {
                                when {
                                    it.key == "file" -> {
                                        append("file", (it.value as Part).data, Headers.build {
                                            append(
                                                HttpHeaders.ContentDisposition,
                                                "filename=\"${(it.value as Part).fileName}\""
                                            )
                                        })
                                    }

                                    it.value is List<*> -> {
                                        val list = it.value as List<*>
                                        for (index in list.indices) {
                                            append("${it.key}[]", list[index].toString())
                                        }
                                    }

                                    else -> {
                                        append(it.key, it.value.toString())
                                    }
                                }
                            }
                        }
                    )
                    setBody(formData)
                } else {
                    setBody(json.encodeToJsonElement(filteredParams.toJsonObject()))
                }
            }
        }

        if (response.status.isSuccess()) {
            try {
                return response.parseResponse(responseType, converter)
            } catch (e: Exception) {
                throw AppwriteException(
                    message = e.message,
                    code = response.status.value,
                    response = response.bodyAsText()
                )
            }
        } else {
            throw AppwriteException(
                code = response.status.value,
                response = response.bodyAsText()
            )
        }
    }
}

/**
 * Upload a file in chunks
 *
 * @param path
 * @param headers
 * @param params
 *
 * @return [T]
 */
@Throws(AppwriteException::class, CancellationException::class)
expect suspend fun <T : Any> Client.chunkedUpload(
    path: String,
    headers: MutableMap<String, String>,
    params: MutableMap<String, Any?>,
    responseType: KClass<T>,
    converter: ((Any) -> T),
    paramName: String,
    idParamName: String? = null,
    onProgress: ((UploadProgress) -> Unit)? = null,
): T