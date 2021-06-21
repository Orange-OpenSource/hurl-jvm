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

package com.orange.ccmd.hurl.core.parser

import com.orange.ccmd.hurl.core.utils.string

/**
 * Determines if the code point is a combining character.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Combining_character">Combining character</a>
 *
 * @return  <code>true</code> if the character is a Unicode combining character, <code>false</code> otherwise.
 */
internal val Int.isCombining: Boolean
    get() = (this in 0x0300..0x036f) ||
            (this in 0x1ab0..0x1aff) ||
            (this in 0x1dc0..0x1dff) ||
            (this in 0xfe20..0xfe2f)

internal val Int.isNewLine: Boolean
    get() = this == '\n'.code || this == '\r'.code

internal val Int.isAsciiSpace: Boolean
    get() = this == ' '.code || this == '\t'.code

internal val Int.isAsciiWhitespace: Boolean
    get() = isAsciiSpace || isNewLine

internal val Int.isAsciiLetter: Boolean
    get() = this in 'A'.code..'Z'.code ||
            this in 'a'.code..'z'.code

internal val Int.isHexaLetter: Boolean
    get() = this in 'A'.code..'F'.code ||
            this in 'a'.code..'f'.code

internal val Int.isAsciiDigit: Boolean
    get() = this in '0'.code..'9'.code

internal val Int.isHurlTemplateControl: Boolean
    get() = this == '{'.code || this == '}'.code

internal val Int.isSignificant: Boolean
    get() = !this.isAsciiWhitespace && this != '#'.code

internal fun Int.any(set: String): Boolean = this in set.codePoints().toArray()

internal fun Int.codePointToString(): String = intArrayOf(this).string()
