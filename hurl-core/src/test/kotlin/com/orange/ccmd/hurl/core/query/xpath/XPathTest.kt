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

package com.orange.ccmd.hurl.core.query.xpath

import com.orange.ccmd.hurl.core.query.InvalidQueryException
import com.orange.ccmd.hurl.safeName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.*


class XPathTest {

    @TestFactory
    fun `evaluate xpath expression`(): List<DynamicTest> {
        val html = File("src/test/resources/sample.html")
        val tests = mapOf(
            "normalize-space(//head/title)" to XPathStringResult("Toto\u00a0\u00a0Tutu"),
            "count(//div[contains(concat(' ',normalize-space(@class),' '),' pet ')])" to
                    XPathNumberResult(2.0),
            "normalize-space((//div[contains(concat(' ',normalize-space(@class),' '),' pet-title ')])[1])" to
                    XPathStringResult("Dog"),
            "normalize-space((//div[contains(concat(' ',normalize-space(@class),' '),' pet-color ')])[1])" to
                    XPathStringResult("Brown"),
            "boolean(count((//div[contains(concat(' ',normalize-space(@class),' '),' pet ')])[1]//div[contains(concat(' ',normalize-space(@class),' '),' pet-title ')]))" to
                    XPathBooleanResult(true),
            "count((//div[contains(concat(' ',normalize-space(@class),' '),' pet ')])[1]//div[contains(concat(' ',normalize-space(@class),' '),' pet-attribute ')])" to
                    XPathNumberResult(2.0),
            "boolean(count((//div[contains(concat(' ',normalize-space(@class),' '),' pet ')])[1]//div[contains(concat(' ',normalize-space(@class),' '),' pet-attribute ')]))" to
                    XPathBooleanResult(true),
            "normalize-space((//div[contains(concat(' ',normalize-space(@class),' '),' pet-title ')])[2])" to
                    XPathStringResult("Cat"),
            "normalize-space((//div[contains(concat(' ',normalize-space(@class),' '),' pet-color ')])[2])" to
                    XPathStringResult("Black"),
            "boolean(count((//div[contains(concat(' ',normalize-space(@class),' '),' pet ')])[2]//div[contains(concat(' ',normalize-space(@class),' '),' pet-color ')]))" to
                    XPathBooleanResult(true),
            "count((//div[contains(concat(' ',normalize-space(@class),' '),' pet ')])[2]//div[contains(concat(' ',normalize-space(@class),' '),' pet-attribute ')])" to
                    XPathNumberResult(4.0),
            "boolean(count((//div[contains(concat(' ',normalize-space(@class),' '),' pet ')])[2]//div[contains(concat(' ',normalize-space(@class),' '),' pet-title ')]))" to
                    XPathBooleanResult(true),
            "count(//li[contains(concat(' ',normalize-space(@class),' '),' item ')])" to
                    XPathNumberResult(4.0),
            "normalize-space((//li[contains(concat(' ',normalize-space(@class),' '),' item ')])[1])" to
                    XPathStringResult("Item 0"),
            "normalize-space((//li[contains(concat(' ',normalize-space(@class),' '),' item ')])[2])" to
                    XPathStringResult("Item 1"),
            "normalize-space((//li[contains(concat(' ',normalize-space(@class),' '),' item ')])[3])" to
                    XPathStringResult("Item 2"),
            "normalize-space((//li[contains(concat(' ',normalize-space(@class),' '),' item ')])[4])" to
                    XPathStringResult("Item 3"),
            "string((//li[contains(concat(' ',normalize-space(@class),' '),' item ')])[1]/a/@href)" to
                    XPathStringResult("/item0"),
            "string((//li[contains(concat(' ',normalize-space(@class),' '),' item ')])[2]/a/@href)" to
                    XPathStringResult("/item1"),
            "string((//li[contains(concat(' ',normalize-space(@class),' '),' item ')])[3]/a/@href)" to
                    XPathStringResult("/item2")
        )
        return tests.map { (expr, expectedValue) ->
            DynamicTest.dynamicTest(expr.safeName()) {
                val ret = XPath.evaluateHtml(expr, html.readText())
                assertEquals(expectedValue, ret)
            }
        }
    }

    @Test
    fun `evaluate xpath with empty input`() {
        val expr = "string(//div)"
        assertThrows<InvalidQueryException> { XPath.evaluateHtml(expr, "") }
    }

    @Test
    fun `evalute xpath with html comment`() {
        val html = """
            <body>
                <div>
                    <!-- Some comment 1 -->
                    <!-- Some comment 2 -->
                    <!-- Some comment 3 -->
                </div>
            </body>""".trimIndent()
        val expr = "count(//comment())"
        val ret = XPath.evaluateHtml(expr, html)
        assertEquals(XPathNumberResult(value = 3.0), ret)
    }

    @TestFactory
    fun `evalute xpath node set`(): List<DynamicTest> {

        val html = """
            <body>
                <div id="one"><p>toto</p></div>
                <div id="two">tutu</div>
                <div id="three"><p>tata</p></div>
                <p></p>
                <a href="http://sample.net">dummy</a>
                <p></p>
            </body>""".trimIndent()

        val tests = listOf(
            "//div" to XPathNodeSetResult(size = 3),
            "//p" to XPathNodeSetResult(size = 4),
            "//div/p" to XPathNodeSetResult(size = 2),
            "//div/a" to XPathNodeSetResult(size = 0),
            "//a" to XPathNodeSetResult(size = 1),
            "//body/a" to XPathNodeSetResult(size = 1),
            "/html/body/a" to XPathNodeSetResult(size = 1),
            "//toto" to XPathNodeSetResult(size = 0)
        )
        return tests.map { (expr, expectedValue) ->
            DynamicTest.dynamicTest(expr.safeName()) {
                val ret = XPath.evaluateHtml(expr, html)
                assertEquals(expectedValue, ret)
            }
        }
    }

}