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

import com.orange.ccmd.hurl.core.query.InvalidQueryException

class CookiePath {

    companion object {

        /**
         * Evaluate a cookie-path query.
         *
         * @param expr cookie-path query
         * @param headers list of HTTP headers to evaluate the query against.
         * @return the cookie path result
         * @throws InvalidQueryException if expr is not a valid cookie path query.
         */
        fun evaluate(expr: String, headers: List<Pair<String, String>>): CookiePathResult {
            val query = try {
                CookiePathQuery.fromString(expr)
            } catch(e: IllegalArgumentException) {
                throw InvalidQueryException("Invalid cookie path query $expr")
            }

            // Filter set-cookie header among all HTTP headers.
            val cookie = try {
                headers
                    .filter { (name, _) -> name.lowercase() == "set-cookie" }
                    .map { (_, value) -> Cookie.fromHeader(header = value) }
                    .firstOrNull { it.name == query.name } ?: return CookiePathFailed
            } catch (e: IllegalArgumentException) {
                throw InvalidQueryException("Invalid set-cookie header in $headers")
            }

            val attribute = query.attribute
            return when {
                attribute is CookiePathAttributeValue -> CookiePathStringResult(value = cookie.value)
                attribute is CookiePathAttributeExpires &&  cookie.expires != null -> CookiePathStringResult(value = cookie.expires)
                attribute is CookiePathAttributeMaxAge && cookie.maxAge != null -> CookiePathNumberResult(value = cookie.maxAge)
                attribute is CookiePathAttributeDomain && cookie.domain != null -> CookiePathStringResult(value = cookie.domain)
                attribute is CookiePathAttributePath && cookie.path != null -> CookiePathStringResult(value = cookie.path)
                attribute is CookiePathAttributeSecure && cookie.secure != null -> CookiePathUnitResult
                attribute is CookiePathAttributeHttpOnly && cookie.httpOnly != null -> CookiePathUnitResult
                attribute is CookiePathAttributeSameSite && cookie.sameSite != null -> CookiePathStringResult(value = cookie.sameSite)
                else -> CookiePathFailed
            }

        }

    }
}