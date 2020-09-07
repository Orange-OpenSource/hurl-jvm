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

import com.orange.ccmd.hurl.core.ast.Assert
import com.orange.ccmd.hurl.core.ast.Body
import com.orange.ccmd.hurl.core.ast.ContainPredicate
import com.orange.ccmd.hurl.core.ast.CountPredicate
import com.orange.ccmd.hurl.core.ast.EqualBoolPredicate
import com.orange.ccmd.hurl.core.ast.EqualNumberPredicate
import com.orange.ccmd.hurl.core.ast.EqualStringPredicate
import com.orange.ccmd.hurl.core.ast.ExistPredicate
import com.orange.ccmd.hurl.core.ast.Header
import com.orange.ccmd.hurl.core.ast.IncludeBoolPredicate
import com.orange.ccmd.hurl.core.ast.IncludeNumberPredicate
import com.orange.ccmd.hurl.core.ast.IncludeStringPredicate
import com.orange.ccmd.hurl.core.ast.MatchPredicate
import com.orange.ccmd.hurl.core.ast.StartWithPredicate
import com.orange.ccmd.hurl.core.ast.Status
import com.orange.ccmd.hurl.core.ast.Version
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.query.InvalidQueryException
import com.orange.ccmd.hurl.core.template.InvalidVariableException
import com.orange.ccmd.hurl.core.template.Template
import com.orange.ccmd.hurl.core.utils.shorten
import java.io.File
import java.io.FileNotFoundException

/**
 * Check equality of a given [version] against an expected {@link Version}
 * @param version actual HTTP version (ex HTTP/1.0)
 * @return the assert result
 */
internal fun Version.checkVersion(version: String): AssertResult =
    when (value) {
        version -> AssertResult(
            succeeded = true,
            position = begin,
            message = "assert http version equals $value succeeded"
        )

        "HTTP/*" -> AssertResult(
            succeeded = true,
            position = begin,
            message = "assert http version succeeded"
        )

        else -> AssertResult(
            succeeded = false,
            position = begin,
            message = "assert http version equals failed\n  actual:   $version\n  expected: $value"
        )
    }

/**
 * Check equality of a given [statusCode] against an expected {@link Status}
 * @param statusCode actual HTTP response status code (ex 200)
 * @return the assert result
 */
internal fun Status.checkStatusCode(statusCode: Int): AssertResult =
    if (value == statusCode) {
        AssertResult(
            succeeded = true,
            position = begin,
            message = "assert status code equals $value succeeded"
        )
    } else {
        AssertResult(
            succeeded = false,
            position = begin,
            message = "assert status code equals failed\n  actual:   $statusCode\n  expected: $value"
        )
    }

/**
 * Check if an expected {@link Header} is present and equals in a list of given HTTP [headers].
 * Received HTTP headers can contains multiple values for the a single key.
 * @param headers actual HTTP headers, a list of key-value string
 * @param variables map of input variables referenced by the expected header
 * @return the assert result
 */
internal fun Header.checkHeader(headers: List<Pair<String, String>>, variables: VariableJar): EntryStepResult {

    val valueExpected = try {
        Template.render(text = value, variables = variables, position = keyValue.value.begin)
    } catch (e: InvalidVariableException) {
        return InvalidVariableResult(position = e.position, reason = e.reason)
    }

    // We filter all received headers against the spec header name.
    val filteredHeaders = headers.filter { it.first.toLowerCase() == name.toLowerCase() }
    if (filteredHeaders.isEmpty()) {
        return AssertResult(
            succeeded = false,
            position = begin,
            message = "assert header equals failed\n  actual:\n  expected: $valueExpected"
        )
    }

    // In the filtered headers, we search the first received header whose value
    // match the spec one. If we found any, assert is succeed; otherwise assert is
    // failed and we use the filtered headers for message failure.
    val header = filteredHeaders.firstOrNull { it.second == valueExpected }
    return if (header != null) {
        AssertResult(
            succeeded = true,
            position = begin,
            message = "assert header $name succeeded"
        )
    } else {
        val actual = filteredHeaders.joinToString(", ") { it.second }
        AssertResult(
            succeeded = false,
            position = begin,
            message = "assert header equals failed\n  actual:   ${actual}\n  expected: $valueExpected"
        )
    }
}

