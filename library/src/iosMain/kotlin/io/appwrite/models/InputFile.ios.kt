package io.appwrite.models

actual class InputFile private actual constructor() {
    actual var path: String? = null
    actual var filename: String? = null
    actual var mimeType: String? = null
    actual var sourceType: String? = null
    actual var data: Any? = null

    companion object {
        fun fromFile(file: String) = InputFile().apply {
            sourceType = "file"
        }

        fun fromPath(path: String): InputFile = fromFile(path).apply {
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