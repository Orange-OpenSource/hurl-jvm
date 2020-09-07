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

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ResponseTest {

    @TestFactory
    fun `parse assert with success`() = listOf(
        "jsonpath \"\$['statusCode']\" equals 200",
        "jsonpath \"\$['statusCode']\" not equals 200",
        "xpath \"boolean(count(//div[@id='tutu']))\" equals false # Some comment",
        "xpath \"//div[@id='tutu']\" not exists",
        "header \"Location\" startsWith \"{{url}}/back\""
    ).map { text ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.assert()
            assertNotNull(node)
            assertNull(parser.error)
        }
    }


    @Test
    fun `parse captures-section with success`() {
        val text = """
            [Captures]
            token: xpath "string(//body/@data-token)" # Some dummy comments
            redirect_url_recap: header "Location"
            body: body
            test: regex "^abs$"
            toto\ tata : jsonpath "${'$'}.phoneNumbers[:1].type"
            var1: header "Location" regex "token=(.*)"  # bla bla bla
            var2: header "Content-Type"
            """.trimIndent()
        val parser = HurlParser(text)
        val node = parser.capturesSection()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals(7, node.captures.size)
    }

    @Test
    fun `parse response with success`() {
        val text = """
            HTTP/1.0 200
            GET http://localhost:8000
            
            """.trimIndent()
        val parser = HurlParser(text)
        val node = parser.response()
        assertNotNull(node)
        assertEquals(node.headers.size, 0)
        assertNull(parser.error)
    }

    @Test
    fun `parse status with success`() {
        val text = "200xxx"
        val expectedValue = 200
        val expectedText = "200"
        val parser = HurlParser(text)
        val node = parser.status()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals(expectedValue, node.value)
        assertEquals(expectedText, node.text)
    }

    @Test
    fun `fail to parse status`() {
        val text = "abcd"
        val parser = HurlParser(text)
        val node = parser.status()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @Test
    fun `parse version with success`() {
        val text = "HTTP/1.1xxx"
        val parser = HurlParser(text)
        val node = parser.version()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals("HTTP/1.1", node.value)
    }
}