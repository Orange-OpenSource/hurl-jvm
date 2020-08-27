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

package com.orange.ccmd.hurl.fmt.highlight

import com.orange.ccmd.hurl.core.ast.*
import com.orange.ccmd.hurl.fmt.Formatter

class HtmlFormatter(val theme: String = "dark256") : Formatter {

    private val highlightingVisitor: HighlightingVisitor = HighlightingVisitor(
        commentFunc = { span(text = it, `class` = "comment") },
        stringFunc = { span(text = it, `class` = "string") },
        numberFunc = { span(text = it, `class` = "number") },
        booleanFunc = { span(text = it, `class` = "boolean") },
        sectionHeaderFunc = { span(text = it, `class` = "section-header") },
        queryTypeFunc = { span(text = it, `class` = "query-type") },
        predicateTypeFunc = { span(text = it, `class` = "predicate-type") },
        urlFunc = { span(text = it, `class` = "url") },
        methodFunc = { span(text = it, `class` = "method") },
        versionFunc = { span(text = it, `class` = "version") },
        whitespacesFunc = { it }
    )

    private val themes: List<Theme> = listOf(
        Theme(
            name = "dark16",
            background = "black",
            foreground = "white",
            string = "green",
            number = "blue",
            boolean = "blue",
            url = "cyan",
            method = "yellow",
            version = "white",
            sectionHeader = "magenta",
            queryType = "cyan",
            predicateType = "yellow",
            comment = "gray"
        ),
        Theme(
            name = "dark256",
            background = "black",
            foreground = "white",
            string = "forestgreen",
            number = "dodgerblue",
            boolean = "dodgerblue",
            url = "cyan",
            method = "orange",
            version = "white",
            sectionHeader = "magenta",
            queryType = "cyan",
            predicateType = "orange",
            comment = "dimgray"
        ),
        Theme(
            name = "light256",
            background = "white",
            foreground = "black",
            string = "darkgreen",
            number = "blue",
            boolean = "blue",
            url = "darkblue",
            method = "black",
            version = "black",
            sectionHeader = "darkmagenta",
            queryType = "teal",
            predicateType = "darkblue",
            comment = "dimgray"
        )
    )

    override fun format(hurlFile: HurlFile): String {

        walk(highlightingVisitor, hurlFile)

        val body = highlightingVisitor.text
        val theme = themes.first { it.name == theme }
        val template = javaClass.getResource("/hurl.mustache").readText()

        return renderTemplate(
            template = template,
            variables = mapOf(
                "theme.background" to theme.background,
                "theme.foreground" to theme.foreground,
                "theme.string" to theme.string,
                "theme.number" to theme.number,
                "theme.boolean" to theme.boolean,
                "theme.url" to theme.url,
                "theme.method" to theme.method,
                "theme.version" to theme.version,
                "theme.sectionHeader" to theme.sectionHeader,
                "theme.queryType" to theme.queryType,
                "theme.predicateType" to theme.predicateType,
                "theme.comment" to theme.comment,
                "body" to body
            )
        )
    }

    private fun span(text: String, `class`: String): String {
        // TODO: do a proper HTML escaping here.
        var escapedText = text
        escapedText = escapedText.replace("<", "&lt;")
        escapedText = escapedText.replace(">", "&gt;")
        return "<span class=\"$`class`\">$escapedText</span>"
    }

    private fun renderTemplate(template: String, variables: Map<String, String>): String {
        var text = template
        for ((name, value) in variables) {
            val token = "{{$name}}"
            text = text.replace(token, value)
        }
        return text
    }

}

