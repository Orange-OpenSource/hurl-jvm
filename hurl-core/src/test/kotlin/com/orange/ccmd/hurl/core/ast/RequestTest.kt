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

class RequestTest {

    @Test
    fun `parse cookie-section with success`() {
        val text = """
            [Cookies]
            cookie1: valueA
            HTTP/1.0 200
            """.trimIndent()
        val parser = HurlParser(text)
        val node = parser.cookiesSection()
        assertNotNull(node)
        assertNull(parser.error)
    }

    @Test
    fun `parse method with success`() {
        val text = "DELETE http://example.org"
        val parser = HurlParser(text)
        val node = parser.method()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals("DELETE", node.value)
    }

    @Test
    fun `multipart-form-data-section`() {
        val text = """
            [MultipartFormData]
            a: toto
            filea: file,/tmp/tata/toto.bin; # some comments
            b: tutu
            c: tata
            # sime comments
            fileb: file,/tmp/toto.bin; text/plain # some comments
            HTTP/1.0 200
            """.trimIndent()
        val parser = HurlParser(text)
        val node = parser.multipartFormDataSection()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals(node.params.size, 3)
        assertEquals(node.fileParams.size, 2)
    }

    @TestFactory
    fun `parse query-params-section with success`() = listOf(
        """
            
            [QueryStringParams]
            	q : valueA # Some comment on query param A
            	id : valueB
            """.trimIndent() to 2,
        """
            # Some comment on query param.
            [QueryStringParams]
            a: b
            """.trimIndent() to 1
    ).map { (text, count) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.queryStringParamsSection()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(count, node.params.size)
        }
    }

    @TestFactory
    fun `parse request with success`() = listOf(
        """
            GET http://example.com
            """.trimIndent(),
        """
            # Some comments
            # Bla bla bal
            
            POST https://example.com?id=1234 # Nothing to say
            toto: tata
            tutu: tata
            {
                "enabled": true
            }
            """.trimIndent(),
        """
            POST http://{{host}}
            [1,2,3]
        """.trimIndent()
    ).map { text ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.request()
            assertNotNull(node)
            assertNull(parser.error)
        }
    }

    @TestFactory
    fun `parse url with success`() = listOf(
        "http://acmecorp.org",
        "http://sample.com?toto=tata&tutu=12",
        "http://sample.com?search=some%20value",
        "{{root_url}}/path",
        "http://acmecorp.org?sort=1,2,3"
    ).map { text ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.url()
            assertNotNull(node)
            assertEquals(node.value, text)
            assertNull(parser.error)
        }
    }
}