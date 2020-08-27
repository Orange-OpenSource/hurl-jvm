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

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class ErrorTest {

    @Test
    fun `single deepest with one error`() {
        val errors = listOf(
            SyntaxError(message = "some error", position = Position(offset = 10))
        )
        val singleDeepest = errors.singleDeepest
        assertNotNull(singleDeepest)
        assertEquals(singleDeepest, errors[0])
    }

    @Test
    fun `single deepest with many errors`() {
        val errors = listOf(
            SyntaxError(message = "error 1", position = Position(offset = 1)),
            SyntaxError(message = "error 2", position = Position(offset = 4)),
            SyntaxError(message = "error 3", position = Position(offset = 2)),
            SyntaxError(message = "error 4", position = Position(offset = 2))
        )
        val singleDeepest = errors.singleDeepest
        assertNotNull(singleDeepest)
        assertEquals(singleDeepest, errors[1])
    }

    @Test
    fun `single deepest is null when many deepest`() {
        val errors = listOf(
            SyntaxError(message = "error 1", position = Position(offset = 1)),
            SyntaxError(message = "error 2", position = Position(offset = 4)),
            SyntaxError(message = "error 3", position = Position(offset = 4)),
            SyntaxError(message = "error 4", position = Position(offset = 2))
        )
        val singleDeepest = errors.singleDeepest
        assertNull(singleDeepest)
    }
}