/**
 * Check if an expected {@link Body} is equals to a given byte array [body].
 * The expected body can be templated with [variables], or be the content of a file
 * relative to [fileRoot].
 * @param variables variables referenced by the expected header
 * @param fileRoot root directory for File body node
 * @return the assert result
 */
internal fun Body.checkBodyContent(body: ByteArray, variables: VariableJar, fileRoot: File): EntryStepResult {
    val expectedBytes = try {
        bytes.toByteArray(fileRoot = fileRoot, variables = variables)
    } catch (e: FileNotFoundException) {
        return RuntimeErrorResult(position = bytes.begin, e)
    }
    return if (body.contentEquals(expectedBytes)) {
        AssertResult(
            succeeded = true,
            position = begin,
            message = "assert body equals succeeded"
        )
    } else {
        val expected = String(expectedBytes).shorten()
        val actual = String(body).shorten()
        AssertResult(
            succeeded = false,
            position = begin,
            message = "assert body equals failed\n  actual:   $actual\n  expected: $expected"
        )
    }
}

internal fun Assert.eval(response: HttpResponse, variables: VariableJar): EntryStepResult {
    // Evaluate actual value against the http response.
    val first = try {
        query.eval(response = response, variables = variables)
    } catch (e: InvalidQueryException) {
        return AssertResult(
            succeeded = false,
            position = begin,
            message = "assert ${query.type.value} evaluation failed, ${e.message}"
        )
    } catch (e: InvalidVariableException) {
        return InvalidVariableResult(position = e.position, reason = e.reason)
    }

    val not = predicate.not != null

    // Renders predicate value if necessary.
    val predicateFunc = predicate.predicateFunc
    val result = try {
        when (predicateFunc) {
            is EqualBoolPredicate -> predicateFunc.eval(not = not, first = first, second = predicateFunc.expr.value)
            is EqualNumberPredicate -> predicateFunc.eval(not = not, first = first, second = predicateFunc.expr.value)
            is EqualStringPredicate -> {
                val second = predicateFunc.valueToString(variables = variables)
                predicateFunc.eval(not = not, first = first, second = second)
            }
            is StartWithPredicate -> {
                val second = predicateFunc.valueToString(variables = variables)
                predicateFunc.eval(not = not, first = first, second = second)
            }
            is CountPredicate -> predicateFunc.eval(not = not, first = first, second = predicateFunc.expr.value)
            is ContainPredicate -> {
                val second = predicateFunc.valueToString(variables = variables)
                predicateFunc.eval(not = not, first = first, second = second)
            }
            is IncludeBoolPredicate -> predicateFunc.eval(not = not, first = first, second = predicateFunc.expr.value)
            is IncludeNumberPredicate -> predicateFunc.eval(not = not, first = first, second = predicateFunc.expr.value)
            is IncludeStringPredicate -> {
                val second = predicateFunc.valueToString(variables = variables)
                predicateFunc.eval(not = not, first = first, second = second)
            }
            is MatchPredicate -> {
                val second = predicateFunc.valueToString(variables = variables)
                predicateFunc.eval(not = not, first = first, second = second)
            }
            is ExistPredicate -> predicateFunc.eval(not = not, first = first)
        }
    } catch (e: InvalidVariableException) {
        return InvalidVariableResult(position = e.position, reason = e.reason)
    }

    return AssertResult(succeeded = result.succeeded, position = begin, message = buildMessage(result))
}

private fun Assert.buildMessage(result: PredicateResult): String {
    val predicateFunc = predicate.predicateFunc
    val state = if (result.succeeded) { "succeeded" } else { "failed" }
    val not = if (predicate.not != null) { "not " } else { "" }
    return """
        assert ${query.type.value} $not${predicateFunc.type.value} $state
          actual:   ${result.first}
          expected: ${result.second}
    """.trimIndent()
}
