package io.appwrite.models

import java.io.File
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths

actual class InputFile private actual constructor() {
    actual var path: String? = null
    actual var filename: String? = null
    actual var mimeType: String? = null
    actual var sourceType: String? = null
    actual var data: Any? = null

    companion object {
        fun fromFile(file: File) = InputFile().apply {
            path = file.canonicalPath
            filename = file.name
            mimeType = Files.probeContentType(Paths.get(file.canonicalPath))
                ?: URLConnection.guessContentTypeFromName(filename)
                        ?: ""
            sourceType = "file"
        }

        fun fromPath(path: String): InputFile = fromFile(File(path)).apply {
            sourceType = "path"
        }

        fun fromBytes(bytes: ByteArray, filename: String = "", mimeType: String = "") =
            InputFile().apply {
                this.filename = filename
                this.mimeType = mimeType
                data = bytes
                sourceType = "bytes"
            }
    }
}