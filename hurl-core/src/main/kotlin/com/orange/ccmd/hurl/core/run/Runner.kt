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

import com.orange.ccmd.hurl.core.ast.Entry
import com.orange.ccmd.hurl.core.ast.HurlFile
import com.orange.ccmd.hurl.core.http.HttpClient
import com.orange.ccmd.hurl.core.http.Proxy
import com.orange.ccmd.hurl.core.http.impl.ApacheHttpClient
import com.orange.ccmd.hurl.core.run.log.RunnerLogger
import com.orange.ccmd.hurl.core.template.InvalidVariableException
import java.io.File
import java.time.Duration
import java.time.Instant


/**
 * Hurl HTTP runner.
 * @property hurlFile hurl file describing requests and response of this session
 * @property variables map of variable (pair of string for name and value), that are injected in the
 *                      construction of a runner, and latter augmented by captures during the execution
 *                      of a session.
 * @property fileRoot root directory for body file includes. Default is hurlFile directory.
 * @property allowsInsecure allow connections to SSL sites without certs. Default is false.
 * @property proxy use proxy on given port (ex: localhost:3128)
 * @property outputHeaders include protocol headers in the output. Default is false.
 */
class Runner(
    val hurlFile: HurlFile,
    variables: Map<String, String> = emptyMap(),
    val fileRoot: File,
    val outputHeaders: Boolean = false,
    val verbose: Boolean = false,
    val allowsInsecure: Boolean = false,
    val proxy: String? = null,
    val runLogger: RunnerLogger = RunnerLogger(outputHeaders = outputHeaders, verbose = verbose)
) {
    private val httpClient: HttpClient
    private val variableJar: VariableJar

    init {
        val httpProxy = if (proxy != null) {
            Proxy.fromString(proxy)
        } else {
            null
        }
        httpClient = ApacheHttpClient(
            allowsInsecure = allowsInsecure,
            httpProxy = httpProxy
        )

        variableJar = VariableJar.from(variables)
    }

    /**
     * Run the hurl file [hurlFile], with the [variables] context.
     * @return a {@link RunResult} for this session.
     */
    fun run(): RunResult {
        val start = Instant.now()
        val results = mutableListOf<EntryResult>()
        val entries = hurlFile.entries

        // Process each entry in the hurlFile, and stop at
        // the first failure.
        for (entry in entries) {
            val result = runEntry(entry)
            results.add(result)

            // Stop if the current entry has any failure.
            if (!result.succeeded) {
                return RunResult(duration = Duration.between(start, Instant.now()), entryResults = results)
            }
        }

        return RunResult(duration = Duration.between(start, Instant.now()), entryResults = results)
    }


    /**
     * Run the [entry].
     * @return
     */
    private fun runEntry(entry: Entry): EntryResult {

        // First, we construct the HTTP request.
        val requestSpec = try {
            entry.request.toHttpRequestSpec(variables = variableJar, fileRoot = fileRoot)
        } catch (e: InvalidVariableException) {
            return EntryResult(errors = listOf(InvalidVariableResult(position = e.position, reason = e.reason)))
        }

        runLogger.logHttpRequestSpec(requestSpec)

        // Then, execute the HTTP request.
        val httpResult = try {
            httpClient.execute(requestSpec)
        } catch (e: Exception) {
            return EntryResult(errors = listOf(RuntimeErrorResult(position = entry.request.method.begin, error = e)))
        }

        with (httpResult) {
            runLogger.logHttpRequest(requestLog)
            runLogger.logHttpResponse(response)
            runLogger.logCookies(cookies)
        }

        val responseSpec = entry.response
        val httpResponse = httpResult.response
        if (responseSpec == null) {
            return EntryResult()
        }

        // First evaluate all capture results and update variable map accordingly.
        val captureResults = responseSpec.getCaptureResults(
            variables = variableJar,
            httpResponse = httpResponse
        )
        val captureVariables = captureResults.getCaptureVariables()
        variableJar.addAll(captureVariables)

        // Then, evaluate HTTP, status, header, body and assert.
        val versionResult = responseSpec.getCheckVersionResult(httpResponse = httpResponse)
        val statusResult = responseSpec.getCheckStatusCodeResult(httpResponse = httpResponse)
        val headersResults = responseSpec.getCheckHeadersResults(variables = variableJar, httpResponse = httpResponse)
        val bodyResult = responseSpec.getCheckBodyResult(variables = variableJar, fileRoot = fileRoot, httpResponse = httpResponse)
        val assertsResults = responseSpec.getAssertsResults(variables = variableJar, httpResponse = httpResponse)

        return EntryResult(
            captures = captureResults,
            asserts = listOfNotNull(
                versionResult,
                statusResult,
                *headersResults.toTypedArray(),
                bodyResult,
                *assertsResults.toTypedArray()
            )
        )
    }


}