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

/**
 * A Visitor's visit method is invoked for each node encountered by [walk].
 * [walk] visits each of the children of node with the visitor.
 */
interface Visitor {
    fun visit(node: Node): Boolean
}

/**
 * Traverses an AST in depth-first order.
 *
 * It starts by calling visitor.visit(node); node must not be null.
 * If visitor.visit(node) is true, [walk] is invoked recursively
 * with visitor for each of the non-nil children of node.
 * @param visitor
 * @param node
 */
fun walk(visitor: Visitor, node: Node?) {
    if (node == null || !visitor.visit(node)) {
        return
    }
    when (node) {
        is Assert -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.query)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.predicate)
            walk(visitor, node.lt)
        }
        is AssertsSection -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.header)
            walk(visitor, node.lt)
            node.asserts.forEach { walk(visitor, it) }
        }
        is Base64 -> {
            walk(visitor, node.prefix)
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.base64String)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.suffix)
        }
        is Body -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.bytes)
            walk(visitor, node.lt)
        }
        is BodyQuery -> {
            walk(visitor, node.type)
        }
        is Capture -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.name)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.colon)
            node.spaces2.forEach { walk(visitor, it) }
            walk(visitor, node.query)
            walk(visitor, node.lt)
        }
        is CapturesSection -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.header)
            walk(visitor, node.lt)
            node.captures.forEach { walk(visitor, it) }
        }
        is ContainPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is Cookie -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.name)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.colon)
            node.spaces2.forEach { walk(visitor, it) }
            walk(visitor, node.value)
            walk(visitor, node.lt)
        }
        is CookieQuery -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
            walk(visitor, node.subquery)
        }
        is CookiesSection -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.header)
            walk(visitor, node.lt)
            node.cookies.forEach { walk(visitor, it) }
        }
        is CountPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is CountSubquery -> {
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.type)
        }
        is DurationQuery -> {
            walk(visitor, node.type)
        }
        is Entry -> {
            walk(visitor, node.request)
            walk(visitor, node.response)
        }
        is EqualBoolPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is EqualExprPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is EqualNumberPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is EqualNullPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is EqualStringPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is ExistPredicate -> {
            walk(visitor, node.type)
        }
        is Expr -> {
            walk(visitor, node.prefix)
            walk(visitor, node.name)
            walk(visitor, node.suffix)
        }
        is File -> {
            walk(visitor, node.prefix)
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.fileName)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.suffix)
        }
        is FileParam -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.key)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.colon)
            node.spaces2.forEach { walk(visitor, it) }
            walk(visitor, node.file)
            walk(visitor, node.lt)
        }
        is FileValue -> {
            walk(visitor, node.prefix)
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.fileName)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.suffix)
            node.spaces2.forEach { walk(visitor, it) }
            walk(visitor, node.contentType)
        }
        is FormParamsSection -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.header)
            walk(visitor, node.lt)
            node.params.forEach { walk(visitor, it) }
        }
        is GreaterPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is GreaterOrEqualPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is Header -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.keyValue)
            walk(visitor, node.lt)
        }
        is HeaderQuery -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.headerName)
            walk(visitor, node.subquery)
        }
        is HurlFile -> {
            node.entries.forEach { walk(visitor, it) }
            node.lts.forEach { walk(visitor, it) }
        }
        is IncludeBoolPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is IncludeNullPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is IncludeNumberPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is IncludeStringPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is JsonPathQuery -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
            walk(visitor, node.subquery)
        }
        is KeyValue -> {
            walk(visitor, node.key)
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.colon)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.value)
        }
        is LessPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is LessOrEqualPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is LineTerminator -> {
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.comment)
            walk(visitor, node.newLine)
        }
        is MatchPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is MultipartFormDataSection -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.header)
            walk(visitor, node.lt)

            val paramsNodes = node.params.union(node.fileParams)
            paramsNodes
                .sortedBy { it.begin.offset }
                .forEach { walk(visitor, it) }
        }
        is Not -> {
            walk(visitor, node.text)
        }
        is Param -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.keyValue)
            walk(visitor, node.lt)
        }
        is Predicate -> {
            walk(visitor, node.not)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.predicateFunc)
        }
        is QueryStringParamsSection -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.header)
            walk(visitor, node.lt)
            node.params.forEach { walk(visitor, it) }
        }
        is RegexQuery -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
            walk(visitor, node.subquery)
        }
        is RegexSubquery -> {
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.type)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is Request -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.method)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.url)
            walk(visitor, node.lt)
            node.headers.forEach { walk(visitor, it) }
            node.sections.forEach { walk(visitor, it) }
            walk(visitor, node.body)
        }
        is Response -> {
            node.lts.forEach { walk(visitor, it) }
            node.spaces0.forEach { walk(visitor, it) }
            walk(visitor, node.version)
            node.spaces1.forEach { walk(visitor, it) }
            walk(visitor, node.status)
            walk(visitor, node.lt)
            node.headers.forEach { walk(visitor, it) }
            node.sections.forEach { walk(visitor, it) }
            walk(visitor, node.body)
        }
        is StartWithPredicate -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
        }
        is StatusQuery -> {
            walk(visitor, node.type)
        }
        is VariableQuery -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.variable)
            walk(visitor, node.subquery)
        }
        is XPathQuery -> {
            walk(visitor, node.type)
            node.spaces.forEach { walk(visitor, it) }
            walk(visitor, node.expr)
            walk(visitor, node.subquery)
        }
        else -> { }
    }
}