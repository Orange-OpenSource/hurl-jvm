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

package com.orange.ccmd.hurl.fmt.json.dto

import com.orange.ccmd.hurl.core.ast.CountSubquery
import com.orange.ccmd.hurl.core.ast.RegexSubquery
import com.orange.ccmd.hurl.core.ast.Subquery
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SubqueryDto

@Serializable
@SerialName("regex")
data class RegexSubqueryDto(
    val expr: String
) : SubqueryDto()

@Serializable
@SerialName("count")
object CountSubqueryDto : SubqueryDto()

fun Subquery.toSubqueryDto(): SubqueryDto {
    return when (this) {
        is CountSubquery -> CountSubqueryDto
        is RegexSubquery -> RegexSubqueryDto(expr = expr.value)
    }
}
