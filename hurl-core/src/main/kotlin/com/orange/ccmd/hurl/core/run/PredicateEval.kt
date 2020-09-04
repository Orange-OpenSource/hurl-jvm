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

package com.orange.ccmd.hurl.core.run

import com.orange.ccmd.hurl.core.ast.*

/**
 * Evaluates if query result [first] equals string [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be evaluated
 * @param second string to match
 * @return the result of predicate
 */
internal fun EqualStringPredicate.eval(not: Boolean, first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> first.value == second
        else -> false
    }
    val secondText = if (not) { "doesn't equal string <$second>" } else { "equals string <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] equals boolean [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be evaluated
 * @param second boolean to match
 * @return the result of predicate
 */
internal fun EqualBoolPredicate.eval(not: Boolean, first: QueryResult, second: Boolean): PredicateResult {
    val succeeded = when (first) {
        is QueryBooleanResult -> first.value == second
        else -> false
    }
    val secondText = if (not) { "doesn't equal boolean <$second>" } else { "equals boolean <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] equals number [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be evaluated
 * @param second number to match
 * @return the result of predicate
 */
internal fun EqualNumberPredicate.eval(not: Boolean, first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryNumberResult -> first.value.toDouble() == second
        else -> false
    }
    val secondText = if (not) { "doesn't equal number <$second>" } else { "equals number <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] contains the string [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be tested
 * @param second substring
 * @return the result of predicate
 */
internal fun ContainPredicate.eval(not: Boolean, first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> first.value.contains(second)
        else -> false
    }
    val secondText = if (not) { "doesn't contain string <$second>" } else { "contains string <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] is a container including number [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be tested
 * @param second number to test
 * @return the result of predicate
 */
internal fun IncludeNumberPredicate.eval(not: Boolean, first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> second in first.value
            .filterIsInstance<Number>()
            .map { it.toDouble() }
        else -> false
    }
    val secondText = if (not) { "doesn't include number <$second>" } else { "include number <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] is a container including boolean [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be tested
 * @param second boolean to test
 * @return the result of predicate
 */
internal fun IncludeBoolPredicate.eval(not: Boolean, first: QueryResult, second: Boolean): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> second in first.value
        else -> false
    }
    val secondText = if (not) { "doesn't include boolean <$second>" } else { "include boolean <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] is a container including string [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be tested
 * @param second string to test
 * @return the result of predicate
 */
internal fun IncludeStringPredicate.eval(not: Boolean, first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> second in first.value
        else -> false
    }
    val secondText = if (not) { "doesn't include string <$second>" } else { "include string <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] is a container of size [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be tested
 * @param second size of the container
 * @return the result of predicate
 */
internal fun CountPredicate.eval(not: Boolean, first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> first.value.size == second.toInt()
        is QueryNodeSetResult -> first.size == second.toInt()
        else -> false
    }
    val secondText = if (not) { "count doesn't equals $second" } else { "count equals $second" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] starts with string [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be tested
 * @param second prefix
 * @return the result of predicate
 */
internal fun StartWithPredicate.eval(not: Boolean, first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> first.value.startsWith(second)
        else -> false
    }
    val secondText = if (not) { "doesn't start with string <$second>" } else { "starts with string <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] matches regex [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be matched
 * @param second regex
 * @return the result of predicate
 */
internal fun MatchPredicate.eval(not: Boolean, first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> Regex(pattern = second).containsMatchIn(first.value)
        else -> false
    }
    val secondText = if (not) { "doesn't match string <$second>" } else { "matches string <$second>" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = secondText
    )
}

/**
 * Evaluates if query result [first] exist.
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be evaluated
 * @return the result of predicate
 */
internal fun ExistPredicate.eval(not: Boolean, first: QueryResult): PredicateResult {
    val succeeded =  when (first) {
        is QueryNoneResult -> false
        is QueryNodeSetResult -> first.size > 0
        else -> true
    }
    val second = if (not) { "" } else { "anything" }
    return PredicateResult(
        succeeded = succeeded xor not,
        first = first.text(),
        second = second
    )
}