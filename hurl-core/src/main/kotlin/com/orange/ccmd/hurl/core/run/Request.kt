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

import com.orange.ccmd.hurl.core.ast.Json
import com.orange.ccmd.hurl.core.ast.Request
import com.orange.ccmd.hurl.core.ast.Xml
import com.orange.ccmd.hurl.core.http.BinaryRequestBody
import com.orange.ccmd.hurl.core.http.HttpRequest
import com.orange.ccmd.hurl.core.http.JsonRequestBody
import com.orange.ccmd.hurl.core.http.XmlRequestBody
import com.orange.ccmd.hurl.core.variable.VariableJar
import java.io.File

/**
 * Create a HTTP spec request from a {%link Request%} node.
 * @param variables variables to use in templates
 * @param fileRoot root directory for File body node
 * @returna a spec request
 */
internal fun Request.toHttpRequestSpec(variables: VariableJar, fileRoot: File): HttpRequest {
    val method = method.value
    val url = url.toUrl(variables)
    val queryStringParams = queryStringParamsSection?.params?.map { it.toQueryStringParam(variables) } ?: emptyList()
    val headers = headers.map { it.toHeader(variables) }
    val data = body?.bytes?.toByteArray(variables = variables, fileRoot = fileRoot)
    val requestBody  = when {
        body == null || data == null -> { null }
        body.bytes is Json -> { JsonRequestBody(data) }
        body.bytes is Xml -> { XmlRequestBody(data) }
        else -> { BinaryRequestBody(data) }
    }
    val formParams = formParamsSection?.params?.map { it.toFormParam(variables) } ?: emptyList()
    val multipartFormDatas = multipartFormDataSection?.toFormDatas(variables = variables, fileRoot = fileRoot) ?: emptyList()
    val cookies = cookiesSection?.cookies?.map { it.toCookie(variables) } ?: emptyList()
    return HttpRequest(
        method = method,
        url = url,
        queryStringParams = queryStringParams,
        headers = headers,
        body = requestBody,
        formParams = formParams,
        multipartFormDatas = multipartFormDatas,
        cookies = cookies
    )
}

