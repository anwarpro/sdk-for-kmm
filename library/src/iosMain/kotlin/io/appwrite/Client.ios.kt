package io.appwrite

import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.UploadProgress
import kotlinx.cinterop.BetaInteropApi
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass

/**
 * Upload a file in chunks
 *
 * @param path
 * @param headers
 * @param params
 *
 * @return [T]
 */
@OptIn(BetaInteropApi::class)
@Throws(AppwriteException::class, CancellationException::class)
actual suspend fun <T : Any> Client.chunkedUpload(
    path: String,
    headers: MutableMap<String, String>,
    params: MutableMap<String, Any?>,
    responseType: KClass<T>,
    converter: ((Any) -> T),
    paramName: String,
    idParamName: String?,
    onProgress: ((UploadProgress) -> Unit)?,
): T {
    var result: Map<*, *>? = null
    return converter(result as Map<String, Any>)
}