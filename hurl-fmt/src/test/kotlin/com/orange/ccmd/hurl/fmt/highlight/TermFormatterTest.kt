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

package com.orange.ccmd.hurl.fmt.highlight

import com.orange.ccmd.hurl.core.ast.HurlParser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class TermFormatterTest {

    @Test
    fun `simple formatter`() {
        val hurl = """
            POST http://sample.org # some comments
            # Some comments before body
            base64,TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIG
            FkaXBpc2NpbmcgZWxpdC4gSW4gbWFsZXN1YWRhLCBuaXNsIHZlbCBkaWN0dW0g
            aGVuZHJlcml0LCBlc3QganVzdG8gYmliZW5kdW0gbWV0dXMsIG5lYyBydXRydW
            0gdG9ydG9yIG1hc3NhIGlkIG1ldHVzLiA=;     # some comments
            
            GET http://sample.com
            [Cookies]
            theme: light
            sessionToken: abc123
            
            # comments on end of line
            """.trimIndent()
        val parser = HurlParser(text = hurl)
        val hurlFile = parser.parse()
        assertNotNull(hurlFile)

        val formatter = TermFormatter(showWhitespaces = false)
        val text = formatter.format(hurlFile)

        // TODO: Remove any ansi escape code and test with the plain value.
        assertEquals(701, text.length)
    }
}