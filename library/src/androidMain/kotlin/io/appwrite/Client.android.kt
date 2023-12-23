package io.appwrite

import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.InputFile
import io.appwrite.models.UploadProgress
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.RandomAccessFile
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

    if (size < Client.CHUNK_SIZE) {
        val data = when (input.sourceType) {
            "file", "path" -> File(input.path).asRequestBody()
            "bytes" -> (input.data as ByteArray).toRequestBody(input.mimeType?.toMediaType())
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

    val buffer = ByteArray(Client.CHUNK_SIZE)
    var offset = 0L
    var result: Map<*, *>? = null

    if (idParamName?.isNotEmpty() == true && params[idParamName] != "unique()") {
        // Make a request to check if a file already exists
        val current = call(
            method = "GET",
            path = "$path/${params[idParamName]}",
            headers = headers,
            params = emptyMap(),
            responseType = Map::class,
        )
        val chunksUploaded = current["chunksUploaded"] as Long
        offset = chunksUploaded * Client.CHUNK_SIZE
    }

    while (offset < size) {
        when (input.sourceType) {
            "file", "path" -> {
                file!!.seek(offset)
                file!!.read(buffer)
            }

            "bytes" -> {
                val end = if (offset + Client.CHUNK_SIZE < size) {
                    offset + Client.CHUNK_SIZE - 1
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
            "bytes $offset-${((offset + Client.CHUNK_SIZE) - 1).coerceAtMost(size - 1)}/$size"

        result = call(
            method = "POST",
            path,
            headers,
            params,
            responseType = Map::class
        )

        offset += Client.CHUNK_SIZE
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