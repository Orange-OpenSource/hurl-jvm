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

package com.orange.ccmd.hurl.core.query.cookiepath

data class Cookie(
    val name: String,
    val value: String,
    val expires: String? = null,
    val maxAge: Int? = null,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean? = null,
    val httpOnly: Boolean? = null,
    val sameSite: String? = null,
) {

    companion object {

        private const val SET_COOKIE = "set-cookie:"

        /**
         * Parse a Cookie object from a Set-Cookie HTTP header value.
         * We can't use java.net.HttpCookie parse method because it is not
         * supporting SameSite attribute and doesn't give access to Expires
         * string value.
         * This parser is a very simple parser and doesn't support all the
         * legacy cookie formats.
         * @param header Set-Cookie HTTP header value
         * @return the Cookie created, or throw IllegalArgumentException if the header is not valid
         */
        fun fromHeader(header: String): Cookie {
            val name: String
            val value: String

            val tokens = header.split(";")
            if (tokens.isEmpty()) {
                throw IllegalArgumentException("Empty cookie header string")
            }

            // there should always have at least on name-value pair;
            // it's cookie's name
            val nameValuePair = tokens[0]
            val index = nameValuePair.indexOf('=')
            if (index != -1) {
                name = nameValuePair
                    .substring(0, index)
                    .trim()
                    .trim('"', '\'')
                value = nameValuePair
                    .substring(index + 1)
                    .trim()
                    .trim('"', '\'')
            } else {
                // no "=" in name-value pair; it's an error
                throw IllegalArgumentException("Invalid cookie name-value pair")
            }

            val pairAttributes = mutableMapOf<String, String>()
            val singleAttributes = mutableListOf<String>()
            for(i in 1 until tokens.size) {
                val token = tokens[i]
                val sep = token.indexOf('=')
                if (sep != -1) {
                    val attName = token
                        .substring(0, sep)
                        .trim()
                        .toLowerCase()
                    val attValue = token
                        .substring(sep + 1)
                        .trim()
                    pairAttributes[attName] = attValue
                } else {
                    val attName = token
                        .trim()
                        .toLowerCase()
                    singleAttributes.add(attName)
                }
            }

            return Cookie(
                name = name,
                value = value,
                expires = pairAttributes["expires"],
                maxAge = pairAttributes["max-age"]?.toInt(),
                domain = pairAttributes["domain"],
                path = pairAttributes["path"],
                secure = if ("secure" in singleAttributes) { true } else { null },
                httpOnly = if ("httponly" in singleAttributes) { true } else { null },
                sameSite = pairAttributes["samesite"],
            )
        }
    }
}

