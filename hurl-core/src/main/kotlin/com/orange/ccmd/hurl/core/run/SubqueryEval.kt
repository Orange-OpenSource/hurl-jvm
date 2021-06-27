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

import com.orange.ccmd.hurl.core.ast.CountSubquery
import com.orange.ccmd.hurl.core.ast.RegexSubquery
import com.orange.ccmd.hurl.core.ast.Subquery
import com.orange.ccmd.hurl.core.variable.VariableJar
import com.orange.ccmd.hurl.core.template.Template

class InvalidSubqueryException(message: String) : Exception(message)

internal fun Subquery.eval(queryResult: QueryResult, variables: VariableJar): QueryResult = when (this) {
    is CountSubquery -> this.eval(queryResult = queryResult)
    is RegexSubquery -> this.eval(queryResult = queryResult, variables = variables)
}

internal fun CountSubquery.eval(queryResult: QueryResult): QueryResult {
    return when (queryResult) {
        is QueryListResult -> QueryNumberResult(queryResult.value.size)
        is QueryNodeSetResult -> QueryNumberResult(queryResult.size)
        QueryNoneResult -> QueryNumberResult(0)
        else -> throw InvalidSubqueryException("count subquery is incompatible with query result")
    }
}

internal fun RegexSubquery.eval(queryResult: QueryResult, variables: VariableJar): QueryResult {
    if (queryResult !is QueryStringResult) {
        throw InvalidSubqueryException("regex subquery expects a query string result")
    }
    val text = queryResult.value
    val exprRendered = Template.render(text = expr.value, variables = variables, position = expr.begin)
    val regex = Regex(pattern = exprRendered)
    val matchResult = regex.matchEntire(text)
    if (matchResult == null || matchResult.groupValues.size <= 1) {
        throw InvalidSubqueryException("regex subquery must have at least one group")
    }
    return QueryStringResult(value = matchResult.groupValues[1])
}