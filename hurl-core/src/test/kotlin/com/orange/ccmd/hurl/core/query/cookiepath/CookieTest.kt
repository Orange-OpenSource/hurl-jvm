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

internal class CookieTest {

    @TestFactory
    fun `create cookie successfully`(): List<DynamicTest> {
        val tests = mapOf(
            "sessionId=38afes7a8"
                    to
                    Cookie(name = "sessionId", value = "38afes7a8"),
            "id=a3fWa; Expires=Wed, 21 Oct 2015 07:28:00 GMT"
                    to
                    Cookie(name = "id", value = "a3fWa", expires = "Wed, 21 Oct 2015 07:28:00 GMT"),
            "qwerty=219ffwef9w0f; Domain=somecompany.co.uk"
                    to
                    Cookie(name = "qwerty", value = "219ffwef9w0f", domain = "somecompany.co.uk"),
            "SSID=Ap4PGTEq; Domain=.localhost; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly; Path=/; SameSite=Lax"
                    to
                    Cookie(
                        name = "SSID",
                        value = "Ap4PGTEq",
                        domain = ".localhost",
                        expires = "Wed, 13 Jan 2021 22:23:01 GMT",
                        secure = true,
                        httpOnly = true,
                        path = "/",
                        sameSite = "Lax",
                    ),
        )
        return tests.map { (header, expectedValue) ->
            DynamicTest.dynamicTest(header.safeName()) {
                val ret = Cookie.fromHeader(header)
                assertEquals(expectedValue, ret)
            }
        }
    }


}