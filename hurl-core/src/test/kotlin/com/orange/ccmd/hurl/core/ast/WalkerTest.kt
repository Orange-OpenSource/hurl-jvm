package com.orange.ccmd.hurl.core.ast

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


internal class WalkerTest {


    class PlainTextFormatter : Visitor {

        var text: String = ""

        override fun visit(node: Node): Boolean {
            when (node) {
                is HurlFile -> text = ""

                // Comments nodes.
                is Comment -> text += node.value
                is Base64String -> text += node.text
                is CookieValue -> text += node.value
                is Json -> text += node.text
                is RawString -> text += node.text
                is Xml -> text += node.text

                // Primitives nodes.
                is Number -> text += node.text
                is Bool -> text += node.text

                // Plain.
                is Literal -> text += node.value
                is HString -> text += node.text

                // HTTP nodes.
                is Status -> text += node.text
                is Url -> text += node.value
                is Method -> text += node.value
                is Version -> text += node.value

                // Hurl nodes.
                is SectionHeader -> text += node.value

                // Query type.
                is QueryType -> text += node.value

                // Query type.
                is PredicateType -> text += node.value

                // Whitespaces nodes.
                is Space -> text += node.value
                is NewLine -> text += node.value
                else -> return true
            }
            return true
        }
    }


    @Test
    fun `plain text formatter return the hurl input text`() {
        val text = """
            # Example of a valid Hurl files
            GET http://sample.org
            key1: value1 # some comments
            # some somments
            [QueryStringParams] # some comments
            a: b # some comments
            [FormParams] # some comments
            c: d # some comments
            [MultipartFormData]
            e: f
            #some comments
            g: file,/toto/tata.bin; text/plain
            d: g
            g: file,/toto/tata.bin; # some comments
            [Cookies]
            a: b # some comments
            c: d # some comments
            ```
            line1
            line2
            line3
            ```
            HTTP/* 200
        """.trimIndent()

        val parser = HurlParser(text)
        val hurlFile = parser.hurlFile()
        assertNotNull(hurlFile)

        val formatter = PlainTextFormatter()
        walk(visitor = formatter, node = hurlFile)
        assertEquals(text, formatter.text)
    }



}