package io.appwrite.services

import io.appwrite.WebAuthServer
import io.appwrite.cookies.stores.CustomCookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

/**
 * Create OAuth2 session
 *
 * Allow the user to login to their account using the OAuth2 provider of their choice. Each OAuth2 provider should be enabled from the Appwrite console first. Use the success and failure arguments to provide a redirect URL&#039;s back to your app when login is completed.If there is already an active session, the new session will be attached to the logged-in account. If there are no active sessions, the server will attempt to look for a user with the same email address as the email received from the OAuth2 provider and attach the new session to the existing user. If no matching user is found - the server will create a new user.A user is limited to 10 active sessions at a time by default. [Learn more about session limits](https://appwrite.io/docs/authentication-security#limits).
 *
 * @param provider OAuth2 Provider. Currently, supported providers are: amazon, apple, auth0, authentik, autodesk, bitbucket, bitly, box, dailymotion, discord, disqus, dropbox, etsy, facebook, github, gitlab, google, linkedin, microsoft, notion, oidc, okta, paypal, paypalSandbox, podio, salesforce, slack, spotify, stripe, tradeshift, tradeshiftBox, twitch, wordpress, yahoo, yammer, yandex, zoom.
 * @param success URL to redirect back to your app after a successful login attempt.  Only URLs from hostnames in your project's platform list are allowed. This requirement helps to prevent an [open redirect](https://cheatsheetseries.owasp.org/cheatsheets/Unvalidated_Redirects_and_Forwards_Cheat_Sheet.html) attack against your project API.
 * @param failure URL to redirect back to your app after a failed login attempt.  Only URLs from hostnames in your project's platform list are allowed. This requirement helps to prevent an [open redirect](https://cheatsheetseries.owasp.org/cheatsheets/Unvalidated_Redirects_and_Forwards_Cheat_Sheet.html) attack against your project API.
 * @param scopes A list of custom OAuth2 scopes. Check each provider internal docs for a list of supported scopes. Maximum of 100 scopes are allowed, each 4096 characters long.
 */
@JvmOverloads
actual suspend fun Account.createOAuth2Session(
    provider: String,
    success: String?,
    failure: String?,
    scopes: List<String>?,
    serverPort: Int?,
    successHtmlResponse: ByteArray?,
    failureHtmlResponse: ByteArray?
) {
    val apiPath = "account/sessions/oauth2/$provider"

    val apiParams = mutableMapOf(
        "success" to success,
        "failure" to failure,
        "scopes" to scopes,
        "project" to client.config["project"]
    )
    val apiQuery = mutableListOf<String>()
    apiParams.forEach {
        when (it.value) {
            null -> {
                return@forEach
            }

            is List<*> -> {
                apiQuery.add("${it.key}[]=${it.value.toString()}")
            }

            else -> {
                apiQuery.add("${it.key}=${it.value.toString()}")
            }
        }
    }

    val apiUrl = URI("${client.endPoint}/${apiPath}?${apiQuery.joinToString("&")}")

    withContext(Dispatchers.IO) {
        Desktop.getDesktop().browse(apiUrl)
    }

    val codes = WebAuthServer.create(
        serverPort!!,
        success!!,
        failure!!,
        successHtmlResponse,
        failureHtmlResponse
    )

    codes?.let {
        val cookies = Cookie(
            name = codes["key"].toString(),
            value = codes["secret"].toString(),
            domain = URI(client.endPoint).host,
            httpOnly = true
        )
        CustomCookiesStorage.addCookie(Url(client.endPoint), cookies)
        return
    }
}