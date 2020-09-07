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
import com.orange.ccmd.hurl.core.utils.getJsonByteCount
import com.orange.ccmd.hurl.core.utils.getXmlByteCount
import com.orange.ccmd.hurl.core.utils.slice
import com.orange.ccmd.hurl.core.utils.string

internal fun HurlParser.base64(): Base64? {
    val begin = position.copy()

    val prefix = literal("base64,") ?: return null
    val spaces0 = zeroOrMore { space() }
    val base64String = base64String() ?: return null
    val spaces1 = zeroOrMore { space() }
    val suffix = literal(";") ?: return null

    return Base64(
        begin = begin,
        end = position,
        prefix = prefix,
        spaces0 = spaces0,
        base64String = base64String,
        spaces1 = spaces1,
        suffix = suffix
    )
}

internal fun HurlParser.bytes(): Bytes? {
    val node = choice(listOf(
        { json() },
        { xml() },
        { rawString() },
        { base64() },
        { file() }
    ))
    return if (node != null) {
        node
    } else {
        error = SyntaxError("a valid bytes is expected", position)
        null
    }
}

internal fun HurlParser.file(): File? {
    val begin = position.copy()

    val prefix = literal("file,") ?: return null
    val spaces0 = zeroOrMore { space() }
    val fileName = pathString() ?: return null
    val spaces1 = zeroOrMore { space() }
    val suffix = literal(";") ?: return null
    return File(
        begin = begin,
        end = position,
        prefix = prefix,
        spaces0 = spaces0,
        fileName = fileName,
        spaces1 = spaces1,
        suffix = suffix
    )
}

internal fun HurlParser.json(): Json? {
    val begin = position.copy()

    val remainingCps = buffer.slice(begin.offset)
    val remainingBytes = remainingCps.string().toByteArray()
    val bytesCount = getJsonByteCount(remainingBytes)
    if (bytesCount == null) {
        error = SyntaxError("valid JSON body is expected", position)
        return null
    }
    val value = remainingBytes.slice(from = 0, to = bytesCount)
    val text = value.string()
    // Advance buffer from the number of bytes consumed.
    val cpsCount = text.codePoints().toArray().size
    read(cpsCount)
    return Json(begin = begin, end = position, text = text)
}

internal fun HurlParser.query(): Query? {
    return choice(listOf(
        { statusQuery() },
        { headerQuery() },
        { cookieQuery() },
        { bodyQuery() },
        { xPathQuery() },
        { jsonPathQuery() },
        { regexQuery() },
        { variableQuery() },
    ))
}

internal fun HurlParser.queryType(type: String): QueryType? {
    val begin = position.copy()
    val value = literal(type)?.value ?: return null
    return QueryType(begin = begin, end = position, value = value)
}

internal fun HurlParser.rawString(): RawString? {
    val begin = position.copy()

    literal(multilineMarker) ?: return null
    optional { leadingRawStringPrefix() }

    val cps = readWhile { !isMultilineMarker() }
    if (cps == null) {
        error = SyntaxError("invalid multiline-string", position)
        return null
    }
    val value = cps.string()
    literal(multilineMarker) ?: return null
    val text = buffer.slice(begin.offset, position.offset).string()

    return RawString(begin = begin, end = position, value = value, text = text)
}

internal fun HurlParser.xml(): Xml? {
    val begin = position.copy()

    // Check if there is a begining of xml header.
    val cp = peek()
    if (cp == null || cp != '<'.toInt()) {
        error = SyntaxError("Xml is expected", position)
        return null
    }

    val remainingCps = buffer.slice(begin.offset)
    val remainingText = remainingCps.string()
    val bytesCount = getXmlByteCount(remainingText)
    if (bytesCount == null) {
        error = SyntaxError("valid Xml body is expected", position)
        return null
    }
    val value = remainingText.toByteArray().slice(from = 0, to = bytesCount)
    val text = value.string()
    // Advance buffer from the number of bytes consumed.
    val cpsCount = text.codePoints().toArray().size
    read(cpsCount)
    return Xml(begin = begin, end = position, text = text)
}

internal fun HurlParser.variableQuery(): VariableQuery? {
    val begin = position.copy()

    val type = queryType("variable") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val variable = quotedString() ?: return null

    return VariableQuery(begin = begin, end = position, type = type, spaces = spaces, variable = variable)
}

internal fun HurlParser.xPathQuery(): XPathQuery? {
    val begin = position.copy()

    val type = queryType("xpath") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null

    return XPathQuery(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}