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

package com.orange.ccmd.hurl.core.http.impl

import com.orange.ccmd.hurl.core.http.HeaderNames
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.protocol.HttpContext

/**
 * Interceptor that merge all the Cookie Header into a
 * single Cookie header.
 */
class CookieRequestInterceptor : HttpRequestInterceptor {

    override fun process(request: HttpRequest?, context: HttpContext?) {

        if (request !is HttpUriRequest) {
            return
        }

        // From https://tools.ietf.org/html/rfc6265#section-5.4
        //  If there is an unprocessed cookie in the cookie-list, output
        //  the characters %x3B and %x20 ("; ")
        val cookies = request.getHeaders(HeaderNames.COOKIE)
        if (cookies.size <= 1) {
            return
        }

        request.removeHeaders(HeaderNames.COOKIE)
        val mergedCookie = cookies
            .map { it.value }
            .sortedBy { it.toLowerCase() }
            .joinToString(separator = "; ")
        request.setHeader(HeaderNames.COOKIE, mergedCookie)
    }
}