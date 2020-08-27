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

import com.orange.ccmd.hurl.core.ast.RegexSubquery
import com.orange.ccmd.hurl.core.ast.Subquery
import com.orange.ccmd.hurl.core.template.Template

class InvalidSubqueryException(message: String) : Exception(message)

internal fun Subquery.eval(text: String, variables: VariableJar): QueryResult = when (this) {
    is RegexSubquery -> this.eval(text = text, variables = variables)
}

internal fun RegexSubquery.eval(text: String, variables: VariableJar): QueryResult {
    val exprRendered = Template.render(text = expr.value, variables = variables, position = expr.begin)
    val regex = Regex(pattern = exprRendered)
    val matchResult = regex.matchEntire(text)
    if (matchResult == null || matchResult.groupValues.size <= 1) {
        throw InvalidSubqueryException("subquery must have at least one group")
    }
    return QueryStringResult(value = matchResult.groupValues[1])
}