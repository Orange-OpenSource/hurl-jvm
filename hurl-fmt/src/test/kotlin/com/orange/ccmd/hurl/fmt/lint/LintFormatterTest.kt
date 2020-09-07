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

package com.orange.ccmd.hurl.fmt.lint

import com.orange.ccmd.hurl.core.ast.HurlParser
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

internal class LintFormatterTest {

    @Test
    fun `lint headers leading spaces`() {
        val input = """
            GET http://example.org
            
            HTTP/1.1 302
            [Asserts]
                header "Location" equals "http://example.org.login"
            """.trimIndent()

        """
            GET http://example.org
            
            HTTP/1.1 302
            [Asserts]
            header "Location" equals "http://example.org.login"
            """.trimIndent()

        val parser = HurlParser(text = input)
        val hurlFile = parser.parse()
        assertNotNull(hurlFile)

        LintFormatter()
        //val formatted = formatter.format(hurlFile)
        //assertEquals(expected, formatted)
    }
}