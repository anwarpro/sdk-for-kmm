package io.appwrite.extensions

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun JsonElement.jsonElementToMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    if (this is JsonObject) {
        for ((key, value) in this.entries) {
            map[key] = when (value) {
                is JsonPrimitive -> {
                    when {
                        value.isString -> value.content
                        value.content == "true" || value.content == "false" -> value.content.toBoolean()
                        else -> {
                            when (val number = parseNumber(value.content)) {
                                is Int -> number
                                is Long -> number
                                is Float -> number
                                is Double -> number
                                else -> ""
                            }
                        }
                    }
                }

                is JsonObject -> value.jsonElementToMap()
                is JsonArray -> value.map { it.jsonElementToMap() }
                else -> value
            }
        }
    }

    return map
}

fun parseNumber(str: String): Number? {
    return try {
        str.toInt()
    } catch (e1: NumberFormatException) {
        try {
            str.toFloat()
        } catch (e2: NumberFormatException) {
            try {
                str.toDouble()
            } catch (e3: NumberFormatException) {
                try {
                    str.toLong()
                } catch (e4: NumberFormatException) {
                    // If none of the types match, you can handle the error or return a default value.
                    null
                }
            }
        }
    }
}