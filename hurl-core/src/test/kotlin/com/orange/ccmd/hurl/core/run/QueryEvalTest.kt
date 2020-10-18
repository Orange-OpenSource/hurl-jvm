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

import com.orange.ccmd.hurl.core.ast.HurlParser
import com.orange.ccmd.hurl.core.ast.bodyQuery
import com.orange.ccmd.hurl.core.ast.cookieQuery
import com.orange.ccmd.hurl.core.ast.headerQuery
import com.orange.ccmd.hurl.core.ast.jsonPathQuery
import com.orange.ccmd.hurl.core.ast.regexQuery
import com.orange.ccmd.hurl.core.ast.statusQuery
import com.orange.ccmd.hurl.core.ast.xPathQuery
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.query.InvalidQueryException
import com.orange.ccmd.hurl.core.variable.VariableJar
import com.orange.ccmd.hurl.core.variable.VariableJar.Companion.from
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class QueryEvalTest {

    @Test
    fun `evaluate status-query against a http response`() {
        val text = "status"
        val query = HurlParser(text).statusQuery()
        assertNotNull(query)
        val response = HttpResponse(
            version = "HTTP/1.1",
            code = 500,
            headers = emptyList(),
            charset = Charset.forName("utf-8"),
            mimeType = "text/plain",
            body = ByteArray(0)
        )
        val expected = QueryNumberResult(500)
        assertEquals(expected, query.eval(response))
    }

    @Test
    fun `evaluate header-query against a http response`() {
        val text = """header "key2""""
        val query = HurlParser(text).headerQuery()
        assertNotNull(query)
        val response = HttpResponse(
            version = "HTTP/1.1",
            code = 200,
            headers = listOf("key1" to "value1", "key2" to "value2", "key3" to "value3", "key3" to "value3bis"),
            charset = Charset.forName("utf-8"),
            mimeType = "text/plain",
            body = ByteArray(0)
        )
        val expected = QueryStringResult("value2")
        assertEquals(expected, query.eval(response))
    }

    @Test
    fun `evaluate cookie-query against a http response succeeded`() {
        val text = """cookie "id""""
        val query = HurlParser(text).cookieQuery()
        assertNotNull(query)
        val response = HttpResponse(
            version = "HTTP/1.1",
            code = 200,
            headers = listOf(
                "Connection" to "close",
                "Date" to "Tue, 21 Apr 2020 10:02:14 GMT",
                "Last-Modified" to "Tue, 21 Apr 2020 06:19:57 GMT",
                "Content-Type" to "application/javascript",
                "Set-Cookie" to "sessionId=38afes7a8",
                "Set-Cookie" to "id=a3fWa; Expires=Wed, 21 Oct 2015 07:28:00 GMT",
                "Location" to "http://sample.com"
            ),
            charset = Charset.forName("utf-8"),
            mimeType = "text/plain",
            body = ByteArray(0)
        )
        val expected = QueryStringResult("a3fWa")
        assertEquals(expected, query.eval(response = response, variables = VariableJar()))
    }

    @Test
    fun `evaluate cookie-query against a http response failed`() {
        val text = """cookie "id""""
        val query = HurlParser(text).cookieQuery()
        assertNotNull(query)
        val response = HttpResponse(
            version = "HTTP/1.1",
            code = 200,
            headers = listOf(
                "Connection" to "close",
                "Date" to "Tue, 21 Apr 2020 10:02:14 GMT",
                "Location" to "http://sample.com"
            ),
            charset = Charset.forName("utf-8"),
            mimeType = "text/plain",
            body = ByteArray(0)
        )
        val expected = QueryNoneResult
        assertEquals(expected, query.eval(response = response, variables = VariableJar()))
    }

    @Test
    fun `evaluate body-query against a http response succeeded`() {
        val text = "body"
        val query = HurlParser(text).bodyQuery()
        assertNotNull(query)
        val response = HttpResponse(
            version = "HTTP/1.1",
            code = 200,
            headers = emptyList(),
            charset = Charset.forName("UTF-8"),
            mimeType = "text/plain",
            body = "café".trimIndent().toByteArray()
        )
        val expected = QueryStringResult("café")
        assertEquals(expected, query.eval(response))
    }

    @Test
    fun `evaluate body-query against a http response failed because of bad charset`() {
        val text = "body"
        val query = HurlParser(text).bodyQuery()
        assertNotNull(query)
        val response = HttpResponse(
            version = "HTTP/1.1",
            code = 200,
            headers = emptyList(),
            charset = Charset.forName("UTF-8"),
            mimeType = "text/plain",
            body = "café".toByteArray(charset = Charset.forName("ISO-8859-1"))
        )
        assertThrows<InvalidQueryException> { query.eval(response) }
    }

    @TestFactory
    fun `evaluate xpath-query against a http response failed because of bad charset`() = listOf(
        "//p" to QueryNodeSetResult(size = 2),
        "count(//p)" to QueryNumberResult(2.0),
        "//div" to QueryNodeSetResult(size = 0),
        "normalize-space(//{{tag}})" to QueryStringResult("My First Heading"),
        "boolean(count(//h2))" to QueryBooleanResult(false)
    ).map { (expr, expectedResult) ->
        DynamicTest.dynamicTest(expr) {
            val variables = from(mapOf("tag" to "h1"))
            val html = """
                <!DOCTYPE html>
                <html>
                <body>
                    <h1>My First Heading</h1>
                        <p>My first paragraph.</p>
                        <p>My second paragraph.</p>
                </body>
                </html>
            """.trimIndent()
            val text = """xpath "$expr""""
            val query = HurlParser(text).xPathQuery()
            assertNotNull(query)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "text/html",
                body = html.toByteArray()
            )
            assertEquals(expectedResult, query.eval(response = response, variables = variables))
        }
    }

    @TestFactory
    fun `evaluate json-query against a http response failed because of bad charset`() = listOf(
        "$.success" to QueryBooleanResult(false),
        "$.{{field}}" to QueryBooleanResult(false),
        "$.errors" to QueryListResult(listOf(
            mapOf("id" to "error1"), mapOf("id" to "error2")
        )),
        "$.warnings" to QueryListResult(emptyList()),
        "$.toto" to QueryNoneResult,
        "$.errors[0].id" to QueryStringResult("error1"),
        "$.errors[0]['id']" to QueryStringResult("error1"),
        "$.count" to QueryNumberResult(13.5)
    ).map { (expr, expectedResult) ->
        DynamicTest.dynamicTest(expr) {
            val variables = from(mapOf("field" to "success"))
            val json = """
                {
                    "success":false,
                    "count": 13.5,
                    "errors":[{"id":"error1"},{"id":"error2"}], 
                    "warnings": []
                }
            """.trimIndent()
            val text = """jsonpath "$expr""""
            val query = HurlParser(text).jsonPathQuery()
            assertNotNull(query)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "application/json",
                body = json.toByteArray()
            )
            assertEquals(expectedResult, query.eval(response = response, variables = variables))
        }
    }

    @TestFactory
    fun `evaluate regex-query against a http response`() = listOf(
        """<title>(.*)<\\/title>""" to QueryStringResult("This is a title")
    ).map { (expr, expectedResult) ->
        DynamicTest.dynamicTest(expr) {
            val variables = from(mapOf("tag" to "h1"))
            val html = """
                <html>
                  <head>
                    <title>This is a title</title>
                  </head>
                  <body>
                    <p>Hello world!</p>
                  </body>
                </html>
            """.trimIndent()
            val text = """regex "$expr""""
            val query = HurlParser(text).regexQuery()
            assertNotNull(query)
            val response = HttpResponse(
                version = "HTTP/1.1",
                code = 200,
                headers = emptyList(),
                charset = Charset.forName("UTF-8"),
                mimeType = "text/html",
                body = html.toByteArray()
            )
            assertEquals(expectedResult, query.eval(response = response, variables = variables))
        }
    }
}