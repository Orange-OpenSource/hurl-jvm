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

package com.orange.ccmd.hurl.core.ast

import com.orange.ccmd.hurl.core.parser.SyntaxError
import com.orange.ccmd.hurl.core.parser.any
import com.orange.ccmd.hurl.core.parser.isAsciiDigit
import com.orange.ccmd.hurl.core.parser.isAsciiLetter
import com.orange.ccmd.hurl.core.utils.string

internal fun HurlParser.cookiesSection(): CookiesSection? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val header = sectionHeader("Cookies") ?: return null
    val lt = lineTerminator() ?: return null
    val cookies = zeroOrMore { cookie() }
    return CookiesSection(
        begin = begin,
        end = position.copy(),
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        cookies = cookies
    )
}

internal fun HurlParser.formParamsSection(): FormParamsSection? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val header = sectionHeader("FormParams") ?: return null
    val lt = lineTerminator() ?: return null
    val params = zeroOrMore { param() }
    return FormParamsSection(
        begin = begin,
        end = position.copy(),
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        params = params
    )
}

internal fun HurlParser.method(): Method? {
    val begin = position.copy()

    val methods = listOf(
        "GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH"
    )
    for (m in methods) {
        val node = optional { literal(m) }
        if (node != null) {
            return Method(begin = begin, end = position.copy(), value = m)
        }
    }
    error = SyntaxError("method is expected", position.copy())
    return null
}

internal fun HurlParser.multipartFormDataSection(): MultipartFormDataSection? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val header = sectionHeader("MultipartFormData") ?: return null
    val lt = lineTerminator() ?: return null
    val fileParams = mutableListOf<FileParam>()
    val params = mutableListOf<Param>()

    while (true) {
        // We try to parse file-param first not to parse b: file,toto; as a param.
        val fileParam = optional { fileParam() }
        if (fileParam != null) {
            fileParams.add(fileParam)
            continue
        }
        val param = optional { param() }
        if (param != null) {
            params.add(param)
            continue
        }
        break
    }

    return MultipartFormDataSection(
        begin = begin,
        end = position.copy(),
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        params = params,
        fileParams = fileParams
    )
}

internal fun HurlParser.queryStringParamsSection(): QueryStringParamsSection? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val header = sectionHeader("QueryStringParams") ?: return null
    val lt = lineTerminator() ?: return null
    val params = zeroOrMore { param() }
    return QueryStringParamsSection(
        begin = begin,
        end = position.copy(),
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        params = params
    )
}

internal fun HurlParser.request(): Request? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces0 = zeroOrMore { space() }
    val method = method() ?: return null
    val spaces1 = oneOrMore { space() } ?: return null
    val url = url() ?: return null
    val lt = lineTerminator() ?: return null
    val headers = zeroOrMore { header() }
    val sections = zeroOrMore { requestSection() }
    val body = optional { body() }

    return Request(
        begin = begin,
        end = position.copy(),
        lts = lts,
        spaces0 = spaces0,
        method = method,
        spaces1 = spaces1,
        url = url,
        lt = lt,
        headers = headers,
        sections = sections,
        body = body
    )
}

internal fun HurlParser.requestSection(): RequestSection? {
    return choice(listOf(
        { queryStringParamsSection() },
        { formParamsSection() },
        { cookiesSection() },
        { multipartFormDataSection() }
    ))
}

/**
 * Parse an url
 * @see <a href="https://tools.ietf.org/html/rfc3986">RFC3986<a>
 */
internal fun HurlParser.url(): Url? {
    val begin = position.copy()

    // FIXME: not a real url parsing
    // For instance, we doen't invalidate query parameters like %2X.
    fun Int.isGenDelims() = any(":/?#[]@")

    fun Int.isSubDelims() = any("!$&\\()*+,;=")
    val url = readWhile {
        val isUnreserved = it.isAsciiLetter || it.isAsciiDigit || it.any("-._~")
        val isReserved = it.isGenDelims() || it.isSubDelims()
        val isHurlSpecific = it == '{'.code || it == '}'.code
        val isQuery = it == '%'.code
        isReserved || isUnreserved || isQuery || isHurlSpecific
    }
    if (url == null || url.isEmpty()) {
        error = SyntaxError("url is expected", position.copy())
        return null
    }
    return Url(begin = begin, value = url.string(), end = position.copy())
}
