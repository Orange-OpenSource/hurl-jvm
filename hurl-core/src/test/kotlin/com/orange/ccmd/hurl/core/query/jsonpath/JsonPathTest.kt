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

package com.orange.ccmd.hurl.core.query.jsonpath

import com.orange.ccmd.hurl.safeName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class JsonPathTest {

    val json = """{
    "store": {
        "book": [
            {
                "category": "reference",
                "author": "Nigel Rees",
                "title": "Sayings of the Century",
                "price": 8.95
            },
            {
                "category": "fiction",
                "author": "Evelyn Waugh",
                "title": "Sword of Honour",
                "price": 12.99
            },
            {
                "category": "fiction",
                "author": "Herman Melville",
                "title": "Moby Dick",
                "isbn": "0-553-21311-3",
                "price": 8.99
            },
            {
                "category": "fiction",
                "author": "J. R. R. Tolkien",
                "title": "The Lord of the Rings",
                "isbn": "0-395-19395-8",
                "price": 22.99
            },
            {
                "category": "fiction",
                "author": null,
                "title": "undefined",
                "isbn": "x-xxx-xxxxx-x",
                "price": 1000
            }

        ],
        "bicycle": {
            "color": "red",
            "price": 19.95
        }
    },
    "expensive": true
}"""

    @TestFactory
    fun `evaluate jsonpath expression`(): List<DynamicTest> {
        val tests = listOf(
            "$.store.book[*].author" to JsonPathOk(
                result = toJson(
                    listOf(
                        "Nigel Rees",
                        "Evelyn Waugh",
                        "Herman Melville",
                        "J. R. R. Tolkien",
                        null
                    )
                )
            ),
            "$..author" to JsonPathOk(
                result = toJson(
                    listOf(
                        "Nigel Rees",
                        "Evelyn Waugh",
                        "Herman Melville",
                        "J. R. R. Tolkien",
                        null
                    )
                )
            )/*,
            "$.store.*" to JsonPathListResult(
                value =
                listOf(
                    listOf(
                        mapOf(
                            "category" to "reference",
                            "author" to "Nigel Rees",
                            "title" to "Sayings of the Century",
                            "price" to 8.95
                        ),
                        mapOf(
                            "category" to "fiction",
                            "author" to "Evelyn Waugh",
                            "title" to "Sword of Honour",
                            "price" to 12.99
                        ),
                        mapOf(
                            "category" to "fiction",
                            "author" to "Herman Melville",
                            "title" to "Moby Dick",
                            "isbn" to "0-553-21311-3",
                            "price" to 8.99
                        ),
                        mapOf(
                            "category" to "fiction",
                            "author" to "J. R. R. Tolkien",
                            "title" to "The Lord of the Rings",
                            "isbn" to "0-395-19395-83",
                            "price" to 22.99
                        ),
                        mapOf(
                            "category" to "fiction",
                            "author" to null,
                            "title" to "undefined",
                            "isbn" to "x-xxx-xxxxx-x",
                            "price" to 1000
                        )
                    ),
                    mapOf(
                        "color" to "red",
                        "price" to 19.95
                    )
                )
            ),
            "$.store..price" to JsonPathListResult(value = listOf()),
            "$..book[2]" to JsonPathObjectResult(
                value = mapOf(
                    "category" to "fiction",
                    "author" to "Herman Melville",
                    "title" to "Moby Dick",
                    "isbn" to "0-553-21311-3",
                    "price" to 8.99
                )
            ),
            "$..book[2].title" to JsonPathStringResult(value = "Moby Dick"),
            "$.store.book[2].title" to JsonPathStringResult(
                value = "Moby Dick"
            ),
            "$.store.book[2].price" to JsonPathNumberResult(
                value = 8.99
            ),
            "$.expensive" to JsonPathBooleanResult(value = true),
            "$.store.book[?(@.price < 10)]" to JsonPathListResult(
                value = listOf()
            )*/
        )
        return tests.map { (expr, expectedValue) ->
            DynamicTest.dynamicTest(expr.safeName()) {
                val ret = JsonPath.evaluate(expr = expr, json = json)
                assertEquals(expectedValue, ret)
            }
        }
    }
}