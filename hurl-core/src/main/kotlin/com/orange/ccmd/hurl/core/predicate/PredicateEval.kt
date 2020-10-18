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

package com.orange.ccmd.hurl.core.predicate

import com.orange.ccmd.hurl.core.run.QueryBooleanResult
import com.orange.ccmd.hurl.core.run.QueryListResult
import com.orange.ccmd.hurl.core.run.QueryNodeSetResult
import com.orange.ccmd.hurl.core.run.QueryNoneResult
import com.orange.ccmd.hurl.core.run.QueryNumberResult
import com.orange.ccmd.hurl.core.run.QueryObjectResult
import com.orange.ccmd.hurl.core.run.QueryResult
import com.orange.ccmd.hurl.core.run.QueryStringResult

/**
 * Evaluates if query result [first] equals string [second].
 * @param first query result to be evaluated
 * @param second string to match
 * @return the result of predicate
 */
internal fun equalString(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> first.value == second
        else -> false
    }
    val secondText = "equals string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] doesn't equal string [second].
 * @param first query result to be evaluated
 * @param second string to match
 * @return the result of predicate
 */
internal fun notEqualString(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> first.value != second
        else -> true
    }
    val secondText = "doesn't equal string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] equals boolean [second].
 * @param first query result to be evaluated
 * @param second boolean to match
 * @return the result of predicate
 */
