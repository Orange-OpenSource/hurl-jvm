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

package com.orange.ccmd.hurl.core.http

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.StandardCharsets


/**
 * A specification of an HTTP request, with the method, url,
 * query string parameters (maybe empty), headers, body, form params and cookies.
 * A HttRequestSpec represents user specifies values, where a {@link HttpRequest}
 * represents a real HTTP request executed by a client, where, for instance,
 * headers are added by the client, and query string params transformed to an url.
 */
data class HttpRequest(
    val method: String,
    val url: String,
    val queryStringParams: List<QueryStringParam> = emptyList(),
    val headers: List<Header> = emptyList(),
    val body: RequestBody? = null,
    val formParams: List<FormParam> = emptyList(),
    val multipartFormDatas: List<FormData> = emptyList(),
    val cookies: List<Cookie> = emptyList()
) {
    // TODO: Content of the body request, in unicode.
    //  The encoding of the response content should is determined based solely on HTTP headers,
    //  (see RFC 2616 https://tools.ietf.org/html/rfc2616)
    val text: String?

    init {
        text = if (body != null) {
            val decoder = StandardCharsets.UTF_8.newDecoder()
            try {
                decoder.decode(ByteBuffer.wrap(body.data)).toString()
            } catch (ex: CharacterCodingException) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Returns the list of header (a name, value pair of string) for a given header name.
     * A HTTP header can have multiple values.
     * @return the list of headers for a name.
     */
    fun headersForName(name: String): List<Header> =
        headers.filter { (k, _) -> k.equals(name, ignoreCase = true) }

    override fun toString(): String {
        return "HttpRequestSpec(method='$method', url='$url', queryStringParams=$queryStringParams, headers=$headers, body=$body)"
    }


}
