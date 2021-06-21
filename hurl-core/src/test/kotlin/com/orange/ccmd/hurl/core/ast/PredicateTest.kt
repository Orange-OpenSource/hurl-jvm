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
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PredicateTest {

    @TestFactory
    fun `parse predicate with success`() = listOf(
        "equals \"06 15 63 36 79\"" to "equals",
        "== \"06 15 63 36 79\"" to "==",
        "equals true" to "equals",
        "== true" to "==",
        "contains \"toto\"" to "contains",
        "equals 123.0" to "equals",
        "== 123.0" to "==",
        "equals 12" to "equals",
        "== 12" to "==",
        "startsWith \"something dummy\"xxx" to "startsWith",
        "countEquals 4" to "countEquals",
        "existsxxx" to "exists",
        "greaterThan 12" to "greaterThan",
        "> 12" to ">",
        "lessThan 224" to "lessThan",
        "< 224" to "<",
        "greaterThanOrEquals 36" to "greaterThanOrEquals",
        ">= 208" to ">=",
        "lessThanOrEquals 224" to "lessThanOrEquals",
        "<= 224" to "<=",
        ).map { (text, type) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.predicateFunc()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(type, node.type.value)
        }
    }
}