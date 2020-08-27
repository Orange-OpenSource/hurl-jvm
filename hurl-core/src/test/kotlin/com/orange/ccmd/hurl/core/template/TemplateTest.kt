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

package com.orange.ccmd.hurl.core.template

import com.orange.ccmd.hurl.core.ast.Position
import com.orange.ccmd.hurl.core.run.VariableJar.Companion.from
import com.orange.ccmd.hurl.safeName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

internal class TemplateTest {

    @TestFactory
    fun `render template with suceess`() = listOf(
        Triple(
            "User id:{{id}} -> name:{{name}}, firstName:{{firstName}}",
            mapOf("id" to "1234567", "name" to "Bart", "firstName" to "Simpson"),
            "User id:1234567 -> name:Bart, firstName:Simpson"
        ),
        Triple(
            "{{name}}-{{name}}-{{name}}",
            mapOf("name" to "toto"),
            "toto-toto-toto"
        ),
        Triple(
            "abcdef",
            mapOf("id" to "1234567", "name" to "Bart", "firstName" to "Simpson"),
            "abcdef"
        ),
        Triple(
            "{{{name}}}",
            mapOf("name" to "toto"),
            "toto"
        )

    ).map { (input, variables, expected) ->
        DynamicTest.dynamicTest(input.safeName()) {
            val zero = Position()
            val result = Template.render(text = input, variables = from(variables), position = zero)
            assertEquals(expected, result)
        }
    }

    @TestFactory
    fun `fail to render template because of undefined variable`() = listOf(
        "{{id}}:{{name}}" to mapOf("id" to "1234567"),
        "{{ name }}" to mapOf("name" to "toto"),
        "{{Name}}" to mapOf("name" to "toto")
    ).map { (input, variables) ->
        DynamicTest.dynamicTest(input.safeName()) {
            val zero = Position()
            assertThrows<InvalidVariableException> {
                Template.render(
                    text = input,
                    variables = from(variables),
                    position = zero
                )
            }
        }
    }
}