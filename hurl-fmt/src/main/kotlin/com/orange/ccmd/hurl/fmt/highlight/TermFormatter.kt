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

import com.orange.ccmd.hurl.core.ast.HurlFile
import com.orange.ccmd.hurl.core.ast.walk
import com.orange.ccmd.hurl.core.utils.ansi
import com.orange.ccmd.hurl.fmt.Formatter

class TermFormatter(val showWhitespaces: Boolean) : Formatter {

    private val highlightingVisitor: HighlightingVisitor = HighlightingVisitor(
        commentFunc = { it.ansi.fg.brightBlack },
        stringFunc = { it.ansi.fg.green },
        numberFunc = { it.ansi.fg.cyan },
        booleanFunc = { it.ansi.fg.cyan },
        nullFunc = { it.ansi.fg.cyan },
        sectionHeaderFunc = { it.ansi.fg.magenta },
        queryTypeFunc = { it.ansi.fg.brightCyan },
        predicateTypeFunc = { it.ansi.fg.brightYellow },
        urlFunc = { it.ansi.fg.brightCyan },
        methodFunc = { it.ansi.fg.brightYellow },
        versionFunc = { it },
        whitespacesFunc = { it.whitespace() }
    )

    override fun format(hurlFile: HurlFile): String {
        walk(highlightingVisitor, hurlFile)
        return highlightingVisitor.text
    }

    private fun String.whitespace(): String {
        val whitespace = if (showWhitespaces) {
            val whites = listOf(
                " " to "\u00B7",
                "\n" to "\u21B5\n",
                "\t" to "\u2192   "
            )
            var output = this
            for ((src, dst) in whites) {
                output = output.replace(src, dst)
            }
            output
        } else {
            this
        }
        return whitespace.ansi.fg.brightBlack
    }
}

