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

import com.orange.ccmd.hurl.core.ast.BodyQuery
import com.orange.ccmd.hurl.core.ast.CookieQuery
import com.orange.ccmd.hurl.core.ast.HeaderQuery
import com.orange.ccmd.hurl.core.ast.JsonPathQuery
import com.orange.ccmd.hurl.core.ast.Query
import com.orange.ccmd.hurl.core.ast.RegexQuery
import com.orange.ccmd.hurl.core.ast.StatusQuery
import com.orange.ccmd.hurl.core.ast.VariableQuery
import com.orange.ccmd.hurl.core.ast.XPathQuery
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.query.InvalidQueryException
import com.orange.ccmd.hurl.core.query.cookiepath.CookiePath
import com.orange.ccmd.hurl.core.query.cookiepath.CookiePathBooleanResult
import com.orange.ccmd.hurl.core.query.cookiepath.CookiePathFailed
import com.orange.ccmd.hurl.core.query.cookiepath.CookiePathNumberResult
import com.orange.ccmd.hurl.core.query.cookiepath.CookiePathResult
import com.orange.ccmd.hurl.core.query.cookiepath.CookiePathStringResult
import com.orange.ccmd.hurl.core.query.jsonpath.JsonArray
import com.orange.ccmd.hurl.core.query.jsonpath.JsonBoolean
import com.orange.ccmd.hurl.core.query.jsonpath.JsonNull
import com.orange.ccmd.hurl.core.query.jsonpath.JsonNumber
import com.orange.ccmd.hurl.core.query.jsonpath.JsonObject
import com.orange.ccmd.hurl.core.query.jsonpath.JsonPath
import com.orange.ccmd.hurl.core.query.jsonpath.JsonPathOk
import com.orange.ccmd.hurl.core.query.jsonpath.JsonPathResult
import com.orange.ccmd.hurl.core.query.jsonpath.JsonString
import com.orange.ccmd.hurl.core.query.xpath.XPath
import com.orange.ccmd.hurl.core.query.xpath.XPathBooleanResult
import com.orange.ccmd.hurl.core.query.xpath.XPathNodeSetResult
import com.orange.ccmd.hurl.core.query.xpath.XPathNumberResult
import com.orange.ccmd.hurl.core.query.xpath.XPathResult
import com.orange.ccmd.hurl.core.query.xpath.XPathStringResult
import com.orange.ccmd.hurl.core.template.Template
import java.net.HttpCookie
import java.util.regex.PatternSyntaxException

fun Query.eval(response: HttpResponse, variables: VariableJar): QueryResult = when (this) {
    is StatusQuery -> this.eval(response = response)
    is HeaderQuery -> this.eval(response = response)
    is CookieQuery -> this.eval(response = response, variables = variables)
    is BodyQuery -> this.eval(response = response)
    is XPathQuery -> this.eval(response = response, variables = variables)
    is JsonPathQuery -> this.eval(response = response, variables = variables)
    is RegexQuery -> this.eval(response = response, variables = variables)
    is VariableQuery -> this.eval(variables = variables)
}

fun XPathResult.toQueryResult(): QueryResult = when (this) {
    is XPathBooleanResult -> QueryBooleanResult(value = value)
    is XPathNumberResult -> QueryNumberResult(value = value)
    is XPathStringResult -> QueryStringResult(value = value)
    is XPathNodeSetResult -> QueryNodeSetResult(size = size)
}

fun JsonPathResult.toQueryResult(): QueryResult {
    return if (this is JsonPathOk) {
        when (result) {
            is JsonBoolean -> QueryBooleanResult(value = result.value)
            is JsonNumber -> QueryNumberResult(value = result.value)
            is JsonString -> QueryStringResult(value = result.value)
            is JsonArray -> QueryListResult(size = result.value.size)
            is JsonObject -> QueryObjectResult(value = result.value)
            is JsonNull -> QueryObjectResult(value = null)
        }
    } else {
        QueryNoneResult
    }
}

fun CookiePathResult.toQueryResult(): QueryResult = when (this) {
    is CookiePathStringResult -> QueryStringResult(value = value)
    is CookiePathNumberResult -> QueryNumberResult(value = value)
    is CookiePathBooleanResult -> QueryBooleanResult(value = value)
    CookiePathFailed -> QueryNoneResult
}

