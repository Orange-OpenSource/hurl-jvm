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

import com.orange.ccmd.hurl.core.utils.slice

data class Position(
    var offset: Int = 0, // Offset in code points, starting at 0
    var line: Int = 1,   // Line number in code points, starting at 1
    var column: Int = 1  // Column number in code points, starting at 1
) {
    val text: String
        get() = "[$line:$column]"

    companion object {
        val zero: Position = Position()
    }
}

typealias ParseFunc<T> = Parser.() -> T?

/**
 * A Parser for a unicode code point parser.
 * @constructor create a parser from [text].
 */
open class Parser(val text: String) {

    val buffer: IntArray = text.codePoints().toArray()
    var position: Position = Position()
    var error: Error? = null
    val errors: MutableList<Error> = mutableListOf()

    /**
     * Read the next code point and increment the position.
     * If the [position] is out of bounds of the [buffer], return null.
     */
    fun read(): Int? {
        val c = peek()
        if (c == null) {
            error = EofError(position)
            return null
        }
        position.offset += 1
        if (!c.isCombining) {
            position.column += 1
        }
        if (c == '\n'.toInt()) {
            position.line += 1
            position.column = 1
        }
        return c
    }

    fun read(count: Int): IntArray? {
        val offset = position.offset
        repeat(count) {
            read()
            if (error != null) {
                return null
            }
        }
        return buffer.sliceArray(offset until position.offset)
    }

    fun readWhile(f: (Int) -> Boolean): IntArray? {
        val offset = position.offset
        while (true) {
            val c = peek()
            if (error != null || c == null) {
                // We can't read any data any more (EOF), if we haven't been able to read any
                // code point, we return an EOF error, otherwise we return the read slice.
                if (offset == position.offset) {
                    return null
                } else {
                    error = null
                    break
                }
            }
            if (f(c)) {
                read()
            } else break
        }
        return buffer.slice(offset, position.offset)
    }

    fun peek(): Int? = buffer.getOrNull(position.offset)

    fun left(): Int = buffer.size - position.offset

    fun rewindTo(pos: Position) {
        val currentError = error
        if (currentError != null) {
            errors.add(currentError)
        }
        error = null
        position = pos
    }

    fun <T> optional(f: ParseFunc<T>): T? {
        val pos = position.copy()
        val node = f()
        if (error != null || node == null) {
            rewindTo(pos)
            return null
        }
        return node
    }

    fun <T> oneOrMore(f: ParseFunc<T>): List<T>? {
        val nodes = mutableListOf<T>()
        val first = f() ?: return null
        nodes.add(first)
        while (left() > 0) {
            val node = optional { f() } ?: break
            nodes.add(node)
        }
        return nodes
    }

    fun <T> zeroOrMore(f: ParseFunc<T>): List<T> {
        val nodes = mutableListOf<T>()
        while (left() > 0) {
            val node = optional { f() } ?: break
            nodes.add(node)
        }
        return nodes
    }

    fun <T> choice(fs: List<ParseFunc<T>>): T? {
        for (f in fs) {
            val node = optional { f() }
            if (node != null) {
                return node
            }
        }
        error = SyntaxError("no valid choices at", position)
        return null
    }

    /**
     * Returns the most probable root error.
     *
     * The root error is heuristically computed from all parser [errors]:
     * - if there is only one error with the maximum offset, returns it
     * - otherwise, returns the deepest codepoint as unexpected char
     */
    val rootError: Error
        get() {
            val deepest = errors.maxByOrNull { it.position.offset } ?: throw IllegalStateException("Empty syntax error list")
            val allDeepest = errors.filter { it.position.offset == deepest.position.offset }
            return if (allDeepest.size == 1) {
                allDeepest[0]
            } else {
                // If we are at the end of file, we can't display the current
                // code point...
                if (deepest.position.offset >= buffer.size) {
                    SyntaxError("unexpected end of file", deepest.position)
                } else {
                    val cp = buffer[deepest.position.offset]
                    SyntaxError("unexpected char '${cp.codePointToString()}'", deepest.position)
                }
            }
        }
}