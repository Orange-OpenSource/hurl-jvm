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

package com.orange.ccmd.hurl.core.query.cookiepath

import com.orange.ccmd.hurl.safeName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class CookiePathAttributeTest {

    @TestFactory
    fun `create CookiePathAttribute`(): List<DynamicTest> {
        val tests = mapOf(
            "LSID" to CookiePathQuery(name = "LSID", attribute = CookiePathAttributeValue),
            "LSID[Value]" to CookiePathQuery(name = "LSID", attribute = CookiePathAttributeValue),
            "LSID[Expires]" to CookiePathQuery(name = "LSID", attribute = CookiePathAttributeExpires),
            "LSID[Max-Age]" to CookiePathQuery(name = "LSID", attribute = CookiePathAttributeMaxAge),
            "LSID[Domain]" to CookiePathQuery(name = "LSID", attribute = CookiePathAttributeDomain),
            "LSID[Path]" to CookiePathQuery(name = "LSID", attribute = CookiePathAttributePath),
            "LSID[HttpOnly]" to CookiePathQuery(name = "LSID", attribute = CookiePathAttributeHttpOnly),
            "LSID[SameSite]" to CookiePathQuery(name = "LSID", attribute = CookiePathAttributeSameSite),
        )
        return tests.map { (expr, expectedValue) ->
            DynamicTest.dynamicTest(expr.safeName()) {
                val ret = CookiePathQuery.fromString(expr)
                assertEquals(expectedValue, ret)
            }
        }
    }

    @TestFactory
    fun `fail to create CookiePathAttribute`(): List<DynamicTest> {
        val tests = listOf(
            "abcd[",
            "toto tata[]"
        )
        return tests.map { expr ->
            DynamicTest.dynamicTest(expr.safeName()) {
                assertThrows<IllegalArgumentException> { CookiePathQuery.fromString(expr) }
            }
        }
    }

}