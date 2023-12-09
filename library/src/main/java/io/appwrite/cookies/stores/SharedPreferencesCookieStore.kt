package io.appwrite.cookies.stores

import android.content.Context
import android.os.Build
import android.util.Log
import io.appwrite.cookies.InternalCookie
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpCookie
import java.net.URI

open class SharedPreferencesCookieStore(
    context: Context,
    private val name: String
) : InMemoryCookieStore(name) {

    private val preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private val gson = Json {
        ignoreUnknownKeys = true
    }

    init {
        synchronized(SharedPreferencesCookieStore::class.java) {
            preferences.all.forEach { (key, value) ->
                try {
                    val index = URI.create(key)
                    val internalCookies =
                        gson.decodeFromString<MutableList<InternalCookie>>(value.toString())
                    val cookies = internalCookies.map { it.toHttpCookie() }.toMutableList()
                    uriIndex[index] = cookies
                } catch (exception: Throwable) {
                    Log.e(
                        javaClass.simpleName,
                        "Error while loading key = $key, value = $value from cookie store named $name",
                        exception
                    )
                }
            }
        }
    }

    override fun removeAll(): Boolean =
        synchronized(SharedPreferencesCookieStore::class.java) {
            super.removeAll()
            preferences.edit().clear().apply()
            true
        }

    override fun add(uri: URI?, cookie: HttpCookie?) =
        synchronized(SharedPreferencesCookieStore::class.java) {
            uri ?: return@synchronized

            super.add(uri, cookie)
            val index = getEffectiveURI(uri)
            val cookies = uriIndex[index] ?: return@synchronized

            val internalCookies = cookies.map {
                InternalCookie(it).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        httpOnly = it.isHttpOnly
                    }
                }
            }

            val json = gson.encodeToString(internalCookies)

            preferences
                .edit()
                .putString(index.toString(), json)
                .apply()
        }

    override fun remove(uri: URI?, cookie: HttpCookie?): Boolean =
        synchronized(SharedPreferencesCookieStore::class.java) {
            uri ?: return false

            val result = super.remove(uri, cookie)
            val index = getEffectiveURI(uri)
            val cookies = uriIndex[index]
            val internalCookies = cookies?.map {
                InternalCookie(it).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        httpOnly = it.isHttpOnly
                    }
                }
            }

            val json = gson.encodeToString(internalCookies)

            preferences.edit().apply {
                when (cookies) {
                    null -> remove(index.toString())
                    else -> putString(index.toString(), json)
                }
            }.apply()

            return result
        }
}