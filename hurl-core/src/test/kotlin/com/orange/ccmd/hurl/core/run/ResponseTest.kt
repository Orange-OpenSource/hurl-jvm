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
import com.orange.ccmd.hurl.core.parser.Position
import com.orange.ccmd.hurl.core.ast.Response
import com.orange.ccmd.hurl.core.ast.response
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.variable.VariableJar
import com.orange.ccmd.hurl.core.variable.VariableJar.Companion.from
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ResponseTest {

    private val httpResponse = HttpResponse(
        version = "HTTP/1.1",
        code = 200,
        headers = listOf("x-app-0" to "var0", "x-app-1" to "var1", "x-app-2" to "var2"),
        charset = Charset.forName("UTF-8"),
        mimeType = "application/json",
        body = """{"state": "running", "id": "123"}""".toByteArray(),
        encodings = emptyList()
    )


    private fun response(text: String): Response {
        return HurlParser(text).response() ?: throw IllegalArgumentException()
    }

    @Test
    fun `capture variables results`() {
        val responseSpec = response(text = """
            HTTP/1.1 200
            [Captures]
            var1: jsonpath "$.state"
        """.trimIndent())
        val results = responseSpec.getCaptureResults(variables = VariableJar(), httpResponse = httpResponse)
        assertEquals(results.size, 1)
        val captureResults = results.filterIsInstance<CaptureResult>()

        assertEquals(captureResults[0].succeeded, true)
        assertEquals(captureResults[0].position, Position(offset = 24, line = 3, column = 1))
        assertEquals(captureResults[0].variable, "var1")
        assertEquals(captureResults[0].value, QueryStringResult("running"))

        val variables = results.getCaptureVariables()
        assertEquals(variables, mapOf("var1" to QueryStringResult("running")))
    }

    @Test
    fun `no capture for empty section`() {
        val responseSpec = response(text = """
            HTTP/1.1 200
        """.trimIndent())
        val results = responseSpec.getCaptureResults(variables = VariableJar(), httpResponse = httpResponse)
        assertEquals(results.size, 0)
    }

    @Test
    fun `version result`() {
        var responseSpec = response(text = "HTTP/1.1 304")
        var result = responseSpec.getCheckVersionResult(httpResponse = httpResponse)
        assertEquals(result.succeeded, true)

        responseSpec = response(text = "HTTP/1.0 304")
        result = responseSpec.getCheckVersionResult(httpResponse = httpResponse)
        assertEquals(result.succeeded, false)
    }

    @Test
    fun `status code result`() {
        var responseSpec = response(text = "HTTP/1.1 200")
        var result = responseSpec.getCheckStatusCodeResult(httpResponse = httpResponse)
        assertEquals(result.succeeded, true)

        responseSpec = response(text = "HTTP/1.1 304")
        result = responseSpec.getCheckStatusCodeResult(httpResponse = httpResponse)
        assertEquals(result.succeeded, false)
    }

    @Test
    fun `headers results`() {
        val responseSpec = response(text = """
            HTTP/1.1 200
            x-app-0: var0
            x-app-1: var1
            x-app-3: var3
        """.trimIndent())

        val results = responseSpec.getCheckHeadersResults(variables = VariableJar(), httpResponse = httpResponse)
        assertEquals(results.size, 3)
        assertEquals(results[0].succeeded, true)
        assertEquals(results[1].succeeded, true)
        assertEquals(results[2].succeeded, false)
    }

    @Test
    fun `body result`() {
        val responseSpec = response(text = """
            HTTP/1.1 200
            {"state": "running", "id": "{{ID}}"}
        """.trimIndent())

        val result = responseSpec.getCheckBodyResult(variables = from(mapOf("ID" to "123")), fileRoot = File(""), httpResponse = httpResponse)
        assertNotNull(result)
        assertEquals(result.succeeded, true)
    }

    @Test
    fun `assert results`() {
        val responseSpec = response(text = """
            HTTP/1.1 200
            [Asserts]
            jsonpath "$.toto" not exists
            jsonpath "$.state" equals "running"
            jsonpath "$.id" startsWith "1"
            jsonpath "$.id" equals "{{ID}}"
        """.trimIndent())

        val results = responseSpec.getAssertsResults(variables = from(mapOf("ID" to "123")), httpResponse = httpResponse)
        assertEquals(results.size, 4)
        assertEquals(results[0].succeeded, true)
        assertEquals(results[1].succeeded, true)
        assertEquals(results[2].succeeded, true)
        assertEquals(results[3].succeeded, true)
    }

}