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

package com.orange.ccmd.hurl.core.run

import com.orange.ccmd.hurl.core.ast.AnyStatusValue
import com.orange.ccmd.hurl.core.ast.Assert
import com.orange.ccmd.hurl.core.ast.HurlParser
import com.orange.ccmd.hurl.core.ast.IntStatusValue
import com.orange.ccmd.hurl.core.ast.Status
import com.orange.ccmd.hurl.core.ast.Version
import com.orange.ccmd.hurl.core.ast.assert
import com.orange.ccmd.hurl.core.ast.body
import com.orange.ccmd.hurl.core.ast.header
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.parser.Position.Companion.zero
import com.orange.ccmd.hurl.core.variable.VariableJar
import com.orange.ccmd.hurl.core.variable.VariableJar.Companion.from
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class AssertTest {

    private fun assert(text: String): Assert {
        return HurlParser(text).assert() ?: throw IllegalArgumentException()
    }

    @Test
    fun `assert of wildcard version succeeded`() {
        val specVersion = Version(begin = zero, end = zero, value = "HTTP/*")
        val realVersion = "HTTP/1.0"
        val result = specVersion.checkVersion(realVersion)
        assertTrue(result.succeeded)
    }

    @Test
    fun `assert of specific version succeeded`() {
        val specVersion = Version(begin = zero, end = zero, value = "HTTP/2.0")
        val realVersion = "HTTP/2.0"
        val result = specVersion.checkVersion(realVersion)
        assertTrue(result.succeeded)
    }

    @Test
    fun `assert of specific version failed`() {
        val specVersion = Version(begin = zero, end = zero, value = "HTTP/1.1")
        val realVersion = "HTTP/1.0"
        val result = specVersion.checkVersion(realVersion)
        assertFalse(result.succeeded)
        assertEquals(
            """ 
            assert http version equals failed
              actual:   HTTP/1.0
              expected: HTTP/1.1
            """.trimIndent(),
            result.message
        )
    }

    @Test
    fun `assert of status code succeeded`() {
        val specStatus = Status(begin = zero, end = zero, value = IntStatusValue(500), text = "500")
        val realStatus = 500
        val result = specStatus.checkStatusCode(realStatus)
        assertTrue(result.succeeded)
    }

    @Test
    fun `assert of wildcard status code succeeded`() {
        val specStatus = Status(begin = zero, end = zero, value = AnyStatusValue, text = "*")
        val realStatus = 432
        val result = specStatus.checkStatusCode(realStatus)
        assertTrue(result.succeeded)
    }

    @Test
    fun `assert of status code failed`() {
        val specStatus = Status(begin = zero, end = zero, value = IntStatusValue(200), text = "200")
        val realStatus = 404
        val result = specStatus.checkStatusCode(realStatus)
        assertFalse(result.succeeded)
        assertEquals(
            """ 
            assert status code equals failed
              actual:   404
              expected: 200
            """.trimIndent(),
            result.message
        )
    }

    @Test
    fun `assert of header succeeded`() {
        val hurl = "x-key1: value 1"
        val parser = HurlParser(hurl)
        val specHeader = parser.header()
        assertNotNull(specHeader)

        val realHeaders = listOf("x-key0" to "value0", "x-key1" to "value 0", "x-key1" to "value 1")
        val result = specHeader.checkHeader(headers = realHeaders, variables = from(emptyMap()))
        assertTrue(result.succeeded)
    }

    @Test
    fun `assert of header with variable succeeded`() {
        val hurl = "x-key1: {{MY_VAR}}"
        val parser = HurlParser(hurl)
        val specHeader = parser.header()
        assertNotNull(specHeader)

        val realHeaders = listOf("x-key0" to "value0", "x-key1" to "value 0", "x-key1" to "value 1")
        val result = specHeader.checkHeader(
            headers = realHeaders,
            variables = from(mapOf("ID" to "345", "MY_VAR" to "value 0"))
        )
        assertTrue(result.succeeded)
    }

    @Test
    fun `assert of header with undefined variable failed`() {
        val hurl = "x-key1: {{MY_VAR}}"
        val parser = HurlParser(hurl)
        val specHeader = parser.header()
        assertNotNull(specHeader)

        val realHeaders = listOf("x-key0" to "value0", "x-key1" to "value 0", "x-key1" to "value 1")
        val result = specHeader.checkHeader(headers = realHeaders, variables = from(mapOf("ID" to "345")))
        assertTrue(result is InvalidVariableResult)
        assertFalse(result.succeeded)
    }

    @Test
    fun `assert of header failed when header name not present`() {
        val hurl = "app: some_app"
        val parser = HurlParser(hurl)
        val specHeader = parser.header()
        assertNotNull(specHeader)

        val realHeaders = listOf("x-key0" to "value0", "x-key1" to "value0", "x-key1" to "value1")
        val result = specHeader.checkHeader(headers = realHeaders, variables = VariableJar())
        assertFalse(result.succeeded)
        assertEquals(
            """ 
            assert header equals failed
              actual:
              expected: some_app
            """.trimIndent(),
            result.message
        )
    }

    @Test
    fun `assert of header failed when header value not present`() {
        val hurl = "app: value2"
        val parser = HurlParser(hurl)
        val specHeader = parser.header()
        assertNotNull(specHeader)

        val realHeaders = listOf("app" to "value0", "app" to "value1")
        val result = specHeader.checkHeader(headers = realHeaders, variables = VariableJar())
        assertFalse(result.succeeded)
        assertEquals(
            """ 
            assert header equals failed
              actual:   value0, value1
              expected: value2
            """.trimIndent(),
            result.message
        )
    }

    @Test
    fun `assert of body equals with variables succeeded`() {
        val hurl = """
            {
                "id": "{{my-id}}",
                "state": "RUNNING",
                "enabled": true
            }
        """.trimIndent()
        val variables = from(mapOf("my-id" to "123456", "name" to "dude"))
        val fileRoot = File("")
        val realBody = """
            {
                "id": "123456",
                "state": "RUNNING",
                "enabled": true
            }
        """.trimIndent().toByteArray()
        val parser = HurlParser(hurl)
        val specBody = parser.body()
        assertNotNull(specBody)

        val result = specBody.checkBodyContent(body = realBody, variables = variables, fileRoot = fileRoot)
        assertTrue(result.succeeded)
    }

    @Test
    fun `assert of body equals failed`() {
        val text = "Welcome"
        val text64 = Base64.getEncoder().encodeToString(text.toByteArray())
        val hurl = "base64,$text64;"

        val fileRoot = File("")
        val realBody = "Zelcome".toByteArray()
        val parser = HurlParser(hurl)
        val specBody = parser.body()
        assertNotNull(specBody)

        val result = specBody.checkBodyContent(body = realBody, variables = VariableJar(), fileRoot = fileRoot)
        assertFalse(result.succeeded)
        assertEquals(
            """ 
            assert body equals failed
              actual:   Zelcome
              expected: Welcome
            """.trimIndent(),
            result.message
        )
    }

    @TestFactory
    fun `evaluate assert jsonpath exist`() = listOf(
        Triple("""jsonpath "$.warnings" exists""", true, """
                                                            assert jsonpath exists succeeded
                                                              actual:   list()
                                                              expected: anything
                                                            """.trimIndent()),
        Triple("""jsonpath "$.warnings" not exists""", false, """
                                                            assert jsonpath not exists failed
                                                              actual:   list()
                                                              expected: 
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" exists""", true, """
                                                            assert jsonpath exists succeeded
                                                              actual:   string <running>
                                                              expected: anything
                                                            """.trimIndent()),
        Triple("""jsonpath "$.success" not exists""", false, """
                                                            assert jsonpath not exists failed
                                                              actual:   boolean <false>
                                                              expected: 
                                                            """.trimIndent()),
        Triple("""jsonpath "$.toto" not exists""", true, """
                                                            assert jsonpath not exists succeeded
                                                              actual:   
                                                              expected: 
                                                            """.trimIndent())
    ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "application/json",
                body = """
                    {
                        "success": false,
                        "count": 13.5,
                        "errors":[{"id":"error1"},{"id":"error2"}], 
                        "warnings": [],
                        "state": "running"
                    }
                """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

    @TestFactory
    fun `evaluate assert xpath exist`() = listOf(
        Triple("""xpath "//h1" exists""", true, """
                                                    assert xpath exists succeeded
                                                      actual:   nodeset(size=1)
                                                      expected: anything
                                                    """.trimIndent()),
        Triple("""xpath "//p" not exists""", false, """
                                                        assert xpath not exists failed
                                                          actual:   nodeset(size=4)
                                                          expected: 
                                                        """.trimIndent()),
        Triple("""xpath "//toto" not exists""", true, """
                                                            assert xpath not exists succeeded
                                                              actual:   nodeset(size=0)
                                                              expected: 
                                                            """.trimIndent())
    ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "text/xml",
                body = """
                <body>
                    <h1>Title</h1>
                    <div id="one"><p>toto</p></div>
                    <div id="two">tutu</div>
                    <div id="three"><p>tata</p></div>
                    <p></p>
                    <p></p>
                </body>
            """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

    @TestFactory
    fun `evaluate assert jsonpath matches`() = listOf(
        Triple("""jsonpath "$.warnings" matches ".*"""", false, """
                                                            assert jsonpath matches failed
                                                              actual:   list()
                                                              expected: matches string <.*>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.warnings" not matches ".*"""", false, """
                                                            assert jsonpath not matches failed
                                                              actual:   list()
                                                              expected: doesn't match string <.*>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" matches "run"""", true, """
                                                            assert jsonpath matches succeeded
                                                              actual:   string <running>
                                                              expected: matches string <run>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" not matches "\\d+"""", true, """
                                                            assert jsonpath not matches succeeded
                                                              actual:   string <running>
                                                              expected: doesn't match string <\d+>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.success" matches "false"""", false, """
                                                            assert jsonpath matches failed
                                                              actual:   boolean <false>
                                                              expected: matches string <false>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.toto" matches ".*"""", false, """
                                                            assert jsonpath matches failed
                                                              actual:   
                                                              expected: matches string <.*>
                                                            """.trimIndent())
    ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "application/json",
                body = """
                    {
                        "success": false,
                        "count": 13.5,
                        "errors":[{"id":"error1"},{"id":"error2"}], 
                        "warnings": [],
                        "state": "running"
                    }
                """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

    @TestFactory
    fun `evaluate assert jsonpath starts with`() = listOf(
        Triple("""jsonpath "$.warnings" startsWith "something"""", false, """
                                                            assert jsonpath startsWith failed
                                                              actual:   list()
                                                              expected: starts with string <something>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.warnings" not startsWith "something"""", false, """
                                                            assert jsonpath not startsWith failed
                                                              actual:   list()
                                                              expected: doesn't start with string <something>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" startsWith "run"""", true, """
                                                            assert jsonpath startsWith succeeded
                                                              actual:   string <running>
                                                              expected: starts with string <run>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" not startsWith "stop"""", true, """
                                                            assert jsonpath not startsWith succeeded
                                                              actual:   string <running>
                                                              expected: doesn't start with string <stop>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.success" startsWith "f"""", false, """
                                                            assert jsonpath startsWith failed
                                                              actual:   boolean <false>
                                                              expected: starts with string <f>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.toto" startsWith "anything"""", false, """
                                                            assert jsonpath startsWith failed
                                                              actual:   
                                                              expected: starts with string <anything>
                                                            """.trimIndent())
    ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "application/json",
                body = """
                    {
                        "success": false,
                        "count": 13.5,
                        "errors":[{"id":"error1"},{"id":"error2"}], 
                        "warnings": [],
                        "state": "running"
                    }
                """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

    @TestFactory
    fun `evaluate assert jsonpath contains`() = listOf(
        Triple("""jsonpath "$.warnings" contains "something"""", false, """
                                                            assert jsonpath contains failed
                                                              actual:   list()
                                                              expected: contains string <something>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.warnings" not contains "something"""", false, """
                                                            assert jsonpath not contains failed
                                                              actual:   list()
                                                              expected: doesn't contain string <something>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" contains "unni"""", true, """
                                                            assert jsonpath contains succeeded
                                                              actual:   string <running>
                                                              expected: contains string <unni>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" not contains "stop"""", true, """
                                                            assert jsonpath not contains succeeded
                                                              actual:   string <running>
                                                              expected: doesn't contain string <stop>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.success" contains "f"""", false, """
                                                            assert jsonpath contains failed
                                                              actual:   boolean <false>
                                                              expected: contains string <f>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.toto" contains "anything"""", false, """
                                                            assert jsonpath contains failed
                                                              actual:   
                                                              expected: contains string <anything>
                                                            """.trimIndent())
    ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "application/json",
                body = """
                    {
                        "success": false,
                        "count": 13.5,
                        "errors":[{"id":"error1"},{"id":"error2"}], 
                        "warnings": [],
                        "state": "running"
                    }
                """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

    @TestFactory
    fun `evaluate assert jsonpath count equals`() = listOf(
        Triple("""jsonpath "$.warnings" countEquals 0""", true, """
                                                            assert jsonpath countEquals succeeded
                                                              actual:   list()
                                                              expected: count equals 0.0
                                                            """.trimIndent()),
        Triple("""jsonpath "$.errors" not countEquals 3""", true, """
                                                            assert jsonpath not countEquals succeeded
                                                              actual:   list({id=error1}, {id=error2})
                                                              expected: count doesn't equals 3.0
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" countEquals 1""", false, """
                                                            assert jsonpath countEquals failed
                                                              actual:   string <running>
                                                              expected: count equals 1.0
                                                            """.trimIndent()),
        Triple("""jsonpath "$.success" countEquals 1""", false, """
                                                            assert jsonpath countEquals failed
                                                              actual:   boolean <false>
                                                              expected: count equals 1.0
                                                            """.trimIndent()),
        Triple("""jsonpath "$.toto" countEquals 18""", false, """
                                                            assert jsonpath countEquals failed
                                                              actual:   
                                                              expected: count equals 18.0
                                                            """.trimIndent())
    ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "application/json",
                body = """
                    {
                        "success": false,
                        "count": 13.5,
                        "errors":[{"id":"error1"},{"id":"error2"}], 
                        "warnings": [],
                        "state": "running"
                    }
                """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

    @TestFactory
    fun `evaluate assert jsonpath includes`() = listOf(
        Triple("""jsonpath "$.fruits" includes "lemon"""", true, """
                                                            assert jsonpath includes succeeded
                                                              actual:   list(apple, banana, lemon)
                                                              expected: include string <lemon>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.warnings" not includes 3""", true, """
                                                            assert jsonpath not includes succeeded
                                                              actual:   list()
                                                              expected: doesn't include number <3.0>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.ids" includes 567""", true, """
                                                            assert jsonpath includes succeeded
                                                              actual:   list(234, 567, 45, 789)
                                                              expected: include number <567.0>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.ids" includes 1""", false, """
                                                            assert jsonpath includes failed
                                                              actual:   list(234, 567, 45, 789)
                                                              expected: include number <1.0>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.states" not includes true""", false, """
                                                            assert jsonpath not includes failed
                                                              actual:   list(true, true, true)
                                                              expected: doesn't include boolean <true>
                                                            """.trimIndent()),
    ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "application/json",
                body = """
                    {
                        "fruits": ["apple", "banana", "lemon"],
                        "warnings": [],
                        "ids": [234, 567, 45, 789],
                        "states": [true, true, true]
                    }
                """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

    @TestFactory
    fun `evaluate assert xpath count equals`() = listOf(
        Triple("""xpath "//div" countEquals 3""", true, """
                                                            assert xpath countEquals succeeded
                                                              actual:   nodeset(size=3)
                                                              expected: count equals 3.0
                                                            """.trimIndent()),
        Triple("""xpath "//p" not countEquals 0""", true, """
                                                            assert xpath not countEquals succeeded
                                                              actual:   nodeset(size=4)
                                                              expected: count doesn't equals 0.0
                                                            """.trimIndent()),
        Triple("""xpath "//toto" countEquals 0""", true, """
                                                            assert xpath countEquals succeeded
                                                              actual:   nodeset(size=0)
                                                              expected: count equals 0.0
                                                            """.trimIndent()),
        Triple("""xpath "//body" countEquals 2""", false, """
                                                            assert xpath countEquals failed
                                                              actual:   nodeset(size=1)
                                                              expected: count equals 2.0
                                                            """.trimIndent())
    ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "text/html",
                body = """
                <body>
                    <div id="one"><p>toto</p></div>
                    <div id="two">tutu</div>
                    <div id="three"><p>tata</p></div>
                    <p></p>
                    <p></p>
                </body>
            """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

    @TestFactory
    fun `evaluate assert jsonpath equals`() = listOf(
        Triple("""jsonpath "$.state" equals "running"""", true, """
                                                            assert jsonpath equals succeeded
                                                              actual:   string <running>
                                                              expected: equals string <running>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.warnings" equals "something"""", false, """
                                                            assert jsonpath equals failed
                                                              actual:   list()
                                                              expected: equals string <something>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.warnings" not equals "something"""", true, """
                                                            assert jsonpath not equals succeeded
                                                              actual:   list()
                                                              expected: doesn't equal string <something>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.toto" equals "anything"""", false, """
                                                            assert jsonpath equals failed
                                                              actual:   
                                                              expected: equals string <anything>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.count" equals 13.5""", true, """
                                                            assert jsonpath equals succeeded
                                                              actual:   number <13.5>
                                                              expected: equals number <13.5>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" not equals 13.5""", true, """
                                                            assert jsonpath not equals succeeded
                                                              actual:   string <running>
                                                              expected: doesn't equal number <13.5>
                                                            """.trimIndent()),

        Triple("""jsonpath "$.success" equals false""", true, """
                                                            assert jsonpath equals succeeded
                                                              actual:   boolean <false>
                                                              expected: equals boolean <false>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.success" not equals false""", false, """
                                                            assert jsonpath not equals failed
                                                              actual:   boolean <false>
                                                              expected: doesn't equal boolean <false>
                                                            """.trimIndent()),
        Triple("""jsonpath "$.state" equals true""", false, """
                                                            assert jsonpath equals failed
                                                              actual:   string <running>
                                                              expected: equals boolean <true>
                                                            """.trimIndent())
        ).map { (expr, succeeded, message) ->
        DynamicTest.dynamicTest(expr) {
            val assert = assert(expr)
            assertNotNull(assert)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "application/json",
                body = """
                    {
                        "success": false,
                        "count": 13.5,
                        "errors":[{"id":"error1"},{"id":"error2"}], 
                        "warnings": [],
                        "state": "running"
                    }
                """.trimIndent().toByteArray(),
                encodings = emptyList(),
                duration = 100,
            )

            val result = assert.eval(response = response, variables = VariableJar())
            assertEquals(succeeded, result.succeeded)
            assertEquals(message, result.message)
        }
    }

}