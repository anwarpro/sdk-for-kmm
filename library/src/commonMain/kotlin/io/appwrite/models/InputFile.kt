package io.appwrite.models

expect class InputFile private constructor() {
    var path: String?
    var filename: String?
    var mimeType: String?
    var sourceType: String?
    var data: Any?
}