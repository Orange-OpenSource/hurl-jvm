package com.orange.ccmd.hurl.core.run.experimental

import com.orange.ccmd.hurl.core.http.Cookie
import com.orange.ccmd.hurl.core.http.HttpClient
import java.time.Instant
import java.util.*


sealed class Command {

    abstract fun run(httpClient: HttpClient): Boolean

    companion object {
        fun fromString(text: String): Command? {
            return when {
                text.startsWith("# @cookie_storage_clear") -> parseCookieStorageClearCommand()
                text.startsWith("# @cookie_storage_set") -> parseCookieStorageSetCommand(text)
                else -> null
            }
        }
    }
}


fun parseCookieStorageClearCommand(): CookieStorageClearCommand? {
    return CookieStorageClearCommand
}

/**
 * From https://curl.haxx.se/docs/http-cookies.html
 */
fun parseCookieStorageSetCommand(text: String): CookieStorageSetCommand? {
    val tokens = text
        .removePrefix("# @cookie_storage_set:")
        .trim()
        .split(regex="""\s+""".toRegex())
    if (tokens.size != 7) {
        return null
    }

    // TODO: add better validation on tokens
    return try {
        CookieStorageSetCommand(
            domain = tokens[0],
            includeSubDomains = tokens[1] == "TRUE",
            path = tokens[2],
            httpOnly = tokens[3] == "TRUE",
            expires = tokens[4].toInt(),
            name = tokens[5],
            value = tokens[6]
        )
    } catch (e: NumberFormatException) {
        null
    }
}


object CookieStorageClearCommand : Command() {

    override fun run(httpClient: HttpClient): Boolean {
        httpClient.clearCookieStorage()
        return true
    }
}

data class CookieStorageSetCommand(
    val domain: String,
    val includeSubDomains: Boolean,
    val path: String,
    val httpOnly: Boolean,
    val expires: Int,
    val name: String,
    val value: String,
) : Command() {

    override fun run(httpClient: HttpClient): Boolean {
        val expires = if (expires != 0) {
            val instant = Instant.ofEpochSecond(expires.toLong())
            Date.from(instant)
        } else {
            null
        }
        val cookie = Cookie(
            domain = domain,
            path = path,
            secure = httpOnly,
            expires = expires,
            name = name,
            value = value
        )
        httpClient.addCookie(cookie = cookie)
        return true
    }
}