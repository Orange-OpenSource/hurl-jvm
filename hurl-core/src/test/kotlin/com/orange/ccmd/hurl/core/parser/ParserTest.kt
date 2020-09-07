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
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class ParserTest {

    @Test
    fun `read one code point`() {
        val text = "Some content"
        val parser = Parser(text)

        val cp = parser.read()
        assertNotNull(cp)
        assertNull(parser.error)
        assertEquals('S'.toInt(), cp, "First code point should be equal")
        assertEquals(1, parser.position.offset, "Parser offset should be incremented")
        assertEquals(1, parser.position.line, "Parser line should be equal")
        assertEquals(2, parser.position.column, "Parser column should be incremented")
    }

    @Test
    fun `read multiple code points with a combining character`() {
        val text = "cafe\u0301"
        val parser = Parser(text)

        val cps = parser.read(5)
        assertNotNull(cps)
        assertNull(parser.error)
        assertEquals(cps.string(), "cafe\u0301")
        assertEquals(5, parser.position.offset, "Parser offset should be incremented by 5")
        assertEquals(1, parser.position.line, "Parser line should be equal")
        assertEquals(1 + 4, parser.position.column, "Parser column should be incremented by 4")
    }

    @Test
    fun `count lines from multi-lines text`() {
        val text = """
            Some multiline string:
            line 2
            line 3
            line 4
            """.trimIndent()
        val parser = Parser(text)
        while (parser.error == null) {
            parser.read()
        }
        assertTrue(parser.error is EofError)
        assertEquals(4, parser.position.line, "Parser line should be equal")
    }

    @Test
    fun `read all the buffer until end of file error`() {
        val text = "123"
        val parser = Parser(text)

        while (parser.left() > 0) { parser.read() }
        assertNull(parser.error)

        parser.read()
        assertTrue(parser.error is EofError, "Latest parser error should be end of file")
    }

    @Test
    fun `read empty string set end of file error`() {
        val text = ""
        val parser = Parser(text)
        parser.read()
        assertTrue(parser.error is EofError, "Latest parser error should be end of file")
    }

    @Test
    fun `read code points while closure is true`() {
        val text = "aaaaabcdef"
        val parser = Parser(text)

        val cps = parser.readWhile { it == 'a'.toInt() }
        assertNotNull(cps)
        assertNull(parser.error)
        assertEquals("aaaaa", cps.string())
    }

    @Test
    fun `read code points while extension call is true`() {
        fun Int.isA(): Boolean = this == 'a'.toInt()
        val text = "aaaaabcdef"
        val parser = Parser(text)

        val cps = parser.readWhile { it.isA() }
        assertNotNull(cps)
        assertNull(parser.error)
        assertEquals("aaaaa", cps.string())
    }
}