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
import com.orange.ccmd.hurl.core.parser.codePointToString
import com.orange.ccmd.hurl.core.parser.isAsciiDigit
import com.orange.ccmd.hurl.core.parser.isAsciiLetter
import com.orange.ccmd.hurl.core.parser.isAsciiSpace
import com.orange.ccmd.hurl.core.parser.isHexaLetter
import com.orange.ccmd.hurl.core.parser.isHurlTemplateControl
import com.orange.ccmd.hurl.core.parser.isNewLine
import com.orange.ccmd.hurl.core.utils.slice
import com.orange.ccmd.hurl.core.utils.string
import java.util.Base64 as JdkBase64

internal fun HurlParser.base64String(): Base64String? {
    val begin = position.copy()

    val cps = readWhile { it.isAsciiLetter || it.isAsciiDigit || it.isNewLine ||
            it == '+'.code || it == '/'.code || it == '='.code
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
    return Base64String(begin = begin, end = position.copy(), value = value, text = encoded)
}

internal fun HurlParser.body(): Body? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val bytes = bytes() ?: return null
    val lt = lineTerminator() ?: return null

    return Body(begin = begin, end = position.copy(), lts = lts, spaces = spaces, bytes = bytes, lt = lt)
}

internal fun HurlParser.bool(): Bool? {
    val begin = position.copy()

    val bools = listOf("true" to true, "false" to false)
    for ((text, value) in bools) {
        val lit = optional { literal(text) }
        if (lit != null) {
            return Bool(begin = begin, value = value, text = text, end = position.copy())
        }
    }
    error = SyntaxError("true or false is expected", position.copy())
    return null
}

internal fun HurlParser.comment(): Comment? {
    val pos = position.copy()

    literal("#") ?: return null
    readWhile { !it.isNewLine }
    val value = buffer.slice(pos.offset, position.offset).string()

    return Comment(begin = pos, end = position.copy(), value = value)
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
        end = position.copy(),
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

internal fun HurlParser.cookieValue(): CookieValue? {
    val begin = position.copy()

    // FIXME: remplacer par ascii printable
    val cookies = readWhile {
        it.isHurlTemplateControl ||
            it.isAsciiLetter ||
            it.isAsciiDigit ||
            it == ':'.code ||
            it == '/'.code ||
            it == '%'.code ||
            it == '_'.code ||
            it == '-'.code
    }
    if (cookies == null || cookies.isEmpty()) {
        error = SyntaxError("[A-Za-z0-9:/%_-] char is expected in cookie-value", position.copy())
        return null
    }
    return CookieValue(begin = begin, end = position.copy(), value = cookies.string())
}

internal fun HurlParser.expr(): Expr? {
    val begin = position.copy()

    val prefix = literal("{{") ?: return null
    val name = variableName() ?: return null
    val suffix = literal("}}") ?: return null
    val text = buffer.slice(begin.offset, position.offset).string()

    return Expr(begin = begin, end = position.copy(), prefix = prefix, name = name, suffix = suffix, text = text)
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
        end = position.copy(),
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
        end = position.copy(),
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
    if (cp0 == '-'.code || cp0 == '+'.code) {
        read()
    }
    val cps0 = readWhile { it.isAsciiDigit }
    if (cps0 == null || cps0.isEmpty()) {
        error = SyntaxError("[0-9] is expected", position.copy())
        return null
    }
    val cp1 = read()
    if (cp1 == null || cp1 != '.'.code) {
        error = SyntaxError("'.' is expected", position.copy())
        return null
    }
    val cps1 = readWhile { it.isAsciiDigit }
    if (cps1 == null || cps1.isEmpty()) {
        error = SyntaxError("[0-9] is expected", position.copy())
        return null
    }
    val text = buffer.slice(begin.offset, position.offset).string()
    val value = text.toDoubleOrNull() ?: return null
    return Number(begin = begin, end = position.copy(), value = value, text = text)
}

internal fun HurlParser.header(): Header? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val keyValue = keyValue() ?: return null
    val lt = lineTerminator() ?: return null

    return Header(
        begin = begin,
        end = position.copy(),
        lts = lts,
        spaces = spaces,
        keyValue = keyValue,
        lt = lt
    )
}

