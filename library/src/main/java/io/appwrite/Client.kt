package io.appwrite

import android.content.Context
import android.content.pm.PackageManager
import io.appwrite.cookies.stores.CustomCookiesStorage
import io.appwrite.exceptions.AppwriteException
import io.appwrite.extensions.parseResponse
import io.appwrite.json.toJsonObject
import io.appwrite.models.InputFile
import io.appwrite.models.Part
import io.appwrite.models.UploadProgress
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.websocket.WebSockets
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.RandomAccessFile
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext

class Client @JvmOverloads constructor(
    context: Context,
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
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return@lazy pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return@lazy ""
        }
    }

    init {
        defaultHeaders = mutableMapOf(
            "content-type" to "application/json",
            "origin" to "appwrite-android://${context.packageName}",
            "user-agent" to "${context.packageName}/${appVersion}, ${System.getProperty("http.agent")}",
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

        return HttpClient(OkHttp) {
            install(WebSockets) {
                this@HttpClient.engine {
                    preconfigured = OkHttpClient.Builder()
                        .pingInterval(20, TimeUnit.SECONDS)
                        .build()
                }
            }

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

            engine {
                config {
                    trustCerts()
                }
            }
        }
    }

    private fun OkHttpClient.Builder.trustCerts() {
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
    @Throws(AppwriteException::class)
    suspend fun <T> call(
        method: String,
        path: String,
        headers: Map<String, String> = mapOf(),
        params: Map<String, Any?> = mapOf(),
        responseType: Class<T>,
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

    /**
     * Upload a file in chunks
     *
     * @param path
     * @param headers
     * @param params
     *
     * @return [T]
     */
    @Throws(AppwriteException::class)
    suspend fun <T> chunkedUpload(
        path: String,
        headers: MutableMap<String, String>,
        params: MutableMap<String, Any?>,
        responseType: Class<T>,
        converter: ((Any) -> T),
        paramName: String,
        idParamName: String? = null,
        onProgress: ((UploadProgress) -> Unit)? = null,
    ): T {
        var file: RandomAccessFile? = null
        val input = params[paramName] as InputFile
        val size: Long = when (input.sourceType) {
            "path", "file" -> {
                file = RandomAccessFile(input.path, "r")
                file.length()
            }

            "bytes" -> {
                (input.data as ByteArray).size.toLong()
            }

            else -> throw UnsupportedOperationException()
        }

        if (size < CHUNK_SIZE) {
            val data = when (input.sourceType) {
                "file", "path" -> File(input.path).asRequestBody()
                "bytes" -> (input.data as ByteArray).toRequestBody(input.mimeType.toMediaType())
                else -> throw UnsupportedOperationException()
            }
            params[paramName] = MultipartBody.Part.createFormData(
                paramName,
                input.filename,
                data
            )
            return call(
                method = "POST",
                path,
                headers,
                params,
                responseType,
                converter
            )
        }

        val buffer = ByteArray(CHUNK_SIZE)
        var offset = 0L
        var result: Map<*, *>? = null

        if (idParamName?.isNotEmpty() == true && params[idParamName] != "unique()") {
            // Make a request to check if a file already exists
            val current = call(
                method = "GET",
                path = "$path/${params[idParamName]}",
                headers = headers,
                params = emptyMap(),
                responseType = Map::class.java,
            )
            val chunksUploaded = current["chunksUploaded"] as Long
            offset = chunksUploaded * CHUNK_SIZE
        }

        while (offset < size) {
            when (input.sourceType) {
                "file", "path" -> {
                    file!!.seek(offset)
                    file!!.read(buffer)
                }

                "bytes" -> {
                    val end = if (offset + CHUNK_SIZE < size) {
                        offset + CHUNK_SIZE - 1
                    } else {
                        size - 1
                    }
                    (input.data as ByteArray).copyInto(
                        buffer,
                        startIndex = offset.toInt(),
                        endIndex = end.toInt()
                    )
                }

                else -> throw UnsupportedOperationException()
            }

            params[paramName] = MultipartBody.Part.createFormData(
                paramName,
                input.filename,
                buffer.toRequestBody()
            )

            headers["Content-Range"] =
                "bytes $offset-${((offset + CHUNK_SIZE) - 1).coerceAtMost(size - 1)}/$size"

            result = call(
                method = "POST",
                path,
                headers,
                params,
                responseType = Map::class.java
            )

            offset += CHUNK_SIZE
            headers["x-appwrite-id"] = result!!["\$id"].toString()
            onProgress?.invoke(
                UploadProgress(
                    id = result!!["\$id"].toString(),
                    progress = offset.coerceAtMost(size).toDouble() / size * 100,
                    sizeUploaded = offset.coerceAtMost(size),
                    chunksTotal = result!!["chunksTotal"].toString().toInt(),
                    chunksUploaded = result!!["chunksUploaded"].toString().toInt(),
                )
            )
        }

        return converter(result as Map<String, Any>)
    }
}