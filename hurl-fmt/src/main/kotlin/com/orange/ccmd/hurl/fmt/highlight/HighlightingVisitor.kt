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
import com.orange.ccmd.hurl.core.ast.Number

class HighlightingVisitor(
    val commentFunc: (String) -> String,
    val stringFunc: (String) -> String,
    val numberFunc: (String) -> String,
    val booleanFunc: (String) -> String,
    val urlFunc: (String) -> String,
    val methodFunc: (String) -> String,
    val versionFunc: (String) -> String,
    val sectionHeaderFunc: (String) -> String,
    val queryTypeFunc: (String) -> String,
    val predicateTypeFunc: (String) -> String,
    val whitespacesFunc: (String) -> String
) : Visitor {

    var text: String = ""

    override fun visit(node: Node): Boolean {

        when (node) {
            // Comments nodes.
            is Comment -> text += commentFunc(node.value)

            // String nodes
            is Base64String -> text += stringFunc(node.text)
            is CookieValue -> text += stringFunc(node.value)
            is HString -> text += stringFunc(node.text)
            is Json -> text += stringFunc(node.text)
            is RawString -> text += stringFunc(node.text)
            is Xml -> text += stringFunc(node.text)

            // Primitives nodes.
            is Number -> text += numberFunc(node.text)
            is Bool -> text += booleanFunc(node.text)

            // Plain.
            is Literal -> text += node.value

            // HTTP nodes.
            is Status -> text += numberFunc(node.text)
            is Url -> text += urlFunc(node.value)
            is Method -> text += methodFunc(node.value)
            is Version -> text += versionFunc(node.value)

            // Hurl nodes.
            is SectionHeader -> text += sectionHeaderFunc(node.value)

            // Query type.
            is QueryType -> text += queryTypeFunc(node.value)

            // Predicate.
            is Not -> text += predicateTypeFunc(node.text.value)
            is PredicateType -> text += predicateTypeFunc(node.value)

            // Whitespaces nodes.
            is Space -> text += whitespacesFunc(node.value)
            is NewLine -> text += whitespacesFunc(node.value)
            else -> return true
        }
        return false
    }

}