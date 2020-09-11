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

internal fun HurlParser.containPredicate(): ContainPredicate? {
    val begin = position.copy()
    val type = predicateType("contains") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return ContainPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.countPredicate(): CountPredicate? {
    val begin = position.copy()
    val type = predicateType("countEquals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = integer() ?: return null
    return CountPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.equalPredicate(): PredicateFunc? {
    return choice(listOf(
        { equalNumberPredicate() },
        { equalBoolPredicate() },
        { equalStringPredicate() },
        { equalNullPredicate() },
        { equalExprPredicate() },
    ))
}

internal fun HurlParser.equalBoolPredicate(): EqualBoolPredicate? {
    val begin = position.copy()
    val type = predicateType("equals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = bool() ?: return null
    return EqualBoolPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.equalNullPredicate(): EqualNullPredicate? {
    val begin = position.copy()
    val type = predicateType("equals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = `null`() ?: return null
    return EqualNullPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.equalNumberPredicate(): EqualNumberPredicate? {
    val begin = position.copy()
    val type = predicateType("equals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = choice(listOf(
        { float() },
        { integer() }
    )) ?: return null
    return EqualNumberPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.equalStringPredicate(): EqualStringPredicate? {
    val begin = position.copy()
    val type = predicateType("equals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return EqualStringPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.equalExprPredicate(): EqualExprPredicate? {
    val begin = position.copy()
    val type = predicateType("equals") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = expr() ?: return null
    return EqualExprPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.existPredicate(): ExistPredicate? {
    val begin = position.copy()
    val type = predicateType("exists") ?: return null
    return ExistPredicate(begin = begin, end = position, type = type)
}

internal fun HurlParser.includePredicate(): PredicateFunc? {
    return choice(listOf(
        { includeBoolPredicate() },
        { includeNumberPredicate() },
        { includeStringPredicate() },
        { includeNullPredicate() },
    ))
}

internal fun HurlParser.includeBoolPredicate(): IncludeBoolPredicate? {
    val begin = position.copy()
    val type = predicateType("includes") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = bool() ?: return null
    return IncludeBoolPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.includeNullPredicate(): IncludeNullPredicate? {
    val begin = position.copy()
    val type = predicateType("includes") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = `null`() ?: return null
    return IncludeNullPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.includeNumberPredicate(): IncludeNumberPredicate? {
    val begin = position.copy()
    val type = predicateType("includes") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = choice(listOf(
        { float() },
        { integer() }
    )) ?: return null
    return IncludeNumberPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.includeStringPredicate(): IncludeStringPredicate? {
    val begin = position.copy()
    val type = predicateType("includes") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return IncludeStringPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.matchPredicate(): MatchPredicate? {
    val begin = position.copy()
    val type = predicateType("matches") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return MatchPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

internal fun HurlParser.predicate(): Predicate? {
    val begin = position.copy()

    val not = optional { not() }
    val spaces = if (not != null) {
        oneOrMore { space() } ?: return null
    } else {
        listOf()
    }
    val predicateFunc = predicateFunc() ?: return null
    return Predicate(begin = begin, end = position, not = not, spaces = spaces, predicateFunc = predicateFunc)
}

internal fun HurlParser.predicateFunc(): PredicateFunc? {
    return choice(listOf(
        { equalPredicate() },
        { countPredicate() },
        { startWithPredicate() },
        { containPredicate() },
        { includePredicate() },
        { matchPredicate() },
        { existPredicate() },
    ))
}

internal fun HurlParser.predicateType(type: String): PredicateType? {
    val begin = position.copy()
    val value = literal(type)?.value ?: return null
    return PredicateType(begin = begin, end = position, value = value)
}

internal fun HurlParser.startWithPredicate(): StartWithPredicate? {
    val begin = position.copy()
    val type = predicateType("startsWith") ?: return null
    val spaces = zeroOrMore { space() }
    val expr = quotedString() ?: return null
    return StartWithPredicate(begin = begin, end = position, type = type, spaces = spaces, expr = expr)
}

