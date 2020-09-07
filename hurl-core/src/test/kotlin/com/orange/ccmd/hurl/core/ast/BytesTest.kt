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

import com.orange.ccmd.hurl.safeName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BytesTest {

    @Test
    fun `parse base64 with success`() {
        val text = "base64,V2VsY29tZSBodXJsIQ==;xxxxx"
        val expectedValue = "Welcome hurl!"
        val parser = HurlParser(text)
        val node = parser.base64()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals(expectedValue, String(node.base64String.value))
    }

    @Test
    fun `fail to parse base64`() {
        val text = "base64,V2VsY29tZSBodXJsIQ=="
        val parser = HurlParser(text)
        val node = parser.base64()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @TestFactory
    fun `parse file with success`() = listOf(
        "file,tmp/tata.bin;" to "tmp/tata.bin",
        "file,  test12345678;xxxx" to "test12345678"
    ).map { (text, fileName) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.file()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(fileName, node.fileName.value)
        }
    }

    @Test
    fun `fail to parse file`() {
        val text = "file,../../secret.key;"
        val parser = HurlParser(text)
        val node = parser.file()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @TestFactory
    fun `parse json with success`() = listOf(
        Triple("\"simple string\"", 15, 0),
        Triple("true\nxxx", 4, 4),
        Triple("""{"id": 0,"selected": true}""", 26, 0),
        Triple(
            """
                {
                    "user": {
                        "name": "toto",
                        "authentified":true,
                        "id": "abcedf"
                    }
                }12345678
            """.trimIndent(), 99, 8
        ),
        Triple(
            """
            {
                "id": 0,
                "name": "Frieda",
                "picture": "images/scottish-terrier.jpeg",
                "age": 3,
                "breed": "Scottish Terrier",
                "location": "Lisco, Alabama"
            }abcdef
            """.trimIndent(), 165, 6
        ),
        Triple("""[1, 2, 3, 4]true""", 12, 4),
        Triple("{\"id\": \"cafe\u0301\"}012345", 16, 6)
    ).map { (text, expectedBytes, expectedLeft) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.json()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedBytes, node.text.toByteArray().size)
            assertEquals(expectedLeft, parser.left())
        }
    }

    @TestFactory
    fun `parse raw-string with success`() = listOf(
        "```   \nline1\nline2\nline3\n```" to "line1\nline2\nline3\n",
        "```\n\nline1\n```" to "\nline1\n",
        "```abcdef\n12345678\ntoto```xxx" to "abcdef\n12345678\ntoto"
    ).map { (text, expectedValue) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.rawString()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedValue, node.value)
        }
    }

    @Test
    fun `fail to parse raw-string`() {
        val text = "```0123456789``"
        val parser = HurlParser(text)
        val node = parser.rawString()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @TestFactory
    fun `parse xml with success`() = listOf(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<drink>café</drink>" to "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<drink>café</drink>",
        """
            <?xml version="1.0" encoding="UTF-8"?>
             <note>
                 <to>Tove</to>
                 <from>Jani</from>
                 <heading>Reminder</heading>
                 <body>Don't forget me this weekend!</body>
             </note>xxx""".trimIndent() to """
                    <?xml version="1.0" encoding="UTF-8"?>
                     <note>
                         <to>Tove</to>
                         <from>Jani</from>
                         <heading>Reminder</heading>
                         <body>Don't forget me this weekend!</body>
                     </note>""".trimIndent(),
        """<?xml version="1.0" encoding="UTF-8"?><root/>123456789""" to """<?xml version="1.0" encoding="UTF-8"?><root/>"""
    ).map { (text, xml) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.xml()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(xml, node.text)
        }
    }

    @Test
    fun `fail to parse xml`() {
        val text = "toto"
        val parser = HurlParser(text)
        val node = parser.xml()
        assertNull(node)
        assertNotNull(parser.error)
    }

}