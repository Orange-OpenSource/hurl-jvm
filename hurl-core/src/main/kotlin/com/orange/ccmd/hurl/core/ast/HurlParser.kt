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

import com.orange.ccmd.hurl.core.utils.*

import java.util.Base64 as JdkBase64

/**
 * HurlParser is a recursive descent parser implementing Hurl grammar
 * as specified at https://hurl.dev/docs/grammar.html.
 */

class HurlParser(text: String) : Parser(text) {
    fun parse(): HurlFile? = hurlFile()
}

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

internal fun HurlParser.base64String(): Base64String? {
    val begin = position.copy()

    val cps = readWhile { it.isAsciiLetter || it.isAsciiDigit || it.isNewLine ||
            it == '+'.toInt() || it == '/'.toInt() || it == '='.toInt()
    }
    if (cps == null) {
        error = SyntaxError("a valid base64-string is expected", position)
        return null
    }
    val encoded = cps.string()
    val value = try { JdkBase64.getMimeDecoder().decode(encoded) } catch (e: IllegalArgumentException) { null }
    if (value == null) {
        error = SyntaxError("a valid base64-string is expected", position)
        return null
    }
    return Base64String(begin = begin, end = position, value = value, text = encoded)
}

internal fun HurlParser.body(): Body? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val bytes = bytes() ?: return null
    val lt = lineTerminator() ?: return null

    return Body(begin = begin, end = position, lts = lts, spaces = spaces, bytes = bytes, lt = lt)
}

internal fun HurlParser.bodyQuery(): BodyQuery? {
    val begin = position.copy()
    val type = queryType("body") ?: return null
    return BodyQuery(begin = begin, end = position, type = type)
}

internal fun HurlParser.bool(): Bool? {
    val begin = position.copy()

    val bools = listOf("true" to true, "false" to false)
    for ((text, value) in bools) {
        val lit = optional { literal(text) }
        if (lit != null) {
            return Bool(begin = begin, value = value, text = text, end = position)
        }
    }
    error = SyntaxError("true or false is expected", position)
    return null
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

internal fun HurlParser.comment(): Comment? {
    val pos = position.copy()

    literal("#") ?: return null
    readWhile { !it.isNewLine }
    val value = buffer.slice(pos.offset, position.offset).string()

    return Comment(begin = pos, end = position, value = value)
}

internal fun HurlParser.containPredicate(): ContainPredicate? {
    val begin = position.copy()
    val type = predicateType("contains") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return ContainPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.cookie(): Cookie? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces0 = zeroOrMore { space() }
    val name = keyString() ?: return null
    val spaces1 = zeroOrMore { space() }
    val colon = literal(":") ?: return null
    val spaces2 = zeroOrMore { space() }
    val cookieValue = cookieValue() ?: return null
    val lt = lineTerminator() ?: return null

    return Cookie(
        begin = begin,
        end = position,
        lts = lts,
        spaces0 = spaces0,
        name = name,
        spaces1 = spaces1,
        colon = colon,
        spaces2 = spaces2,
        value = cookieValue,
        lt = lt
    )
}

internal fun HurlParser.cookieQuery(): CookieQuery? {
    val begin = position.copy()

    val type = queryType("cookie") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val cookieName = quotedString() ?: return null

    return CookieQuery(begin = begin, end = position, type = type, spaces = spaces, expr = cookieName)
}

internal fun HurlParser.cookiesSection(): CookiesSection? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val header = sectionHeader("Cookies") ?: return null
    val lt = lineTerminator() ?: return null
    val cookies = zeroOrMore { cookie() }
    return CookiesSection(
        begin = begin,
        end = position,
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        cookies = cookies
    )
}

