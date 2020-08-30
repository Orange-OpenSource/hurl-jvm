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


data class CookiePathQuery(val name: String, val attribute: CookiePathAttribute) {

    companion object {

        fun fromString(query: String): CookiePathQuery {

            val start = query.indexOf("[")
            if (start == -1) {
                return CookiePathQuery(name = query, attribute = CookiePathAttributeValue)
            }
            val end = query.indexOf("]", startIndex = start+1)
            if (end == -1) {
                throw IllegalArgumentException("$query is not a valid cookie query")
            }
            val name = query.substring(0 until start)
            val rawAttribute = query.substring((start+1) until end)
            // Does the query contains a cookie attribute?
            val attribute = when (rawAttribute.toLowerCase()) {
                "value" -> CookiePathAttributeValue
                "expires" -> CookiePathAttributeExpires
                "max-age" -> CookiePathAttributeMaxAge
                "domain" -> CookiePathAttributeDomain
                "path" -> CookiePathAttributePath
                "secure" -> CookiePathAttributeSecure
                "httponly" -> CookiePathAttributeHttpOnly
                "samesite" -> CookiePathAttributeSameSite
                else -> throw IllegalArgumentException("$rawAttribute is not a valid cookie attribute")
            }

            return CookiePathQuery(name = name, attribute = attribute)
        }
    }

}