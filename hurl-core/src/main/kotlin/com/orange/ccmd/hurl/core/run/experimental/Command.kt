/*
 * Copyright (C) 2020 Orange
 *
 * Hurl JVM (JVM Runner for https://hurl.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

    override fun toString(): String {
        return "cookie_storage_clear"
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

    override fun toString(): String {
        return "cookie_storage_set(domain='$domain', includeSubDomains=$includeSubDomains, path='$path', httpOnly=$httpOnly, expires=$expires, name='$name', value='$value')"
    }

}