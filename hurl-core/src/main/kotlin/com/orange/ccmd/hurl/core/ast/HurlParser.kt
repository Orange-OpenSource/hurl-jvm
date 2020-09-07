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

import com.orange.ccmd.hurl.core.parser.Parser
import com.orange.ccmd.hurl.core.parser.SyntaxError
import com.orange.ccmd.hurl.core.parser.codePointToString

/**
 * HurlParser is a recursive descent parser implementing Hurl grammar
 * as specified at https://hurl.dev/docs/grammar.html.
 */

class HurlParser(text: String) : Parser(text) {
    fun parse(): HurlFile? = hurlFile()
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

internal fun HurlParser.entry(): Entry? {
    val begin = position.copy()

    val request = request() ?: return null
    val response = optional { response() }

    return Entry(begin = begin, end = position, request = request, response = response)
}

