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
import kotlin.test.assertEquals

internal class CookiePathTest {


    data class Test(
        val expr: String,
        val headers: List<Pair<String, String>>,
        val result: CookiePathResult,
    )

    @TestFactory
    fun `evaluate cookiepath query`(): List<DynamicTest> {
        val tests = listOf(
            Test(
                expr = "LSID",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathStringResult(value = "DQAAAKEaem_vYg")
            ),
            Test(
                expr = "LSID[Value]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathStringResult(value = "DQAAAKEaem_vYg")
            ),
            Test(
                expr = "LSID[Expires]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathStringResult(value = "Wed, 13 Jan 2021 22:23:01 GMT")
            ),
            Test(
                expr = "HSID[Max-Age]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathNumberResult(value = 2592000)
            ),
            Test(
                expr = "LSID[Domain]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathFailed
            ),
            Test(
                expr = "HSID[Domain]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathStringResult(value = ".localhost")
            ),
            Test(
                expr = "LSID[Path]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathStringResult(value = "/accounts")
            ),
            Test(
                expr = "LSID[Secure]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathUnitResult
            ),
            Test(
                expr = "LSID[HttpOnly]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathUnitResult
            ),
            Test(
                expr = "LSID[SameSite]",
                headers = listOf(
                    "toto" to "tutu",
                    "tata" to "tutu",
                    "Set-Cookie" to "LSID=DQAAAKEaem_vYg; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/accounts",
                    "Set-Cookie" to "HSID=AYQEVnDKrdst; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly; Path=/; Max-Age=2592000",
                ),
                result = CookiePathFailed
            ),
        )
        return tests.map { (expr, headers, expectedValue) ->
            DynamicTest.dynamicTest(expr.safeName()) {
                val ret = CookiePath.evaluate(expr = expr, headers = headers)
                assertEquals(expectedValue, ret)
            }
        }
    }
}