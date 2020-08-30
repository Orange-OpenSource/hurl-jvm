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

import com.orange.ccmd.hurl.core.ast.Response
import com.orange.ccmd.hurl.core.http.HttpResponse
import java.io.File

/**
 * Returns the capture results from the captures section of this response spec, against
 * a real HTTP response [httpResponse], using [variables] for templating.
 * @param variables variables to use in templates
 * @param httpResponse the actual HTTP response
 * @return a list of {@link EntryResult} for each capture
 */
internal fun Response.getCaptureResults(variables: VariableJar, httpResponse: HttpResponse): List<EntryStepResult> {
    return capturesSection?.captures?.map {
        it.eval(response = httpResponse, variables = variables)
    } ?: emptyList()
}

fun List<EntryStepResult>.getCaptureVariables(): Map<String, QueryResult> {
    return filterIsInstance<CaptureResult>()
        .filter { it.succeeded }
        .map { it.variable to (it.value ?: QueryNoneResult) }
        .toMap()
}

/**
 * Check the version of this response spec, against a real HTTP response [httpResponse].
 * @param httpResponse the actual HTTP response
 * @return a {@link EntryResult}
 */
internal fun Response.getCheckVersionResult(httpResponse: HttpResponse): EntryStepResult = version.checkVersion(httpResponse.version)

/**
 * Check the status code of this response spec, against a real HTTP response [httpResponse].
 * @param httpResponse the actual HTTP response
 * @return a {@link EntryResult}
 */
internal fun Response.getCheckStatusCodeResult(httpResponse: HttpResponse): EntryStepResult = status.checkStatusCode(httpResponse.code)

/**
 * Check the headers of this response spec, against a real HTTP response [httpResponse],
 * using [variables] for templating.
 * @param variables variables to use in templates
 * @param httpResponse the actual HTTP response
 * @return a list of {@link EntryResult} for each spec header
 */
internal fun Response.getCheckHeadersResults(
    variables: VariableJar,
    httpResponse: HttpResponse
): List<EntryStepResult> = headers
    .map { it.checkHeader(headers = httpResponse.headers, variables = variables) }

/**
 * Check the body of this response spec, against a real HTTP response [httpResponse],
 * using [variables] for templating.
 * @param variables variables to use in templates
 * @param fileRoot root directory for File body node
 * @param httpResponse the actual HTTP response
 * @return a {@link EntryResult}
 */
internal fun Response.getCheckBodyResult(variables: VariableJar, fileRoot: File, httpResponse: HttpResponse): EntryStepResult? {
    return body?.checkBodyContent(
        body = httpResponse.body,
        variables = variables,
        fileRoot = fileRoot
    )
}

/**
 * Check the explicit asserts of this response spec, against a real HTTP response [httpResponse],
 * using [variables] for templating.
 * @param variables variables to use in templates
 * @param httpResponse the actual HTTP response
 * @return a list of {@link EntryResult} for each spec assert
 */
internal fun Response.getAssertsResults(variables: VariableJar, httpResponse: HttpResponse): List<EntryStepResult> {
    return assertsSection?.asserts?.map {
        it.eval(response = httpResponse, variables = variables)
    } ?: emptyList()
}