internal fun HurlParser.cookieValue(): CookieValue? {
    val begin = position.copy()

    // FIXME: remplacer par ascii printable
    val cookies = readWhile {
        it.isHurlTemplateControl ||
                it.isAsciiLetter ||
                it.isAsciiDigit ||
                it == ':'.toInt() ||
                it == '/'.toInt() ||
                it == '%'.toInt() ||
                it == '_'.toInt() ||
                it == '-'.toInt()
    }
    if (cookies == null || cookies.isEmpty()) {
        error = SyntaxError("[A-Za-z0-9:/%_-] char is expected in cookie-value", position)
        return null
    }
    return CookieValue(begin = begin, end = position, value = cookies.string())
}

internal fun HurlParser.countPredicate(): CountPredicate? {
    val begin = position.copy()
    val type = predicateType("countEquals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = integer() ?: return null
    return CountPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.entry(): Entry? {
    val begin = position.copy()

    val request = request() ?: return null
    val response = optional { response() }

    return Entry(begin = begin, end = position, request = request, response = response)
}

internal fun HurlParser.equalPredicate(): PredicateFunc? {
    return choice(listOf(
        { equalNumberPredicate() },
        { equalBoolPredicate() },
        { equalStringPredicate() },
    ))
}

internal fun HurlParser.equalBoolPredicate(): EqualBoolPredicate? {
    val begin = position.copy()
    val type = predicateType("equals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = bool() ?: return null
    return EqualBoolPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.equalNumberPredicate(): EqualNumberPredicate? {
    val begin = position.copy()
    val type = predicateType("equals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = choice(listOf(
        { float() },
        { integer() }
    )) ?: return null
    return EqualNumberPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.equalStringPredicate(): EqualStringPredicate? {
    val begin = position.copy()
    val type = predicateType("equals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return EqualStringPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.existPredicate(): ExistPredicate? {
    val begin = position.copy()
    val type = predicateType("exists") ?: return null
    return ExistPredicate(begin = begin, end = position, type = type)
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

internal fun HurlParser.fileParam(): FileParam? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces0 = zeroOrMore { space() }
    val key = keyString() ?: return null
    val spaces1 = zeroOrMore { space() }
    val colon = literal(":") ?: return null
    val spaces2 = zeroOrMore { space() }
    val file = fileValue() ?: return null
    val lt = lineTerminator() ?: return null

    return FileParam(
        begin = begin,
        end = position,
        lts = lts,
        spaces0 = spaces0,
        key = key,
        spaces1 = spaces1,
        colon = colon,
        spaces2 = spaces2,
        file = file,
        lt = lt
    )
}

internal fun HurlParser.fileValue(): FileValue? {
    val begin = position.copy()

    val prefix = literal("file,") ?: return null
    val spaces0 = zeroOrMore { space() }
    val fileName = pathString() ?: return null
    val spaces1 = zeroOrMore { space() }
    val suffix = literal(";") ?: return null
    val spaces2 = zeroOrMore { space() }
    var contentType = optional{ valueString() }

    // Because we're reusing value-string to parse the optional content type, and because a value-string
    // can be empty (for instance, when there is no codepoint left), the resulted content type is considered null
    // if the parsed value string is empty.
    if (contentType?.value.isNullOrEmpty()) {
        contentType = null
    }

    return FileValue(
        begin = begin,
        end = position,
        prefix = prefix,
        spaces0 = spaces0,
        fileName = fileName,
        spaces1 = spaces1,
        suffix = suffix,
        spaces2 = spaces2,
        contentType = contentType
    )
}

internal fun HurlParser.float(): Number? {
    val begin = position.copy()

    val cp0 = peek() ?: return null
    if (cp0 == '-'.toInt() || cp0 == '+'.toInt()) {
        read()
    }
    val cps0 = readWhile { it.isAsciiDigit }
    if (cps0 == null || cps0.isEmpty()) {
        error = SyntaxError("[0-9] is expected", position)
        return null
    }
    val cp1 = read()
    if (cp1 == null || cp1 != '.'.toInt()) {
        error = SyntaxError("'.' is expected", position)
        return null
    }
    val cps1 = readWhile { it.isAsciiDigit }
    if (cps1 == null || cps1.isEmpty()) {
        error = SyntaxError("[0-9] is expected", position)
        return null
    }
    val text = buffer.slice(begin.offset, position.offset).string()
    val value = text.toDoubleOrNull() ?: return null
    return Number(begin = begin, end = position, value = value, text = text)
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
        end = position,
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        params = params
    )
}

internal fun HurlParser.header(): Header? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val keyValue = keyValue() ?: return null
    val lt = lineTerminator() ?: return null

    return Header(
        begin = begin,
        end = position,
        lts = lts,
        spaces = spaces,
        keyValue = keyValue,
        lt = lt
    )
}

internal fun HurlParser.headerQuery(): HeaderQuery? {
    val begin = position.copy()

    val type = queryType("header") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val headerName = quotedString() ?: return null

    return HeaderQuery(begin = begin, end = position, type = type, spaces = spaces, headerName = headerName)
}

internal fun HurlParser.hurlFile(): HurlFile? {
    val begin = position.copy()

    val entries = zeroOrMore { entry() }
    val lts = zeroOrMore { lineTerminator() }

    if (error != null) {
        return null
    }
    if (left() > 0) {
        // There are still codepoints into read in the buffer. The next [peek] call
        // can't return a null value.
        val cp = peek()
        error = SyntaxError("unexpected char '${cp?.codePointToString()}'", position)
        return null
    }
    return HurlFile(
        begin = begin,
        end = position,
        entries = entries,
        lts = lts
    )
}

internal fun HurlParser.includePredicate(): PredicateFunc? {
    return choice(listOf(
        { includeBoolPredicate() },
        { includeNumberPredicate() },
        { includeStringPredicate() },
    ))
}

internal fun HurlParser.includeBoolPredicate(): IncludeBoolPredicate? {
    val begin = position.copy()
    val type = predicateType("includes") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = bool() ?: return null
    return IncludeBoolPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.includeNumberPredicate(): IncludeNumberPredicate? {
    val begin = position.copy()
    val type = predicateType("includes") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = choice(listOf(
        { float() },
        { integer() }
    )) ?: return null
    return IncludeNumberPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.includeStringPredicate(): IncludeStringPredicate? {
    val begin = position.copy()
    val type = predicateType("includes") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return IncludeStringPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.integer(): Number? {
    val begin = position.copy()

    val cp = peek() ?: return null
    if (cp == '-'.toInt() || cp == '+'.toInt()) {
        read()
    }
    val cps = readWhile { it.isAsciiDigit }
    if (cps == null || cps.isEmpty()) {
        error = SyntaxError("[0-9] is expected", position)
        return null
    }
    val text = buffer.slice(begin.offset, position.offset).string()
    val value = text.toIntOrNull() ?: return null
    return Number(begin = begin, end = position, value = value.toDouble(), text = text)
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

internal fun HurlParser.jsonPathQuery(): JsonPathQuery? {
    val begin = position.copy()

    val type = queryType("jsonpath") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null

    return JsonPathQuery(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.keyString(): HString? {
    val begin = position.copy()

    var key = ""

    while (true) {
        var cp = peek()
        if (cp == null) {
            break
        } else if (cp.isAsciiLetter || cp.isAsciiDigit || cp == '_'.toInt() || cp == '-'.toInt() || cp == '.'.toInt()) {
            read()
            key += cp.codePointToString()
        } else if (cp == '\\'.toInt()) {
            read()
            cp = read()
            when (cp) {
                null -> {
                    error = SyntaxError("invalid key-string", position)
                    return null
                }
                '#'.toInt() -> key += "#"
                ' '.toInt() -> key += " "
                ':'.toInt() -> key += ":"
                '\\'.toInt() -> key += "\\"
                'b'.toInt() -> key += "\b"
                'n'.toInt() -> key += "\n"
                'r'.toInt() -> key += "\r"
                't'.toInt() -> key += "\t"
                'u'.toInt() -> {
                    val unicode = unicodeChar() ?: return null
                    key += unicode.codePointToString()
                }
                else -> {
                    error = SyntaxError("invalid escape char ${cp.codePointToString()}", position)
                    return null
                }
            }
        } else {
            break
        }
    }

    if (position.offset == begin.offset) {
        error = SyntaxError("invalid empty key-string", position)
        return null
    }

    val text = buffer.slice(begin.offset, position.offset).string()
    return HString(begin = begin, end = position, value = key, text = text)
}

internal fun HurlParser.keyValue(): KeyValue? {
    val begin = position.copy()

    val key = keyString() ?: return null
    val spaces0 = zeroOrMore { space() }
    val colon = literal(":") ?: return null
    val spaces1 = zeroOrMore { space() }
    val value = valueString() ?: return null

    return KeyValue(
        begin = begin,
        end = position,
        key = key,
        spaces0 = spaces0,
        colon = colon,
        spaces1 = spaces1,
        value = value
    )
}

internal fun HurlParser.lineTerminator(): LineTerminator? {
    val begin = position.copy()

    val spaces = zeroOrMore { space() }
    val comment = optional { comment() }
    val newLine = optional { newLine() }
    // If line terminator doesn(t end with a newLine, we must be
    // at the end of file.
    if (newLine == null && left() > 0) {
        error = SyntaxError("newline is expected", begin)
        return null
    }
    return LineTerminator(begin = begin, end = position, spaces = spaces, comment = comment, newLine = newLine)
}

internal fun HurlParser.literal(literal: String): Literal? {
    val begin = position.copy()

    val cps = literal.codePoints().toArray()
    for (i in cps.indices) {
        val c = peek()
        if (c == null) {
            val message = "'$literal' is expected, invalid eof instead of '${cps[i].codePointToString()}'"
            error = SyntaxError(message, position)
            return null
        } else if (c != cps[i]) {
            val message =
                "'$literal' is expected, invalid '${c.codePointToString()}' instead of '${cps[i].codePointToString()}'"
            error = SyntaxError(message, position)
            return null
        }
        read()
    }
    return Literal(begin = begin, end = position, value = literal)
}

internal fun HurlParser.matchPredicate(): MatchPredicate? {
    val begin = position.copy()
    val type = predicateType("matches") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return MatchPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.method(): Method? {
    val begin = position.copy()

    val methods = listOf(
        "GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH"
    )
    for (m in methods) {
        val node = optional { literal(m) }
        if (node != null) {
            return Method(begin = begin, end = position, value = m)
        }
    }
    error = SyntaxError("method is expected", position)
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
        end = position,
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        params = params,
        fileParams = fileParams
    )
}

internal const val multilineMarker = "```"

internal fun HurlParser.isMultilineMarker(): Boolean {
    val begin = position.copy()
    val isMarker = literal(multilineMarker) != null
    error = null
    position = begin
    return isMarker
}

internal fun HurlParser.leadingRawStringPrefix(): HString? {
    val begin = position.copy()

    zeroOrMore { space() }
    newLine() ?: return null
    val cps = buffer.slice(begin.offset, position.offset)
    val text = cps.string()
    return HString(begin = begin, end = position, value = text, text = text)
}

internal fun HurlParser.not(): Not? {
    val begin = position.copy()

    val text = literal("not") ?: return null
    return Not(begin = begin, end = position, text = text)
}

internal fun HurlParser.newLine(): NewLine? {
    val begin = position.copy()

    when (read()) {
        '\n'.toInt() -> { }
        '\r'.toInt() -> {
            val cp = read()
            if (cp == null || cp != '\n'.toInt()) {
                error = SyntaxError("\\n is expected", begin)
                return null
            }
        }
        else -> {
            error = SyntaxError("\\n or \\r\\n is expected", begin)
            return null
        }
    }
    val eol = buffer.slice(begin.offset, position.offset)
    return NewLine(begin = begin, end = position, value = eol.string())
}

internal fun HurlParser.param(): Param? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val keyValue = keyValue() ?: return null
    val lt = lineTerminator() ?: return null

    return Param(
        begin = begin,
        end = position,
        lts = lts,
        spaces = spaces,
        keyValue = keyValue,
        lt = lt
    )
}

internal fun HurlParser.pathString(): HString? {
    val begin = position.copy()

    val cps = readWhile {
        it.isAsciiLetter || it.isAsciiDigit || it.isAsciiDigit ||
                it == '.'.toInt() || it == '/'.toInt() || it == '+'.toInt() || it == '_'.toInt() || it == '-'.toInt()
    }
    if (cps == null) {
        error = SyntaxError("a valid filename is expected", position)
        return null
    }
    val fileName = cps.string()
    if (fileName.contains("..")) {
        error = SyntaxError("relative filename is not valid", position)
        return null
    }
    return HString(begin = begin, end = position, value = fileName, text = fileName)
}

internal fun HurlParser.predicate(): Predicate? {
    val begin = position.copy()

    val not = optional { not() }
    val spaces = if (not != null) {
        oneOrMore { space() } ?: return null
    } else {
        listOf()
    }
    val predicateFunc = predicateFunc() ?: return null
    return Predicate(begin = begin, end = position, not = not, spaces = spaces, predicateFunc = predicateFunc)
}

internal fun HurlParser.predicateFunc(): PredicateFunc? {
    return choice(listOf(
        { equalPredicate() },
        { countPredicate() },
        { startWithPredicate() },
        { containPredicate() },
        { includePredicate() },
        { matchPredicate() },
        { existPredicate() },
    ))
}

internal fun HurlParser.predicateType(type: String): PredicateType? {
    val begin = position.copy()
    val value = literal(type)?.value ?: return null
    return PredicateType(begin = begin, end = position, value = value)
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

internal fun HurlParser.queryStringParamsSection(): QueryStringParamsSection? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val header = sectionHeader("QueryStringParams") ?: return null
    val lt = lineTerminator() ?: return null
    val params = zeroOrMore { param() }
    return QueryStringParamsSection(
        begin = begin,
        end = position,
        lts = lts,
        spaces = spaces,
        header = header,
        lt = lt,
        params = params
    )
}

internal fun HurlParser.quotedString(): HString? {
    val begin = position.copy()

    var cp = read()
    if (cp == null || cp != '"'.toInt()) {
        error = SyntaxError("\" is expected at quoted-string beginning", position)
        return null
    }

    var value = ""

    while (true) {
        cp = read()
        if (cp == null) {
            error = SyntaxError("\" is expected at quoted-string end", position)
            return null
        } else if (cp == '"'.toInt()) {
            break
        } else if (cp == '\\'.toInt()) {
            cp = read()
            when (cp) {
                null -> {
                    error = SyntaxError("invalid quoted-string", position)
                    return null
                }
                '"'.toInt() -> value += "\""
                '\\'.toInt() -> value += "\\"
                'b'.toInt() -> value += "\b"
                'n'.toInt() -> value += "\n"
                'r'.toInt() -> value += "\r"
                't'.toInt() -> value += "\t"
                'u'.toInt() -> {
                    val unicode = unicodeChar() ?: return null
                    value += unicode.codePointToString()
                }
                else -> {
                    error = SyntaxError("invalid escape char ${cp.codePointToString()}", position)
                    return null
                }
            }
        } else {
            value += cp.codePointToString()
        }
    }

    val text = buffer.slice(begin.offset, position.offset).string()
    return HString(begin = begin, end = position, value = value, text = text)
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

internal fun HurlParser.regexQuery(): RegexQuery? {
    val begin = position.copy()

    val type = queryType("regex") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null

    return RegexQuery(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.regexSubquery(): RegexSubquery? {
    val begin = position.copy()

    val type = subqueryType("regex") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null

    return RegexSubquery(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
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
        end = position,
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

internal fun HurlParser.sectionHeader(section: String): SectionHeader? {
    val begin = position.copy()
    val value = literal("[$section]")?.value ?: return null
    return SectionHeader(begin = begin, end = position, value = value)
}

internal fun HurlParser.space(): Space? {
    val begin = position.copy()

    val cp = read()
    if (cp == null || !cp.isAsciiSpace) {
        error = SyntaxError("space or tab is expected", position)
        return null
    }
    return Space(begin = begin, end = position, value = cp.codePointToString())
}

internal fun HurlParser.startWithPredicate(): StartWithPredicate? {
    val begin = position.copy()
    val type = predicateType("startsWith") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return StartWithPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.status(): Status? {
    val begin = position.copy()

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
    return Status(begin = begin, end = position, value = value, text = digits)
}

internal fun HurlParser.statusQuery(): StatusQuery? {
    val begin = position.copy()
    val type = queryType("status") ?: return null
    return StatusQuery(begin = begin, end = position, type = type)
}

internal fun HurlParser.subquery(): Subquery? {
    return regexSubquery()
}

internal fun HurlParser.subqueryType(type: String): SubqueryType? {
    val begin = position.copy()
    val value = literal(type)?.value ?: return null
    return SubqueryType(begin = begin, end = position, value = value)
}

internal fun HurlParser.unicodeChar(): Int? {
    // Unicode literal are {XXXX}
    var cp = read()
    if (cp == null || cp != '{'.toInt()) {
        error = SyntaxError("{ expected, invalid unicode literal", position)
        return null
    }
    val cps = readWhile { it.isAsciiDigit || it.isHexaLetter }
    if (cps == null || cps.isEmpty() || cps.size > 8) {
        error = SyntaxError("invalid unicode literal", position)
        return null
    }
    cp = read()
    if (cp == null || cp != '}'.toInt()) {
        error = SyntaxError("} expected, invalid unicode literal", position)
        return null
    }
    return cps.string().toInt(radix = 16)
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
        val isHurlSpecific = it == '{'.toInt() || it == '}'.toInt()
        val isQuery = it == '%'.toInt()
        isReserved || isUnreserved || isQuery || isHurlSpecific
    }
    if (url == null || url.isEmpty()) {
        error = SyntaxError("url is expected", position)
        return null
    }
    return Url(begin = begin, value = url.string(), end = position)
}

internal fun HurlParser.valueString(): HString? {
    val begin = position.copy()

    val cp0 = peek() ?: return HString(begin = begin, end = position, value = "", text = "")
    if (cp0.isAsciiSpace) {
        error = SyntaxError("invalid unquoted-string-value", position)
        return null
    }

    var value = ""
    var end = position.copy()
    var spaces = ""

    while (true) {
        var cp = read()
        if (cp == null || cp == '#'.toInt() || cp.isNewLine) {
            rewindTo(pos = end)
            break
        } else if (cp.isAsciiSpace) {
            spaces += cp.codePointToString()
        } else if (cp == '\\'.toInt()) {
            value += spaces
            spaces = ""
            cp = read()
            when (cp) {
                null -> {
                    error = SyntaxError("invalid unquoted-string-value", position)
                    return null
                }
                '\\'.toInt() -> value += "\\"
                '#'.toInt() -> value += "#"
                'b'.toInt() -> value += "\b"
                'n'.toInt() -> value += "\n"
                'r'.toInt() -> value += "\r"
                't'.toInt() -> value += "\t"
                'u'.toInt() -> {
                    val unicode = unicodeChar() ?: return null
                    value += unicode.codePointToString()
                }
                else -> {
                    error = SyntaxError("invalid escape char ${cp.codePointToString()}", position)
                    return null
                }
            }
            end = position.copy()
        } else {
            value += spaces
            spaces = ""
            value += cp.codePointToString()
            end = position.copy()
        }
    }
    val text = buffer.slice(begin.offset, position.offset).string()
    return HString(begin = begin, end = position, value = value, text = text)
}

internal fun HurlParser.variableQuery(): VariableQuery? {
    val begin = position.copy()

    val type = queryType("variable") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val variable = quotedString() ?: return null

    return VariableQuery(begin = begin, end = position, type = type, spaces = spaces, variable = variable)
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

internal fun HurlParser.xPathQuery(): XPathQuery? {
    val begin = position.copy()

    val type = queryType("xpath") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null

    return XPathQuery(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}