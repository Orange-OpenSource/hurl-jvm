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

package com.orange.ccmd.hurl.cli

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ArgsParserTest {

    @Test
    fun `parse a line of positional and optional arguments`() {

        val args = listOf(
            "--proxy", "http://localhost:3128",
            "--variable", "x=1",
            "--variable", "y=2",
            "--insecure",
            "-L",
            "-i",
            "file1.hurl",
            "file2.hurl",
        ).toTypedArray()

        val (positional, options) = ArgsParser().parse(args)

        assertEquals(listOf("file1.hurl", "file2.hurl"), positional)
        assertEquals(
            Options(
                variables = mapOf("x" to "1", "y" to "2"),
                proxy = "http://localhost:3128",
                insecure = true,
                followRedirect = true,
                include = true,
            ),
            options
        )
    }
}