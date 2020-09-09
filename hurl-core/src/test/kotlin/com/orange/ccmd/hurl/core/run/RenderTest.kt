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


import com.orange.ccmd.hurl.core.ast.Cookie as CookieNode
import com.orange.ccmd.hurl.core.ast.Header as HeaderNode
import com.orange.ccmd.hurl.core.ast.HurlParser
import com.orange.ccmd.hurl.core.ast.Json
import com.orange.ccmd.hurl.core.ast.Param
import com.orange.ccmd.hurl.core.parser.Position
import com.orange.ccmd.hurl.core.parser.Position.Companion.zero
import com.orange.ccmd.hurl.core.ast.RawString
import com.orange.ccmd.hurl.core.ast.Url
import com.orange.ccmd.hurl.core.ast.Xml
import com.orange.ccmd.hurl.core.ast.containPredicate
import com.orange.ccmd.hurl.core.ast.cookie
import com.orange.ccmd.hurl.core.ast.equalStringPredicate
import com.orange.ccmd.hurl.core.ast.header
import com.orange.ccmd.hurl.core.ast.matchPredicate
import com.orange.ccmd.hurl.core.ast.param
import com.orange.ccmd.hurl.core.ast.startWithPredicate
import com.orange.ccmd.hurl.core.http.Cookie
import com.orange.ccmd.hurl.core.http.FormParam
import com.orange.ccmd.hurl.core.http.Header
import com.orange.ccmd.hurl.core.http.TextFormData
import com.orange.ccmd.hurl.core.variable.VariableJar
import com.orange.ccmd.hurl.core.variable.VariableJar.Companion.from
import com.orange.ccmd.hurl.core.utils.string
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class RenderTest {

    @Test
    fun `rendering of a json body`() {
        val json = """
            {
                "id": "anydude",
                "state": "{{state}}",
                enabled: true
            }
        """.trimIndent()
        val expected = """
            {
                "id": "anydude",
                "state": "RUNNING",
                enabled: true
            }
        """.trimIndent()
        val fileRoot = File("")
        val variables = from(mapOf("state" to "RUNNING"))

        val node = Json(begin = zero, end = zero, text = json)
        val bytes = node.toByteArray(variables, fileRoot)
        assertEquals(expected, bytes.string())
    }

    @Test
    fun `rendering of a xml body`() {
        val xml = """
            <?xml version="1.0"?>
            <catalog>
                <book id="{{book-id}}">
                    <author>Gambardella, Matthew</author>
                    <title>XML Developer's Guide</title>
                    <genre>Computer</genre>
                    <price>{{book-price}}</price>
                    <publish_date>2000-10-01</publish_date>
                    <description>An in-depth look at creating applications with XML.</description>
                </book>
            </catalog>
        """.trimIndent()
        val expected = """
            <?xml version="1.0"?>
            <catalog>
                <book id="bk101">
                    <author>Gambardella, Matthew</author>
                    <title>XML Developer's Guide</title>
                    <genre>Computer</genre>
                    <price>44.95</price>
                    <publish_date>2000-10-01</publish_date>
                    <description>An in-depth look at creating applications with XML.</description>
                </book>
            </catalog>
        """.trimIndent()
        val fileRoot = File("")
        val variables = from(mapOf("book-id" to "bk101", "book-price" to "44.95"))

        val node = Xml(begin = zero, end = zero, text = xml)
        val bytes = node.toByteArray(variables, fileRoot)

        assertEquals(expected, bytes.string())
    }

    @Test
    fun `rendering of a raw string body`() {
        val rawStringText = """
            ```
            Year,Make,Model,Description,Price
            {{year-0}},Ford,E350,"ac, abs, moon",3000.00
            {{year-1}},Chevy,"Venture ""Extended Edition"","",4900.00
            {{year-2}},Chevy,"Venture ""Extended Edition, Very Large"",,5000.00
            {{year-3}},Jeep,Grand Cherokee,"MUST SELL! air, moon roof, loaded",4799.00
            ```
        """.trimIndent()
        val rawStringValue = """
            Year,Make,Model,Description,Price
            {{year-0}},Ford,E350,"ac, abs, moon",3000.00
            {{year-1}},Chevy,"Venture ""Extended Edition"","",4900.00
            {{year-2}},Chevy,"Venture ""Extended Edition, Very Large"",,5000.00
            {{year-3}},Jeep,Grand Cherokee,"MUST SELL! air, moon roof, loaded",4799.00
        """.trimIndent()
        val expected = """
            Year,Make,Model,Description,Price
            1997,Ford,E350,"ac, abs, moon",3000.00
            1999,Chevy,"Venture ""Extended Edition"","",4900.00
            1999,Chevy,"Venture ""Extended Edition, Very Large"",,5000.00
            1996,Jeep,Grand Cherokee,"MUST SELL! air, moon roof, loaded",4799.00
        """.trimIndent()

        val zero = Position()
        val fileRoot = File("")
        val variables = from(mapOf("year-0" to "1997", "year-1" to "1999", "year-2" to "1999", "year-3" to "1996"))

        val node = RawString(begin = zero, end = zero, text = rawStringText, value = rawStringValue)
        val bytes = node.toByteArray(variables, fileRoot)
        assertEquals(expected, bytes.string())
    }

    @Test
    fun `rendering of an url`() {
        val url = "https://sample.net"
        val node = Url(begin = zero, end = zero, value=url)
        val rendered = node.toUrl(variables = VariableJar())
        assertEquals(url, rendered)
    }

    @Test
    fun `rendering of an url with variables`() {
        val url = "https://{{host}}"
        val node = Url(begin = zero, end = zero, value=url)
        val variables = from(mapOf("host" to "www.acme.com"))
        val expected = "https://www.acme.com"
        val rendered = node.toUrl(variables = variables)
        assertEquals(expected, rendered)
    }


    private fun header(kv: Pair<String, String>): HeaderNode {
        val parser = HurlParser("${kv.first}: ${kv.second}")
        return parser.header() ?: throw IllegalArgumentException()
    }

    @TestFactory
    fun `rendering of a header with variables`() = listOf(
        header("key1" to "abcdef") to Header("key1", "abcdef"),
        header("key2" to "{{value2}}") to Header("key2", "red"),
        header("key2" to "{{value3}}") to Header("key2", "yellow"),
        header("key3" to "dummy sample") to Header("key3", "dummy sample")
    ).map { (header, expected) ->
        DynamicTest.dynamicTest("${header.name}: ${header.value}") {
            val variables = from(mapOf("value2" to "red", "value3" to "yellow"))
            val rendered = header.toHeader(variables = variables)
            assertEquals(expected, rendered)
        }
    }


    private fun param(kv: Pair<String, String>): Param {
        val parser = HurlParser("${kv.first}: ${kv.second}")
        return parser.param() ?: throw IllegalArgumentException()
    }

    @TestFactory
    fun `rendering of a param`() = listOf(
        param("key1" to "abcdef") to FormParam("key1", "abcdef"),
        param("key2" to "{{value2}}") to FormParam("key2", "red"),
        param("key2" to "{{value3}}") to FormParam("key2", "yellow"),
        param("key3" to "dummy sample") to FormParam("key3", "dummy sample")
    ).map { (param, expected) ->
        DynamicTest.dynamicTest("${param.name}: ${param.value}") {
            val variables = from(mapOf("value2" to "red", "value3" to "yellow"))
            val rendered = param.toFormParam(variables = variables)
            assertEquals(expected, rendered)
        }
    }


    private fun cookie(kv: Pair<String, String>): CookieNode {
        val parser = HurlParser("${kv.first}: ${kv.second}")
        return parser.cookie() ?: throw IllegalArgumentException()
    }

    @TestFactory
    fun `rendering of a cookie`() = listOf(
        cookie("cookie1" to "abcdef") to Cookie("cookie1", "abcdef"),
        cookie("cookie2" to "{{value2}}") to Cookie("cookie2", "red"),
        cookie("cookie2" to "{{value3}}") to Cookie("cookie2", "yellow"),
        cookie("cookie3" to "cookievalue") to Cookie("cookie3", "cookievalue")
    ).map { (cookie, expected) ->
        DynamicTest.dynamicTest(cookie.name.value) {
            val variables = from(mapOf("value2" to "red", "value3" to "yellow"))
            val rendered = cookie.toCookie(variables = variables)
            assertEquals(expected, rendered)
        }
    }


    private fun textFormData(kv: Pair<String, String>): Param {
        val parser = HurlParser("${kv.first}: ${kv.second}")
        return parser.param() ?: throw IllegalArgumentException()
    }

    @TestFactory
    fun `rendering a text form data`() = listOf(
        textFormData("field1" to "value1") to TextFormData(name = "field1", value = "value1"),
        textFormData("field2" to "{{value2}}") to TextFormData(name = "field2", value = "red")
    ).map { (param, expected) ->
        DynamicTest.dynamicTest(param.name) {
            val variables = from(mapOf("value2" to "red", "value3" to "yellow"))
            val rendered = param.toFormData(variables = variables)
            assertEquals(expected, rendered)
        }
    }

    @Test
    fun `rendering of equal-predicate function value`() {
        val variables = from(mapOf("var0" to "red", "var1" to "yellow"))
        val parser = HurlParser("""equals "{{var1}}"""")
        val predFunc = parser.equalStringPredicate()
        assertNotNull(predFunc)
        assertEquals("yellow", predFunc.valueToString(variables = variables))
    }

    @Test
    fun `rendering of start-with-predicate function value`() {
        val variables = from(mapOf("var0" to "red", "var1" to "yellow"))
        val parser = HurlParser("""startsWith "{{var1}}"""")
        val predFunc = parser.startWithPredicate()
        assertNotNull(predFunc)
        assertEquals("yellow", predFunc.valueToString(variables = variables))
    }

    @Test
    fun `rendering of contain-predicate function value`() {
        val variables = from(mapOf("var0" to "red", "var1" to "yellow"))
        val parser = HurlParser("""contains "{{var1}}"""")
        val predFunc = parser.containPredicate()
        assertNotNull(predFunc)
        assertEquals("yellow", predFunc.valueToString(variables = variables))
    }

    @Test
    fun `rendering of match-predicate function value`() {
        val variables = from(mapOf("var0" to "red", "var1" to "yellow"))
        val parser = HurlParser("""matches "{{var1}}"""")
        val predFunc = parser.matchPredicate()
        assertNotNull(predFunc)
        assertEquals("yellow", predFunc.valueToString(variables = variables))
    }

}