internal fun equalBool(first: QueryResult, second: Boolean): PredicateResult {
    val succeeded = when (first) {
        is QueryBooleanResult -> first.value == second
        else -> false
    }
    val secondText = "equals boolean <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] doesn't equal boolean [second].
 * @param first query result to be evaluated
 * @param second boolean to match
 * @return the result of predicate
 */
internal fun notEqualBool(first: QueryResult, second: Boolean): PredicateResult {
    val succeeded = when (first) {
        is QueryBooleanResult -> first.value != second
        else -> true
    }
    val secondText = "doesn't equal boolean <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] equals null.
 * @param first query result to be evaluated
 * @return the result of predicate
 */
internal fun equalNull(first: QueryResult): PredicateResult {
    val succeeded = when (first) {
        is QueryObjectResult -> first.value == null
        else -> false
    }
    val secondText = "equals <null>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] doesn't equal null.
 * @param first query result to be evaluated
 * @return the result of predicate
 */
internal fun notEqualNull(first: QueryResult): PredicateResult {
    val succeeded = when (first) {
        is QueryObjectResult -> first.value != null
        else -> true
    }
    val secondText = "doesn't equal <null>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] equals number [second].
 * @param first query result to be evaluated
 * @param second number to match
 * @return the result of predicate
 */
internal fun equalDouble(first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryNumberResult -> first.value.toDouble() == second
        else -> false
    }
    val secondText = "equals number <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] doesn't equals number [second].
 * @param first query result to be evaluated
 * @param second number to match
 * @return the result of predicate
 */
internal fun notEqualDouble(first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryNumberResult -> first.value.toDouble() != second
        else -> true
    }
    val secondText = "doesn't equal number <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}


internal fun equal(first: QueryResult, second: Any?): PredicateResult {
    return when (second) {
        is String -> equalString(first = first, second = second)
        is Number -> equalDouble(first = first, second = second.toDouble())
        is Boolean -> equalBool(first = first, second = second)
        null -> equalNull(first = first)
        else -> TODO()
    }
}

internal fun notEqual(first: QueryResult, second: Any?): PredicateResult {
    return when (second) {
        is String -> notEqualString(first = first, second = second)
        is Number -> notEqualDouble(first = first, second = second.toDouble())
        is Boolean -> notEqualBool(first = first, second = second)
        null -> notEqualNull(first = first)
        else -> TODO()
    }
}

/**
 * Evaluates if query result [first] contains the string [second].
 *
 * @param first query result to be tested
 * @param second substring
 * @return the result of predicate
 */
internal fun contain(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> first.value.contains(second)
        else -> false
    }
    val secondText = "contains string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] doesn't contain the string [second].
 *
 * @param first query result to be tested
 * @param second substring
 * @return the result of predicate
 */
internal fun notContain(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryNoneResult -> true
        is QueryStringResult -> !first.value.contains(second)
        else -> false
    }
    val secondText = "doesn't contain string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container including null.
 * @param first query result to be tested
 * @return the result of predicate
 */
internal fun includeNull(first: QueryResult): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> null in first.value
        else -> false
    }
    val secondText = "includes <null>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container not including null.
 * @param first query result to be tested
 * @return the result of predicate
 */
internal fun notIncludeNull(first: QueryResult): PredicateResult {
    val succeeded = when (first) {
        is QueryNoneResult -> true
        is QueryListResult -> null !in first.value
        else -> false
    }
    val secondText = "doesn't include <null>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container including number [second].
 * @param first query result to be tested
 * @param second number to test
 * @return the result of predicate
 */
internal fun includeNumber(first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> second in first.value
            .filterIsInstance<Number>()
            .map { it.toDouble() }
        else -> false
    }
    val secondText = "include number <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container not including number [second].
 * @param first query result to be tested
 * @param second number to test
 * @return the result of predicate
 */
internal fun notIncludeNumber(first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryNoneResult -> true
        is QueryListResult -> second !in first.value
            .filterIsInstance<Number>()
            .map { it.toDouble() }
        else -> false
    }
    val secondText = "doesn't include number <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container including boolean [second].
 * @param first query result to be tested
 * @param second boolean to test
 * @return the result of predicate
 */
internal fun includeBool(first: QueryResult, second: Boolean): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> second in first.value
        else -> false
    }
    val secondText = "include boolean <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container not including boolean [second].
 * @param not boolean, true to inverse the predicate, false otherwise
 * @param first query result to be tested
 * @param second boolean to test
 * @return the result of predicate
 */
internal fun notIncludeBool(first: QueryResult, second: Boolean): PredicateResult {
    val succeeded = when (first) {
        is QueryNoneResult -> true
        is QueryListResult -> second !in first.value
        else -> false
    }
    val secondText = "doesn't include boolean <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container including string [second].
 * @param first query result to be tested
 * @param second string to test
 * @return the result of predicate
 */
internal fun includeString(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> second in first.value
        else -> false
    }
    val secondText = "include string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container not including string [second].
 * @param first query result to be tested
 * @param second string to test
 * @return the result of predicate
 */
internal fun notIncludeString(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryNoneResult -> true
        is QueryListResult -> second !in first.value
        else -> false
    }
    val secondText = "doesn't include string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is a container of size [second].
 * @param first query result to be tested
 * @param second size of the container
 * @return the result of predicate
 */
internal fun count(first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryListResult -> first.value.size == second.toInt()
        is QueryNodeSetResult -> first.size == second.toInt()
        else -> false
    }
    val secondText = "count equals $second"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] is not a container of size [second].
 * @param first query result to be tested
 * @param second size of the container
 * @return the result of predicate
 */
internal fun notCount(first: QueryResult, second: Double): PredicateResult {
    val succeeded = when (first) {
        is QueryNoneResult -> true
        is QueryListResult -> first.value.size != second.toInt()
        is QueryNodeSetResult -> first.size != second.toInt()
        else -> false
    }
    val secondText = "count doesn't equals $second"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] starts with string [second].
 * @param first query result to be tested
 * @param second prefix
 * @return the result of predicate
 */
internal fun startWith(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> first.value.startsWith(second)
        else -> false
    }
    val secondText = "starts with string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] doesn't start with string [second].
 * @param first query result to be tested
 * @param second prefix
 * @return the result of predicate
 */
internal fun notStartWith(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryNoneResult -> true
        is QueryStringResult -> !first.value.startsWith(second)
        else -> false
    }
    val secondText = "doesn't start with string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] matches regex [second].
 * @param first query result to be matched
 * @param second regex
 * @return the result of predicate
 */
internal fun match(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryStringResult -> Regex(pattern = second).containsMatchIn(first.value)
        else -> false
    }
    val secondText = "matches string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}

/**
 * Evaluates if query result [first] doesn't match regex [second].
 * @param first query result to be matched
 * @param second regex
 * @return the result of predicate
 */
internal fun notMatch(first: QueryResult, second: String): PredicateResult {
    val succeeded = when (first) {
        is QueryNoneResult -> true
        is QueryStringResult -> !Regex(pattern = second).containsMatchIn(first.value)
        else -> false
    }
    val secondText = "doesn't match string <$second>"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = secondText)
}


/**
 * Evaluates if query result [first] exist.
 * @param first query result to be evaluated
 * @return the result of predicate
 */
internal fun exist(first: QueryResult): PredicateResult {
    val succeeded =  when (first) {
        is QueryNoneResult -> false
        is QueryNodeSetResult -> first.size > 0
        else -> true
    }
    val second = "anything"
    return PredicateResult(succeeded = succeeded, first = first.text(), second = second)
}

/**
 * Evaluates if query result [first] doesn't exist.
 * @param first query result to be evaluated
 * @return the result of predicate
 */
internal fun notExist(first: QueryResult): PredicateResult {
    val succeeded =  when (first) {
        is QueryNoneResult -> true
        is QueryNodeSetResult -> first.size == 0
        else -> false
    }
    val second = ""
    return PredicateResult(succeeded = succeeded, first = first.text(), second = second)
}
