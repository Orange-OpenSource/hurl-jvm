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

import com.orange.ccmd.hurl.core.parser.isAsciiSpace


internal fun HurlParser.query(): Query? {
    return choice(listOf(
        { statusQuery() },
        { headerQuery() },
        { cookieQuery() },
        { bodyQuery() },
        { xPathQuery() },
        { jsonPathQuery() },
        { regexQuery() },
        { variableQuery() },
        { durationQuery() },
    ))
}

internal fun HurlParser.queryType(value: QueryTypeValue): QueryType? {
    val begin = position.copy()
    literal(value.text) ?: return null
    return QueryType(begin = begin, end = position.copy(), value = value)
}

internal fun HurlParser.bodyQuery(): BodyQuery? {
    val begin = position.copy()

    val type = queryType(QueryTypeValue.BODY) ?: return null
    val subquery = optional { subquery() }
    return BodyQuery(begin = begin, end = position.copy(), type = type, subquery = subquery)
}

internal fun HurlParser.cookieQuery(): CookieQuery? {
    val begin = position.copy()

    val type = queryType(QueryTypeValue.COOKIE) ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val cookieName = quotedString() ?: return null
    val subquery = optional { subquery() }
    return CookieQuery(begin = begin, end = position.copy(), type = type, expr = cookieName, spaces = spaces, subquery = subquery)
}

internal fun HurlParser.countSubquery(): CountSubquery? {
    val begin = position.copy()

    val spaces = oneOrMore { space() } ?: return null
    val type = subqueryType(SubqueryTypeValue.COUNT) ?: return null

    // We must test that the next word is not `countPredicate`
    // the grammar is ambiguous until `countPredicate`is removed from the grammar.
    val next = peek()
    if (next != null && !next.isAsciiSpace) {
        return null
    }

    return CountSubquery(begin = begin, end = position.copy(), type = type, spaces = spaces)
}

internal fun HurlParser.durationQuery(): DurationQuery? {
    val begin = position.copy()

    val type = queryType(QueryTypeValue.DURATION) ?: return null
    val subquery = optional { subquery() }
    return DurationQuery(begin = begin, end = position.copy(), type = type, subquery = subquery)
}

internal fun HurlParser.headerQuery(): HeaderQuery? {
    val begin = position.copy()

    val type = queryType(QueryTypeValue.HEADER) ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val headerName = quotedString() ?: return null
    val subquery = optional { subquery() }
    return HeaderQuery(begin = begin, end = position.copy(), type = type, spaces = spaces, headerName = headerName, subquery = subquery)
}

internal fun HurlParser.jsonPathQuery(): JsonPathQuery? {
    val begin = position.copy()

    val type = queryType(QueryTypeValue.JSONPATH) ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null
    val subquery = optional { subquery() }
    return JsonPathQuery(begin = begin, end = position.copy(), type = type, spaces = spaces, expr = expr, subquery = subquery)
}

internal fun HurlParser.regexQuery(): RegexQuery? {
    val begin = position.copy()

    val type = queryType(QueryTypeValue.REGEX) ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null
    val subquery = optional { subquery() }
    return RegexQuery(begin = begin, end = position.copy(), type = type, spaces = spaces, expr = expr, subquery = subquery)
}

internal fun HurlParser.regexSubquery(): RegexSubquery? {
    val begin = position.copy()

    val spaces0 = oneOrMore { space() } ?: return null
    val type = subqueryType(SubqueryTypeValue.REGEX) ?: return null
    val spaces1 = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null
    return RegexSubquery(begin = begin, end = position.copy(), type = type, spaces0 = spaces0, expr = expr, spaces1 = spaces1)
}

internal fun HurlParser.statusQuery(): StatusQuery? {
    val begin = position.copy()
    val type = queryType(QueryTypeValue.STATUS) ?: return null
    val subquery = optional { subquery() }
    return StatusQuery(begin = begin, end = position.copy(), type = type, subquery = subquery)
}

internal fun HurlParser.subquery(): Subquery? {
    return choice(
        listOf(
            { regexSubquery() },
            { countSubquery() },
        )
    )
}

internal fun HurlParser.subqueryType(value: SubqueryTypeValue): SubqueryType? {
    val begin = position.copy()
    literal(value.text) ?: return null
    return SubqueryType(begin = begin, end = position.copy(), value = value)
}

internal fun HurlParser.variableQuery(): VariableQuery? {
    val begin = position.copy()

    val type = queryType(QueryTypeValue.VARIABLE) ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val variable = quotedString() ?: return null
    val subquery = optional { subquery() }
    return VariableQuery(begin = begin, end = position.copy(), type = type, spaces = spaces, variable = variable, subquery = subquery)
}

internal fun HurlParser.xPathQuery(): XPathQuery? {
    val begin = position.copy()

    val type = queryType(QueryTypeValue.XPATH) ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null
    val subquery = optional { subquery() }
    return XPathQuery(begin = begin, end = position.copy(), type = type, spaces = spaces, expr = expr, subquery = subquery)
}
