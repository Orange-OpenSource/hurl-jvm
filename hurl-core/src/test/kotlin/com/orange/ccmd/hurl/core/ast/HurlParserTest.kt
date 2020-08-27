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

import com.orange.ccmd.hurl.safeName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class HurlParserTest {

    @Test
    fun `parse method with success`() {
        val text = "DELETE http://example.org"
        val parser = HurlParser(text)
        val node = parser.method()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals("DELETE", node.value)
    }

    @TestFactory
    fun `parse comment with success`() = listOf(
        """
            # Some comment
            GET http://example.com
            """.trimIndent() to "# Some comment",
        "#" to "#"
    ).map { (text, expectedValue) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.comment()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedValue, node.value)
        }
    }

    @TestFactory
    fun `parse newline with success`() = listOf(
        "\n" to "\n",
        "\r\n" to "\r\n",
        "\nABCDEF" to "\n",
        "\r\n\n" to "\r\n"
    ).map { (text, expectedValue) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.newLine()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedValue, node.value)
        }
    }

    @TestFactory
    fun `fail to parse newline`() = listOf(
        "",
        "\r12344",
        "toto"
    ).map { text ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.newLine()
            assertNull(node)
            assertNotNull(parser.error)
        }
    }

    @TestFactory
    fun `parse lt with success`() = listOf(
        "\n",
        "",
        "     # Some dummy content\nABCDEF"
    ).map { text ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.lineTerminator()
            assertNotNull(node)
            assertNull(parser.error)
        }
    }

    @TestFactory
    fun `parse header with success`() = listOf(
        Triple("key: 0xabcdef", "key", "0xabcdef"),
        Triple("fruit: apple # Some comment\n", "fruit", "apple"),
        Triple("fruit: \"some banana\" # Some comment", "fruit", "\"some banana\""),
        Triple("id: \"\"", "id", "\"\"")
    ).map { (text, expectedName, expectedValue) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.header()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedName, node.name)
            assertEquals(expectedValue, node.value)
        }
    }

    @TestFactory
    fun `parse url with success`() = listOf(
        "http://acmecorp.org",
        "http://sample.com?toto=tata&tutu=12",
        "http://sample.com?search=some%20value",
        "{{root_url}}/path"
    ).map { text ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.url()
            assertNotNull(node)
            assertEquals(node.value, text)
            assertNull(parser.error)
        }
    }

    @Test
    fun `parse section-header with success`() {
        val text = "[Cookies]"
        val parser = HurlParser(text)
        val node = parser.sectionHeader("Cookies")
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals("[Cookies]", node.value)
    }

    @TestFactory
    fun `parse query-params-section with success`() = listOf(
        """
            
            [QueryStringParams]
            	q : valueA # Some comment on query param A
            	id : valueB
            """.trimIndent() to 2,
        """
            # Some comment on query param.
            [QueryStringParams]
            a: b
            """.trimIndent() to 1
    ).map { (text, count) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.queryStringParamsSection()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(count, node.params.size)
        }
    }

    @TestFactory
    fun `parse cookie with success`() = listOf(
        Triple("cookieA : valueA # Some comment on value A", "cookieA", "valueA"),
        Triple("cookieB:valueB", "cookieB", "valueB"),
        Triple("cookieC:{{token}}", "cookieC", "{{token}}"),
        Triple("cookieD: some-value", "cookieD", "some-value")
    ).map { (text, expectedKey, expectedValue) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.cookie()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedKey, node.name.value)
            assertEquals(expectedValue, node.value.value)
        }
    }

    @TestFactory
    fun `parse json with success`() = listOf(
        Triple("\"simple string\"", 15, 0),
        Triple("true\nxxx", 4, 4),
        Triple("""{"id": 0,"selected": true}""", 26, 0),
        Triple(
            """
                {
                    "user": {
                        "name": "toto",
                        "authentified":true,
                        "id": "abcedf"
                    }
                }12345678
            """.trimIndent(), 99, 8
        ),
        Triple(
            """
            {
                "id": 0,
                "name": "Frieda",
                "picture": "images/scottish-terrier.jpeg",
                "age": 3,
                "breed": "Scottish Terrier",
                "location": "Lisco, Alabama"
            }abcdef
            """.trimIndent(), 165, 6
        ),
        Triple("""[1, 2, 3, 4]true""", 12, 4),
        Triple("{\"id\": \"cafe\u0301\"}012345", 16, 6)
    ).map { (text, expectedBytes, expectedLeft) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.json()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedBytes, node.text.toByteArray().size)
            assertEquals(expectedLeft, parser.left())
        }
    }

    @TestFactory
    fun `parse raw-string with success`() = listOf(
        "```   \nline1\nline2\nline3\n```" to "line1\nline2\nline3\n",
        "```\n\nline1\n```" to "\nline1\n",
        "```abcdef\n12345678\ntoto```xxx" to "abcdef\n12345678\ntoto"
    ).map { (text, expectedValue) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.rawString()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedValue, node.value)
        }
    }

    @Test
    fun `fail to parse raw-string`() {
        val text = "```0123456789``"
        val parser = HurlParser(text)
        val node = parser.rawString()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @TestFactory
    fun `parse xml with success`() = listOf(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<drink>café</drink>" to "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<drink>café</drink>",
        """
            <?xml version="1.0" encoding="UTF-8"?>
             <note>
                 <to>Tove</to>
                 <from>Jani</from>
                 <heading>Reminder</heading>
                 <body>Don't forget me this weekend!</body>
             </note>xxx""".trimIndent() to """
                    <?xml version="1.0" encoding="UTF-8"?>
                     <note>
                         <to>Tove</to>
                         <from>Jani</from>
                         <heading>Reminder</heading>
                         <body>Don't forget me this weekend!</body>
                     </note>""".trimIndent(),
        """<?xml version="1.0" encoding="UTF-8"?><root/>123456789""" to """<?xml version="1.0" encoding="UTF-8"?><root/>"""
    ).map { (text, xml) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.xml()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(xml, node.text)
        }
    }

    @Test
    fun `fail to parse xml`() {
        val text = "toto"
        val parser = HurlParser(text)
        val node = parser.xml()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @TestFactory
    fun `parse base64 string with success`() = listOf(
        "V2VsY29tZSBodXJsIQ==" to "Welcome hurl!",
        """
            TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz
            IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg
            dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu
            dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo
            ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=
        """.trimIndent() to "Man is distinguished, not only by his reason," +
                " but by this singular passion from other animals, which is a lust of the mind, that by a perseverance" +
                " of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of" +
                " any carnal pleasure.",
        """CmxpbmUxCmxpbmUyDWxpbmUzDQoK""" to "\nline1\nline2\rline3\r\n\n"
    ).map { (encoded, expectedDecoded) ->
        DynamicTest.dynamicTest(encoded.safeName()) {
            val parser = HurlParser(encoded)
            val node = parser.base64String()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedDecoded, String(node.value))
        }
    }

    @Test
    fun `parse base64 with success`() {
        val text = "base64,V2VsY29tZSBodXJsIQ==;xxxxx"
        val expectedValue = "Welcome hurl!"
        val parser = HurlParser(text)
        val node = parser.base64()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals(expectedValue, String(node.base64String.value))
    }

    @Test
    fun `fail to parse base64`() {
        val text = "base64,V2VsY29tZSBodXJsIQ=="
        val parser = HurlParser(text)
        val node = parser.base64()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @TestFactory
    fun `parse file with success`() = listOf(
        "file,tmp/tata.bin;" to "tmp/tata.bin",
        "file,  test12345678;xxxx" to "test12345678"
    ).map { (text, fileName) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.file()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(fileName, node.fileName.value)
        }
    }

    @Test
    fun `fail to parse file`() {
        val text = "file,../../secret.key;"
        val parser = HurlParser(text)
        val node = parser.file()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @TestFactory
    fun `parse body with success`() = listOf(
        """```
Year,Make,Model,Description,Price
1997,Ford,E350,"ac, abs, moon",3000.00
1999,Chevy,"Venture ""Extended Edition""${'"'},"",4900.00
1999,Chevy,"Venture ""Extended Edition, Very Large""${'"'},,5000.00
1996,Jeep,Grand Cherokee,"MUST SELL! air, moon roof, loaded",4799.00
```""",
        """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:m="http://www.example.org">
  <soap:Header>
  </soap:Header>
  <soap:Body>
    <m:GetStockPrice>
      <m:StockName>GOOG</m:StockName>
    </m:GetStockPrice>
  </soap:Body>
</soap:Envelope>""",
        "file,tmp/tata.bin;"
    ).map { text ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.body()
            assertNotNull(node)
            assertNull(parser.error)
        }
    }

    @TestFactory
    fun `parse request with success`() = listOf(
        """
            GET http://example.com
            """.trimIndent(),
        """
            # Some comments
            # Bla bla bal
            
            POST https://example.com?id=1234 # Nothing to say
            toto: tata
            tutu: tata
            {
                "enabled": true
            }
            """.trimIndent(),
        """
            POST http://{{host}}
            [1,2,3]
        """.trimIndent()
    ).map { text ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.request()
            assertNotNull(node)
            assertNull(parser.error)
        }
    }

    @Test
    fun `parse status with success`() {
        val text = "200xxx"
        val expectedValue = 200
        val expectedText = "200"
        val parser = HurlParser(text)
        val node = parser.status()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals(expectedValue, node.value)
        assertEquals(expectedText, node.text)
    }

    @Test
    fun `fail to parse status`() {
        val text = "abcd"
        val parser = HurlParser(text)
        val node = parser.status()
        assertNull(node)
        assertNotNull(parser.error)
    }

    @Test
    fun `parse version with success`() {
        val text = "HTTP/1.1xxx"
        val parser = HurlParser(text)
        val node = parser.version()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals("HTTP/1.1", node.value)
    }

    @Test
    fun `parse captures-section with success`() {
        val text = """
            [Captures]
            token: xpath "string(//body/@data-token)" # Some dummy comments
            redirect_url_recap: header "Location"
            body: body
            test: regex "^abs$"
            toto\ tata : jsonpath "${'$'}.phoneNumbers[:1].type"
            var1: header "Location" regex "token=(.*)"  # bla bla bla
            var2: header "Content-Type"
            """.trimIndent()
        val parser = HurlParser(text)
        val node = parser.capturesSection()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals(7, node.captures.size)
    }

    @TestFactory
    fun `parse integer with success`() = listOf(
        Triple("7", 7, "7"),
        Triple("12345678", 12345678, "12345678"),
        Triple("-123xxx", -123, "-123"),
        Triple("+00046", 46, "+00046")
    ).map { (text, expectedValue, expectedText) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.integer()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedValue.toDouble(), node.value)
            assertEquals(expectedText, node.text)
        }
    }

    @TestFactory
    fun `fail to parse integer`() = listOf(
        "+-12",
        "abcdef"
    ).map { text ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.integer()
            assertNull(node)
            assertNotNull(parser.error)
        }
    }

    @TestFactory
    fun `parse float with success`() = listOf(
        Triple("3.14159265", 3.14159265, "3.14159265"),
        Triple("-0.1abcedf", -0.1, "-0.1")
    ).map { (text, expectedValue, expectedText) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.float()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedValue, node.value)
            assertEquals(expectedText, node.text)
        }
    }

    @TestFactory
    fun `fail to parse float`() = listOf(
        "abcedf",
        "+-1.35",
        "+.678",
        "6789.abced"
    ).map { text ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.float()
            assertNull(node)
            assertNotNull(parser.error)
        }
    }

    @TestFactory
    fun `parse predicate with success`() = listOf(
        "equals \"06 15 63 36 79\"" to "equals",
        "equals true" to "equals",
        "contains \"toto\"" to "contains",
        "equals 123.0" to "equals",
        "equals 12" to "equals",
        "startsWith \"something dummy\"xxx" to "startsWith",
        "countEquals 4" to "countEquals",
        "existsxxx" to "exists"
    ).map { (text, type) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.predicateFunc()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(type, node.type.value)
        }
    }

    @TestFactory
    fun `parse assert with success`() = listOf(
        "jsonpath \"\$['statusCode']\" equals 200",
        "jsonpath \"\$['statusCode']\" not equals 200",
        "xpath \"boolean(count(//div[@id='tutu']))\" equals false # Some comment",
        "xpath \"//div[@id='tutu']\" not exists",
        "header \"Location\" startsWith \"{{url}}/back\""
    ).map { text ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.assert()
            assertNotNull(node)
            assertNull(parser.error)
        }
    }

    @TestFactory
    fun `parse quoted-string with success`() = listOf(
        Triple("\"toto 12345\"xxx", "\"toto 12345\"", "toto 12345"),
        Triple("\"caf\\u{E9}\"", "\"caf\\u{E9}\"", "café")
    ).map { (text, expectedText, expectedValue) ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.quotedString()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedText, node.text)
            assertEquals(expectedValue, node.value)
        }
    }

    @TestFactory
    fun `fail to parse quoted-string with success`() = listOf(
        "\"some invalid unicode \\u{xxxxxxxxxx}\"",
        "\"invalid espace codepoint \\a\""
    ).map { text ->
        DynamicTest.dynamicTest(text) {
            val parser = HurlParser(text)
            val node = parser.quotedString()
            assertNull(node)
            assertNotNull(parser.error)
        }
    }

    @TestFactory
    fun `parse value-string-value with success`() = listOf(
        Triple("abcdef", "abcdef", "abcdef"),
        Triple("abcdef   ", "abcdef", "abcdef"),
        Triple("abcdef 0123456", "abcdef 0123456", "abcdef 0123456"),
        Triple("abcdef#0123456", "abcdef", "abcdef"),
        Triple("abcdef    #0123456", "abcdef", "abcdef"),
        Triple("012 345 678   ", "012 345 678", "012 345 678"),
        Triple("#abcdef", "", ""),
        Triple("", "", ""),
        Triple("abcdef  \\# 123454  # some comment", "abcdef  \\# 123454", "abcdef  # 123454"),
        Triple("Drink a good caf\\u{E9}", "Drink a good caf\\u{E9}", "Drink a good café"),
        Triple("\"Welcome !\"  # value-string can begin with ", "\"Welcome !\"", "\"Welcome !\"")
    ).map { (text, expectedText, expectedValue) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.valueString()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedText, node.text)
            assertEquals(expectedValue, node.value)
        }
    }

    @Test
    fun `parse cookie-section with success`() {
        val text = """
            [Cookies]
            cookie1: valueA
            HTTP/1.0 200
            """.trimIndent()
        val parser = HurlParser(text)
        val node = parser.cookiesSection()
        assertNotNull(node)
        assertNull(parser.error)
    }

    @Test
    fun `parse response with success`() {
        val text = """
            HTTP/1.0 200
            GET http://localhost:8000
            
            """.trimIndent()
        val parser = HurlParser(text)
        val node = parser.response()
        assertNotNull(node)
        assertEquals(node.headers.size, 0)
        assertNull(parser.error)
    }

    @TestFactory
    fun `parse key-string with success`() = listOf(
        "key: 0xabcdef" to "key",
        "fruit: apple # Some comment\n" to "fruit",
        "fruit : \"some banana\" # Some comment" to "fruit",
        "id : \"\"" to "id",
        "some\\ key\\ with\\ \\:colon:" to "some key with :colon",
        "tutu#tata: value" to "tutu",
        "Drink_a_good_caf\\u{00e9} : value" to "Drink_a_good_café",
        "tututata\ntoto" to "tututata",
        "banana split" to "banana"
    ).map { (text, expectedValue) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.keyString()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedValue, node.value)
        }
    }

    @TestFactory
    fun `fail to parse key-string`() = listOf(
        "#xxx",
        ""
    ).map { text ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.keyString()
            assertNull(node)
            assertNotNull(parser.error)
        }
    }

    @TestFactory
    fun `parse file-value with success`() = listOf(
        Triple("file,/tmp/data/toto.bin;", "/tmp/data/toto.bin", null),
        Triple("file,/tmp/data/toto.bin; text/plain", "/tmp/data/toto.bin", "text/plain"),
        Triple("file,/tmp/data/toto.bin; image/toto.tata # an invented content type", "/tmp/data/toto.bin", "image/toto.tata")
    ).map { (text, expectedFile, expectedContentType) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.fileValue()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedFile, node.fileName.value)
            assertEquals(expectedContentType, node.contentType?.value)
        }
    }

    private data class FileParamTest(
        val text: String,
        val key: String,
        val file: String,
        val contentType: String?
    )

    @TestFactory
    fun `parse file-param with success`() = listOf(
        FileParamTest(
            text = "file1: file,/tmp/data/toto.bin;",
            key = "file1",
            file = "/tmp/data/toto.bin",
            contentType = null
        ),
        FileParamTest(
            text = "file1: file,/tmp/data/toto.bin; image/jpg    # Some comments",
            key = "file1",
            file = "/tmp/data/toto.bin",
            contentType = "image/jpg"
        )
    ).map { (text, expectedKey, expectedFile, expectedContentType) ->
        DynamicTest.dynamicTest(text.safeName()) {
            val parser = HurlParser(text)
            val node = parser.fileParam()
            assertNotNull(node)
            assertNull(parser.error)
            assertEquals(expectedKey, node.key.value)
            assertEquals(expectedFile, node.file.fileName.value)
            assertEquals(expectedContentType, node.file.contentType?.value)
        }
    }

    @Test
    fun `multipart-form-data-section`() {
        val text = """
            [MultipartFormData]
            a: toto
            filea: file,/tmp/tata/toto.bin; # some comments
            b: tutu
            c: tata
            # sime comments
            fileb: file,/tmp/toto.bin; text/plain # some comments
            HTTP/1.0 200
            """.trimIndent()
        val parser = HurlParser(text)
        val node = parser.multipartFormDataSection()
        assertNotNull(node)
        assertNull(parser.error)
        assertEquals(node.params.size, 3)
        assertEquals(node.fileParams.size, 2)
    }

}