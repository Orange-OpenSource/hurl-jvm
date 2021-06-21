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
import com.orange.ccmd.hurl.core.parser.isAsciiDigit
import com.orange.ccmd.hurl.core.utils.string

internal fun HurlParser.assert(): Assert? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces0 = zeroOrMore { space() }
    val query = query() ?: return null
    val spaces1 = oneOrMore { space() } ?: return null
    val predicate = predicate() ?: return null
    val lt = lineTerminator() ?: return null

    return Assert(
        begin = begin,
        end = position,
        lts = lts,
        spaces0 = spaces0,
        query = query,
        spaces1 = spaces1,
        predicate = predicate,
        lt = lt
    )
}

internal fun HurlParser.assertsSection(): AssertsSection? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val header = sectionHeader("Asserts") ?: return null
    val lt = lineTerminator() ?: return null
    val asserts = zeroOrMore { assert() }
    return AssertsSection(
        begin = begin,
        end = position,
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        asserts = asserts
    )
}

internal fun HurlParser.capture(): Capture? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces0 = zeroOrMore { space() }
    val name = keyString() ?: return null
    val spaces1 = zeroOrMore { space() }
    val colon = literal(":") ?: return null
    val spaces2 = zeroOrMore { space() }
    val query = query() ?: return null
    val spaces3 = zeroOrMore { space() }
    val subquery = if (spaces3.isNotEmpty()) {
        optional { subquery() }
    } else {
        null
    }
    val lt = lineTerminator() ?: return null

    return Capture(
        begin = begin,
        end = position,
        lts = lts,
        spaces0 = spaces0,
        name = name,
        spaces1 = spaces1,
        colon = colon,
        spaces2 = spaces2,
        query = query,
        spaces3 = spaces3,
        subquery = subquery,
        lt = lt
    )
}

internal fun HurlParser.capturesSection(): CapturesSection? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val header = sectionHeader("Captures") ?: return null
    val lt = lineTerminator() ?: return null
    val captures = zeroOrMore { capture() }
    return CapturesSection(
        begin = begin,
        end = position,
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        captures = captures
    )
}

internal fun HurlParser.response(): Response? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces0 = zeroOrMore { space() }
    val version = version() ?: return null
    val spaces1 = oneOrMore { space() } ?: return null
    val status = status() ?: return null
    val lt = lineTerminator() ?: return null
    val headers = zeroOrMore { header() }
    val sections = zeroOrMore { responseSection() }
    val body = optional { body() }

    return Response(
        begin = begin,
        end = position,
        lts = lts,
        spaces0 = spaces0,
        version = version,
        spaces1 = spaces1,
        status = status,
        lt = lt,
        headers = headers,
        sections = sections,
        body = body
    )
}

internal fun HurlParser.responseSection(): ResponseSection? {
    return choice(listOf(
        { capturesSection() },
        { assertsSection() }
    ))
}

internal fun HurlParser.status(): Status? {
    val begin = position.copy()

    // First, test if the status value is a wildcard status.
    val cp0 = peek() ?: return null
    if (cp0 == '*'.code) {
        read()
        return Status(begin = begin, end = position, value = AnyStatusValue, text = "*")
    }

    // It's not a wildcard status, so it must be an integer value.
    val cps = readWhile { it.isAsciiDigit }
    if (cps == null) {
        error = SyntaxError("0-9 is expected", position)
        return null
    }
    val digits = cps.string()
    val value = try {
        digits.toInt()
    } catch (e: NumberFormatException) {
        error = SyntaxError("0-9 is expected", position)
        return null
    }
    return Status(begin = begin, end = position, value = IntStatusValue(value = value), text = digits)
}

internal fun HurlParser.version(): Version? {
    val begin = position.copy()

    val versions = listOf("HTTP/1.0", "HTTP/1.1", "HTTP/2", "HTTP/*")
    for (v in versions) {
        val node = optional { literal(v) }
        if (node != null) {
            return Version(begin = begin, value = v, end = position)
        }
    }
    error = SyntaxError("version is expected", position)
    return null
}
