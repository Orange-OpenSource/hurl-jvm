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

import com.orange.ccmd.hurl.core.http.Header
import com.orange.ccmd.hurl.core.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.protocol.HttpContext


/**
 * Interceptor to capture a request state.
 */
class CapturedRequestInterceptor : HttpRequestInterceptor {

    var capturedRequest: HttpRequest? = null

    override fun process(request: org.apache.http.HttpRequest?, context: HttpContext?) {
        if (request !is HttpUriRequest) {
            return
        }
        val method = request.method
        val url = request.uri.toASCIIString()
        val headers = request.allHeaders.map { Header(it.name, it.value) }
        capturedRequest = HttpRequest(method = method, url = url, headers = headers)
    }
}