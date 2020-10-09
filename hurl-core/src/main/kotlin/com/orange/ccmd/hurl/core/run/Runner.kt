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
import com.orange.ccmd.hurl.core.http.HttpRequest
import com.orange.ccmd.hurl.core.http.HttpResult
import com.orange.ccmd.hurl.core.http.Proxy
import com.orange.ccmd.hurl.core.http.impl.ApacheHttpClient
import com.orange.ccmd.hurl.core.run.experimental.Command
import com.orange.ccmd.hurl.core.run.log.RunnerLogger
import com.orange.ccmd.hurl.core.variable.VariableJar
import com.orange.ccmd.hurl.core.template.InvalidVariableException
import java.io.File
import java.io.FileNotFoundException
import java.time.Duration
import java.time.Instant


/**
 * Hurl HTTP runner.
 *
 * This class take a parsed Hurl file, and run all entries.
 *
 * @property hurlFile hurl file describing requests and response of this session
 * @property variables map of variable (pair of string for name and value), that are injected in the
 * construction of a runner, and latter augmented by captures during the execution
 * of a session.
 * @property fileRoot root directory for body file includes. Default is hurlFile directory.
 * @property allowsInsecure allow connections to SSL sites without certs. Default is false.
 * @property proxy use proxy on given port (ex: localhost:3128)
 * @property outputHeaders include protocol headers in the output. Default is false.
 * @property followsRedirect follow redirect (HTTP 3xx status code). Default is false.
 * @property verbose turn off/on verbosity on log message
 */
data class Runner(
    val hurlFile: HurlFile,
    val variables: Map<String, String> = emptyMap(),
    val fileRoot: File,
    val outputHeaders: Boolean = false,
    val verbose: Boolean = false,
    val allowsInsecure: Boolean = false,
    val proxy: String? = null,
    val followsRedirect: Boolean = false,
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

        runExperimentalCommands(entry)

        // First, we construct the HTTP request.
        var requestSpec = try {
            entry.request.toHttpRequestSpec(variables = variableJar, fileRoot = fileRoot)
        } catch (e: InvalidVariableException) {
            return EntryResult(errors = listOf(InvalidVariableResult(position = e.position, reason = e.reason)))
        } catch (e: FileNotFoundException) {
            // We catch this exception because the evaluation of a request can
            // raise a FileNotFoundException when using a file body node.
            // TODO: create and use here a custom RuntimeErrorException
            return EntryResult(errors = listOf(RuntimeErrorResult(position = entry.request.begin, message = e.message)))
        }


        var httpResult: HttpResult

        // Executes request, follow redirection if necessary.
        while (true) {

            runLogger.logHttpRequestSpec(requestSpec)

            httpResult = try {
                httpClient.execute(requestSpec)
            } catch (e: Exception) {
                return EntryResult(errors = listOf(RuntimeErrorResult(position = entry.request.method.begin, message = e.message)))
            }

            runLogger.logHttpRequest(httpResult.finalizedRequest)
            runLogger.logHttpResponse(httpResult.response)
            runLogger.logCookies(httpResult.cookies)

            if (followsRedirect && httpResult.response.code >= 300 && httpResult.response.code < 400) {
                val header = httpResult.response.headers
                    .firstOrNull { (k, _) -> k.toLowerCase() == "location" }
                    ?: return EntryResult(errors = listOf(RuntimeErrorResult(position = entry.request.method.begin, message = "Unable to get Location header")))
                requestSpec = HttpRequest(method = "GET", url = header.second)
                continue
            }
            break
        }

        val responseSpec = entry.response
        val httpResponse = httpResult.response
        if (responseSpec == null) {
            return EntryResult(httpResponse = httpResponse)
        }

        // First evaluate all capture results and update variable map accordingly.
        val captureResults = responseSpec.getCaptureResults(
            variables = variableJar,
            httpResponse = httpResponse
        )
        val captureVariables = captureResults.getCaptureVariables()
        for ((name, queryResult) in captureVariables) {
            when (queryResult) {
                is QueryStringResult -> variableJar.add(name = name, value = queryResult.value)
                is QueryBooleanResult -> variableJar.add(name = name, value = queryResult.value)
                is QueryNumberResult -> variableJar.add(name = name, value = queryResult.value)
                is QueryListResult -> variableJar.add(name = name, value = queryResult.value)
                is QueryObjectResult -> variableJar.add(name = name, value = queryResult.value)
                // For node set, as we currently don't have any QueryNodeSetResult.values,
                // we directly add the node set result to the variable jar.
                is QueryNodeSetResult -> variableJar.add(name = name, value = queryResult)
                is QueryNoneResult -> {}
            }
        }

        // Then, evaluate HTTP, status, header, body and assert.
        val versionResult = responseSpec.getCheckVersionResult(httpResponse = httpResponse)
        val statusResult = responseSpec.getCheckStatusCodeResult(httpResponse = httpResponse)
        val headersResults = responseSpec.getCheckHeadersResults(variables = variableJar, httpResponse = httpResponse)
        val assertsResults = responseSpec.getAssertsResults(variables = variableJar, httpResponse = httpResponse)
        val bodyResult = responseSpec.getCheckBodyResult(variables = variableJar, fileRoot = fileRoot, httpResponse = httpResponse)

        return EntryResult(
            httpResponse = httpResponse,
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

    /**
     * (Experimental) Parse and run commands for a given entry.
     *
     * Experimental commands are not part of the ast; for the moment
     * we try to parse comment and run commands.
     * @param entry entry that contain commands
     */
    private fun runExperimentalCommands(entry: Entry) {
        entry.request.lts
            .mapNotNull { it.comment }
            .mapNotNull { Command.fromString(it.value) }
            .forEach { it.run(httpClient) }
    }
}