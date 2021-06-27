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
import com.orange.ccmd.hurl.core.http.BasicAuthentification
import com.orange.ccmd.hurl.core.http.HttpClient
import com.orange.ccmd.hurl.core.http.HttpRequest
import com.orange.ccmd.hurl.core.http.HttpResult
import com.orange.ccmd.hurl.core.http.Proxy
import com.orange.ccmd.hurl.core.http.impl.ApacheHttpClient
import com.orange.ccmd.hurl.core.run.experimental.Command
import com.orange.ccmd.hurl.core.run.log.Logger
import com.orange.ccmd.hurl.core.template.InvalidVariableException
import com.orange.ccmd.hurl.core.variable.VariableJar
import java.io.FileNotFoundException
import java.time.Duration
import java.time.Instant
import java.util.*


/**
 * Hurl HTTP runner.
 *
 * This class take a parsed Hurl file, and run all entries.
 *
 * @property hurlFile Hurl file describing requests and response of this session
 * @property options options for this runner
 */
data class Runner(
    val hurlFile: HurlFile,
    val options: Options,
) {
    private val httpClient: HttpClient
    private val variableJar: VariableJar
    private val logger: Logger = Logger(verbose = options.verbose)


    init {
        val httpProxy = if (options.proxy != null) {
            Proxy.fromString(options.proxy)
        } else {
            null
        }
        val authentification = if (options.user != null) {
            BasicAuthentification.fromString(options.user)
        } else {
            null
        }
        httpClient = ApacheHttpClient(
            allowsInsecure = options.allowsInsecure,
            httpProxy = httpProxy,
            authentification = authentification,
            compressed = options.compressed,
            connectTimeoutInSecond = options.connectTimeoutInSecond,
            maxTime = options.maxTime
        )

        variableJar = VariableJar.from(options.variables)
    }

    /**
     * Run the Hurl file [hurlFile], with the [variables] context.
     * @return a {@link RunResult} for this session.
     */
    fun run(): RunResult {
        val start = Instant.now()
        val results = mutableListOf<EntryResult>()
        val entries = hurlFile.entries

        logger.logOptions(options = options)

        // Process each entry in the hurlFile, and stop at
        // the first failure.
        for ((index, entry) in entries.withIndex()) {
            val entryIndex = index + 1
            val result = runEntry(entry = entry, index = entryIndex)
            results.add(result)

            // Stop if the current entry has any failure, or if a ending index has
            // been reached.
            if (!result.succeeded ||
                (options.toEntry != null && options.toEntry == entryIndex)) {
                break
            }
        }

        val duration = Duration.between(start, Instant.now())
        logger.logStop(duration = duration)

        // Log last HTTP response headers
        val lastResponse = results.lastOrNull()?.httpResponse
        if (options.outputHeaders && lastResponse != null) {
            logger.logHttpResponseHeaders(lastResponse)
        }

        return RunResult(duration = duration, entryResults = results)
    }


    /**
     * Run the [entry].
     *
     * @param entry Entry to be executed
     * @param index one-based entry index
     * @return an Entry
     */
    private fun runEntry(entry: Entry, index: Int): EntryResult {

        logger.logEntry(index = index)

        runExperimentalCommands(entry)

        // First, we construct the HTTP request.
        var requestSpec = try {
            entry.request.toHttpRequestSpec(
                variables = variableJar,
                fileRoot = options.fileRoot
            )
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

            logger.logHttpRequestSpec(requestSpec)

            httpResult = try {
                httpClient.execute(requestSpec)
            } catch (e: Exception) {
                return EntryResult(errors = listOf(RuntimeErrorResult(position = entry.request.method.begin, message = e.message)))
            }

            logger.logHttpRequest(httpResult.finalizedRequest)
            logger.logHttpResponse(httpResult.response)
            logger.logCookies(httpResult.cookies)

            if (options.followsRedirect && httpResult.response.code >= 300 && httpResult.response.code < 400) {
                val header = httpResult.response.headers
                    .firstOrNull { (k, _) -> k.lowercase() == "location" }
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
        val bodyResult = responseSpec.getCheckBodyResult(variables = variableJar, fileRoot = options.fileRoot, httpResponse = httpResponse)

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
            .forEach {
                logger.logCommand(it)
                it.run(httpClient)
            }
    }
}