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

internal fun HurlParser.queryType(type: String): QueryType? {
    val begin = position.copy()
    val value = literal(type)?.value ?: return null
    return QueryType(begin = begin, end = position, value = value)
}

internal fun HurlParser.bodyQuery(): BodyQuery? {
    val begin = position.copy()
    val type = queryType("body") ?: return null
    return BodyQuery(begin = begin, end = position, type = type)
}

internal fun HurlParser.cookieQuery(): CookieQuery? {
    val begin = position.copy()

    val type = queryType("cookie") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val cookieName = quotedString() ?: return null

    return CookieQuery(begin = begin, end = position, type = type, spaces = spaces, expr = cookieName)
}

internal fun HurlParser.durationQuery(): DurationQuery? {
    val begin = position.copy()
    val type = queryType("duration") ?: return null
    return DurationQuery(begin = begin, end = position, type = type)
}

internal fun HurlParser.headerQuery(): HeaderQuery? {
    val begin = position.copy()

    val type = queryType("header") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val headerName = quotedString() ?: return null

    return HeaderQuery(begin = begin, end = position, type = type, spaces = spaces, headerName = headerName)
}

internal fun HurlParser.jsonPathQuery(): JsonPathQuery? {
    val begin = position.copy()

    val type = queryType("jsonpath") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null

    return JsonPathQuery(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.regexQuery(): RegexQuery? {
    val begin = position.copy()

    val type = queryType("regex") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null

    return RegexQuery(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.regexSubquery(): RegexSubquery? {
    val begin = position.copy()

    val type = subqueryType("regex") ?: return null
    val spaces = oneOrMore { space() } ?: return null
    val expr = quotedString() ?: return null

    return RegexSubquery(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.statusQuery(): StatusQuery? {
    val begin = position.copy()
    val type = queryType("status") ?: return null
    return StatusQuery(begin = begin, end = position, type = type)
}

internal fun HurlParser.subquery(): Subquery? {
    return regexSubquery()
}

internal fun HurlParser.subqueryType(type: String): SubqueryType? {
    val begin = position.copy()
    val value = literal(type)?.value ?: return null
    return SubqueryType(begin = begin, end = position, value = value)
}