internal fun HurlParser.integer(): Number? {
    val begin = position.copy()

    val cp = peek() ?: return null
    if (cp == '-'.code || cp == '+'.code) {
        read()
    }
    val cps = readWhile { it.isAsciiDigit }
    if (cps == null || cps.isEmpty()) {
        error = SyntaxError("[0-9] is expected", position.copy())
        return null
    }
    val text = buffer.slice(begin.offset, position.offset).string()
    val value = text.toIntOrNull() ?: return null
    return Number(begin = begin, end = position.copy(), value = value.toDouble(), text = text)
}

internal fun HurlParser.keyString(): HString? {
    val begin = position.copy()

    var key = ""

    while (true) {
        var cp = peek()
        if (cp == null) {
            break
        } else if (cp.isAsciiLetter || cp.isAsciiDigit || cp == '_'.code || cp == '-'.code || cp == '.'.code) {
            read()
            key += cp.codePointToString()
        } else if (cp == '\\'.code) {
            read()
            cp = read()
            when (cp) {
                null -> {
                    error = SyntaxError("invalid key-string", position.copy())
                    return null
                }
                '#'.code -> key += "#"
                ' '.code -> key += " "
                ':'.code -> key += ":"
                '\\'.code -> key += "\\"
                'b'.code -> key += "\b"
                'n'.code -> key += "\n"
                'r'.code -> key += "\r"
                't'.code -> key += "\t"
                'u'.code -> {
                    val unicode = unicodeChar() ?: return null
                    key += unicode.codePointToString()
                }
                else -> {
                    error = SyntaxError("invalid escape char ${cp.codePointToString()}", position.copy())
                    return null
                }
            }
        } else {
            break
        }
    }

    if (position.offset == begin.offset) {
        error = SyntaxError("invalid empty key-string", position.copy())
        return null
    }

    val text = buffer.slice(begin.offset, position.offset).string()
    return HString(begin = begin, end = position.copy(), value = key, text = text)
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
        end = position.copy(),
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
    return LineTerminator(begin = begin, end = position.copy(), spaces = spaces, comment = comment, newLine = newLine)
}

internal fun HurlParser.literal(literal: String): Literal? {
    val begin = position.copy()

    val cps = literal.codePoints().toArray()
    for (i in cps.indices) {
        val c = peek()
        if (c == null) {
            val message = "'$literal' is expected, invalid eof instead of '${cps[i].codePointToString()}'"
            error = SyntaxError(message, position.copy())
            return null
        } else if (c != cps[i]) {
            val message =
                "'$literal' is expected, invalid '${c.codePointToString()}' instead of '${cps[i].codePointToString()}'"
            error = SyntaxError(message, position.copy())
            return null
        }
        read()
    }
    return Literal(begin = begin, end = position.copy(), value = literal)
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
    return HString(begin = begin, end = position.copy(), value = text, text = text)
}

internal fun HurlParser.not(): Not? {
    val begin = position.copy()

    val text = literal("not") ?: return null
    return Not(begin = begin, end = position.copy(), text = text)
}

