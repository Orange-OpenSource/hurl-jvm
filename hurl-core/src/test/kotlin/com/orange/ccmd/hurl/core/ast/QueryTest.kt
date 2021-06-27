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

class QueryTest {

    data class QueryTestData (
        val type: QueryTypeValue,
        val subqueryType: SubqueryTypeValue?
    )


    @TestFactory
    fun `parse query with success`() = listOf(
        """jsonpath "string(//div)" equals""" to QueryTestData(type = QueryTypeValue.JSONPATH, subqueryType = null),
        """xpath "//div" count not equals""" to QueryTestData(type = QueryTypeValue.XPATH, subqueryType = SubqueryTypeValue.COUNT),
        """xpath "//div" count equals""" to QueryTestData(type = QueryTypeValue.XPATH, subqueryType = SubqueryTypeValue.COUNT),
        """jsonpath "//div" countEquals 18""" to QueryTestData(type = QueryTypeValue.JSONPATH, subqueryType = null),
        """header "Location" regex "(\\d*)" equals""" to QueryTestData(type = QueryTypeValue.HEADER, subqueryType = SubqueryTypeValue.REGEX),
    ).map { (text, expected) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val query = parser.query()
            assertNotNull(query)
            assertNull(parser.error)
            assertEquals(expected.type, query.type.value)
        }
    }
}