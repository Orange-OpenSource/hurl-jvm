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
import com.orange.ccmd.hurl.core.ast.Request
import com.orange.ccmd.hurl.core.ast.request
import com.orange.ccmd.hurl.core.http.Header
import com.orange.ccmd.hurl.core.run.VariableJar.Companion.from
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

internal class RequestTest {

    private fun request(text: String): Request {
        return HurlParser(text).request() ?: throw IllegalArgumentException()
    }

    @Test
    fun `create a spec HTTP request from a request node`() {

        val hurl = """
            POST https://sample.org
            # x-app-0: red
            a: blue
            b: orange
            c: {{c}}
            {
              "id": "1234",
              "running": true
            }
        """.trimIndent()

        val request = request(hurl)
        val specRequest = request.toHttpRequestSpec(
            variables = from(mapOf("c" to "yellow")),
            fileRoot = File("")
        )

        assertEquals("POST", specRequest.method)
        assertEquals("https://sample.org", specRequest.url)
        assertEquals(listOf(Header("a", "blue"), Header("b", "orange"), Header("c", "yellow")), specRequest.headers)
    }

}