internal fun HurlParser.newLine(): NewLine? {
    val begin = position.copy()

    when (read()) {
        '\n'.code -> { }
        '\r'.code -> {
            val cp = read()
            if (cp == null || cp != '\n'.code) {
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
    return NewLine(begin = begin, end = position.copy(), value = eol.string())
}

internal fun HurlParser.`null`(): Null? {
    val begin = position.copy()
    literal("null") ?: return null
    return Null(begin = begin, end = position.copy())
}

internal fun HurlParser.param(): Param? {
    val begin = position.copy()

    val lts = zeroOrMore { lineTerminator() }
    val spaces = zeroOrMore { space() }
    val keyValue = keyValue() ?: return null
    val lt = lineTerminator() ?: return null

    return Param(
        begin = begin,
        end = position.copy(),
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
                it == '.'.code || it == '/'.code || it == '+'.code || it == '_'.code || it == '-'.code
    }
    if (cps == null) {
        error = SyntaxError("a valid filename is expected", position.copy())
        return null
    }
    val fileName = cps.string()
    if (fileName.contains("..")) {
        error = SyntaxError("relative filename is not valid", position.copy())
        return null
    }
    return HString(begin = begin, end = position.copy(), value = fileName, text = fileName)
}

internal fun HurlParser.quotedString(): HString? {
    val begin = position.copy()

    var cp = read()
    if (cp == null || cp != '"'.code) {
        error = SyntaxError("\" is expected at quoted-string beginning", position.copy())
        return null
    }

    var value = ""

    while (true) {
        cp = read()
        if (cp == null) {
            error = SyntaxError("\" is expected at quoted-string end", position.copy())
            return null
        } else if (cp == '"'.code) {
            break
        } else if (cp == '\\'.code) {
            cp = read()
            when (cp) {
                null -> {
                    error = SyntaxError("invalid quoted-string", position.copy())
                    return null
                }
                '"'.code -> value += "\""
                '\\'.code -> value += "\\"
                'b'.code -> value += "\b"
                'n'.code -> value += "\n"
                'r'.code -> value += "\r"
                't'.code -> value += "\t"
                'u'.code -> {
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
    return HString(begin = begin, end = position.copy(), value = value, text = text)
}

internal fun HurlParser.sectionHeader(section: String): SectionHeader? {
    val begin = position.copy()
    val value = literal("[$section]")?.value ?: return null
    return SectionHeader(begin = begin, end = position.copy(), value = value)
}

internal fun HurlParser.space(): Space? {
    val begin = position.copy()

    val cp = read()
    if (cp == null || !cp.isAsciiSpace) {
        error = SyntaxError("space or tab is expected", position.copy())
        return null
    }
    return Space(begin = begin, end = position.copy(), value = cp.codePointToString())
}

internal fun HurlParser.unicodeChar(): Int? {
    // Unicode literal are {XXXX}
    var cp = read()
    if (cp == null || cp != '{'.code) {
        error = SyntaxError("{ expected, invalid unicode literal", position.copy())
        return null
    }
    val cps = readWhile { it.isAsciiDigit || it.isHexaLetter }
    if (cps == null || cps.isEmpty() || cps.size > 8) {
        error = SyntaxError("invalid unicode literal", position.copy())
        return null
    }
    cp = read()
    if (cp == null || cp != '}'.code) {
        error = SyntaxError("} expected, invalid unicode literal", position.copy())
        return null
    }
    return cps.string().toInt(radix = 16)
}

internal fun HurlParser.valueString(): HString? {
    val begin = position.copy()

    val cp0 = peek() ?: return HString(begin = begin, end = position.copy(), value = "", text = "")
    if (cp0.isAsciiSpace) {
        error = SyntaxError("invalid unquoted-string-value", position.copy())
        return null
    }

    var value = ""
    var end = position.copy()
    var spaces = ""

    while (true) {
        var cp = read()
        if (cp == null || cp == '#'.code || cp.isNewLine) {
            rewindTo(pos = end)
            break
        } else if (cp.isAsciiSpace) {
            spaces += cp.codePointToString()
        } else if (cp == '\\'.code) {
            value += spaces
            spaces = ""
            cp = read()
            when (cp) {
                null -> {
                    error = SyntaxError("invalid unquoted-string-value", position.copy())
                    return null
                }
                '\\'.code -> value += "\\"
                '#'.code -> value += "#"
                'b'.code -> value += "\b"
                'n'.code -> value += "\n"
                'r'.code -> value += "\r"
                't'.code -> value += "\t"
                'u'.code -> {
                    val unicode = unicodeChar() ?: return null
                    value += unicode.codePointToString()
                }
                else -> {
                    error = SyntaxError("invalid escape char ${cp.codePointToString()}", position.copy())
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
    return HString(begin = begin, end = position.copy(), value = value, text = text)
}

internal fun HurlParser.variableName(): VariableName? {
    val begin = position.copy()

    val name = readWhile {
            it.isAsciiLetter ||
            it.isAsciiDigit ||
            it == '_'.code ||
            it == '-'.code
    }
    if (name == null || name.isEmpty()) {
        error = SyntaxError("[A-Za-z0-9_-] char is expected in variable-name", position)
        return null
    }
    return VariableName(begin = begin, end = position.copy(), value = name.string())
}