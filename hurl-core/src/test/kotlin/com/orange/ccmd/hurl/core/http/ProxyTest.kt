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

package com.orange.ccmd.hurl.core.http

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProxyTest {

    @TestFactory
    fun `create proxy from string with success`() = listOf(
        "localhost" to Proxy(protocol = "http", host = "localhost", port = 1080),
        "127.0.0.1:3128" to Proxy(protocol = "http", host = "127.0.0.1", port = 3128),
        "http://localhost" to Proxy(protocol = "http", host = "localhost", port = 1080),
        "http://localhost:3128" to Proxy(protocol = "http", host = "localhost", port = 3128),
        "http://192.168.1.0:3128" to Proxy(protocol = "http", host = "192.168.1.0", port = 3128)
    ).map { (text, expectedProxy) ->
        DynamicTest.dynamicTest(text) {
            val proxy = Proxy.fromString(text)
            assertEquals(expectedProxy, proxy)
        }
    }

    @TestFactory
    fun `fail to create proxy from string`() = listOf(
        "https://localhost",
        "127.0.0.1:"
    ).map { text ->
        DynamicTest.dynamicTest(text) {
            assertFailsWith<IllegalArgumentException> { Proxy.fromString(text) }
        }
    }
}