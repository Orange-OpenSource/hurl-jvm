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

package com.orange.ccmd.hurl.core.run.log

import com.orange.ccmd.hurl.core.http.Cookie
import com.orange.ccmd.hurl.core.http.HttpRequest
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.run.experimental.Command
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RunnerLogger(val outputHeaders: Boolean, val verbose: Boolean) : BaseLogger {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun logStart(index: Int) {
        if (!verbose) {
            return
        }
        logInfo("-".repeat(80))
        logInfo("Entry $index")
    }

    override fun logHttpRequestSpec(request: HttpRequest) {
        if (!verbose) {
            return
        }
        logInfo("Request spec:")
        logInfo("${request.method} ${request.url}")
        request.headers.forEach { (k, v) -> logInfo("$k: $v") }
        if (request.formParams.isNotEmpty()) {
            logInfo("Form params:")
            request.headers.forEach { (k, v) -> logInfo("$k: $v") }
        }
        if (request.queryStringParams.isNotEmpty()) {
            logInfo("Query string params:")
            request.queryStringParams.forEach { (k, v) -> logInfo("$k: $v") }
        }
        logInfo("")
    }

    override fun logHttpRequest(request: HttpRequest) {
        if (!verbose) {
            return
        }
        logInput("${request.method} ${request.url}")
        request.headers.forEach { (k, v) -> logInput("$k: $v") }
        logInput("")
    }

    override fun logHttpResponse(response: HttpResponse) {

        if (verbose) {
            logOutput("${response.version} ${response.code} (${response.body.size} bytes)")
            response.headers.forEach { (k, v) -> logOutput("$k: $v") }
            logOutput("")
        }

        if (!verbose && outputHeaders) {
            log("${response.version} ${response.code} (${response.body.size} bytes)")
            response.headers.forEach { (k, v) -> log("$k: $v") }
            log("")
        }
    }

    override fun logCookies(cookies: List<Cookie>) {
        if (verbose && cookies.isNotEmpty()) {
            logInfo("Cookie store:")
            cookies.forEach { (k, v) -> logInfo("$k: $v") }
        }
    }

    override fun logCommand(command: Command) {
        if (!verbose) {
            return
        }
        logInfo("[Experimental] Run command $command")
    }

    private fun log(text: String) {
        logger.info(text)
    }

    private fun logInfo(text: String) {
        logger.info("* $text")
    }

    private fun logInput(text: String) {
        logger.info("> $text")
    }

    private fun logOutput(text: String) {
        logger.info("< $text")
    }

}
