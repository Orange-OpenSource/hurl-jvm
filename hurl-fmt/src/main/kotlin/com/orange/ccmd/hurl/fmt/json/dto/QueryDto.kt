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

import com.orange.ccmd.hurl.core.ast.BodyQuery
import com.orange.ccmd.hurl.core.ast.CookieQuery
import com.orange.ccmd.hurl.core.ast.DurationQuery
import com.orange.ccmd.hurl.core.ast.HeaderQuery
import com.orange.ccmd.hurl.core.ast.JsonPathQuery
import com.orange.ccmd.hurl.core.ast.Query
import com.orange.ccmd.hurl.core.ast.RegexQuery
import com.orange.ccmd.hurl.core.ast.StatusQuery
import com.orange.ccmd.hurl.core.ast.VariableQuery
import com.orange.ccmd.hurl.core.ast.XPathQuery
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed class QueryDto {
    abstract val subquery: SubqueryDto?
}

@Serializable
@SerialName("body")
data class BodyQueryDto(
    override val subquery: SubqueryDto? = null
): QueryDto()

@Serializable
@SerialName("cookie")
data class CookieQueryDto(
    val expr: String,
    override val subquery: SubqueryDto? = null
) : QueryDto()

@Serializable
@SerialName("status")
data class StatusQueryDto(
    override val subquery: SubqueryDto? = null
) : QueryDto()

@Serializable
@SerialName("header")
data class HeaderQueryDto(
    val name: String,
    override val subquery: SubqueryDto? = null
) : QueryDto()

@Serializable
@SerialName("jsonpath")
data class JsonPathQueryDto(
    val expr: String,
    override val subquery: SubqueryDto? = null,
) : QueryDto()

@Serializable
@SerialName("regex")
data class RegexQueryDto(
    val expr: String,
    override val subquery: SubqueryDto? = null
) : QueryDto()

@Serializable
@SerialName("variable")
data class VariableQueryDto(
    val name: String,
    override val subquery: SubqueryDto? = null
) : QueryDto()

@Serializable
@SerialName("xpath")
data class XPathQueryDto(
    val expr: String,
    override val subquery: SubqueryDto? = null
) : QueryDto()

@Serializable
@SerialName("duration")
data class DurationQueryDto(
    override val subquery: SubqueryDto? = null
) : QueryDto()


fun Query.toQueryDto(): QueryDto {
    val subqueryDto = subquery?.toSubqueryDto()
    return when (this) {
        is BodyQuery -> BodyQueryDto(subquery = subqueryDto)
        is CookieQuery -> CookieQueryDto(expr = expr.value, subquery = subqueryDto)
        is HeaderQuery -> HeaderQueryDto(name = headerName.value, subquery = subqueryDto)
        is JsonPathQuery -> JsonPathQueryDto(expr = expr.value, subquery = subqueryDto)
        is RegexQuery -> RegexQueryDto(expr = expr.value, subquery = subqueryDto)
        is StatusQuery -> StatusQueryDto(subquery = subqueryDto)
        is VariableQuery -> VariableQueryDto(name = variable.value, subqueryDto)
        is XPathQuery -> XPathQueryDto(expr = expr.value, subquery = subqueryDto)
        is DurationQuery -> DurationQueryDto(subquery = subqueryDto)
    }
}