/**
 * Evaluates a spec {%link StatusQuery} against a HTTP [response].
 * @param response an input HTTP response
 * @return a {%link QueryNumberResult} representing the query result.
 */
internal fun StatusQuery.eval(response: HttpResponse): QueryNumberResult = QueryNumberResult(response.code)

/**
 * Evaluates a spec {%link HeaderQuery} against a HTTP [response].
 * @param response an input HTTP response
 * @return a {%link QueryResult} representing the query result
 */
internal fun HeaderQuery.eval(response: HttpResponse): QueryResult {
    val name = headerName.value
    val headers = response.headers.filter { it.first.toLowerCase() == name.toLowerCase() }
    return when {
        headers.isEmpty() -> QueryNoneResult
        headers.size == 1 -> QueryStringResult(headers[0].second)
        else -> QueryListResult(size = headers.size)
    }
}

/**
 * Evaluates a spec {%link CookieQuery} against a HTTP [response].
 * @param response an input HTTP response
 * @return a {%link QueryResult} representing the query result
 */
internal fun CookieQuery.eval(response: HttpResponse, variables: VariableJar): QueryResult {
    val exprRendered = Template.render(text = expr.value, variables = variables, position = expr.begin)
    return CookiePath.evaluate(expr = exprRendered, headers = response.headers).toQueryResult()
}

/**
 * Evaluates a spec {%link BodyQuery} against a HTTP [response].
 * If the body can not be decoded with the HTTP response charsetn an InvalidQueryException
 * is thrown.
 * @param response an input HTTP response
 * @return a {%link QueryStringResult} representing the query result
 */
internal fun BodyQuery.eval(response: HttpResponse): QueryStringResult {
    val body = response.bodyAsText
    return QueryStringResult(body)
}

/**
 * Evaluates a spec {%link XPathQuery} against a HTTP [response].
 * @param response an input HTTP response
 * @param variables ariables to use in templates
 * @return a {%link QueryResult} representing the query result
 */
internal fun XPathQuery.eval(response: HttpResponse, variables: VariableJar): QueryResult {
    val body = response.bodyAsText
    val exprRendered = Template.render(text = expr.value, variables = variables, position = expr.begin)
    return XPath.evaluateHtml(expr = exprRendered, body = body).toQueryResult()
}

/**
 * Evaluates a spec {%link JsonPathQuery} against a HTTP [response].
 * @param response an input HTTP response
 * @param variables variables to use in templates
 * @return a {%link QueryResult} representing the query result
 */
internal fun JsonPathQuery.eval(response: HttpResponse, variables: VariableJar): QueryResult {
    val body = response.bodyAsText
    val exprRendered = Template.render(text = expr.value, variables = variables, position = expr.begin)
    return JsonPath.evaluate(expr = exprRendered, json = body).toQueryResult()
}

/**
 * Evaluates a spec {%link RegexQuery} against a HTTP [response].
 * @param response an input HTTP response
 * @param variables variables to use in templates
 * @return a {%link QueryResult} representing the query result
 */
internal fun RegexQuery.eval(response: HttpResponse, variables: VariableJar): QueryResult {
    val body = response.bodyAsText
    val pattern = Template.render(text = expr.value, variables = variables, position = expr.begin)
    val regex = try {
        Regex(pattern = pattern)
    } catch (e : PatternSyntaxException) {
        throw InvalidQueryException("invalid regex \"$pattern\"")
    }
    val matchResult = regex.find(body)
    if (matchResult == null || matchResult.groupValues.size <= 1) {
        return QueryNoneResult
    }
    return QueryStringResult(value = matchResult.groupValues[1])
}

private val HttpResponse.bodyAsText: String
    get() = text ?: throw InvalidQueryException("body can not be decoded with charset $charset")

/**
 * Evaluates a spec {%link VariableQuery} against a variable jar [variables].
 * @param variables variables to use in templates
 * @return a {%link QueryResult} representing the query result
 */
internal fun VariableQuery.eval(variables: VariableJar): QueryResult = variables[variable.value] ?: QueryNoneResult
