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
import com.orange.ccmd.hurl.core.run.Options
import com.orange.ccmd.hurl.core.run.experimental.Command
import java.time.Duration

class Logger(val outputHeaders: Boolean, val verbose: Boolean) {

    fun logEntry(index: Int) {
        logInfo("-".repeat(80))
        logInfo("Executing entry $index")
        logInfo("")
    }

    fun logStop(duration: Duration) {
        logInfo("Duration: ${duration.toMillis()}ms")
        logInfo("")
    }

    fun logHttpRequestSpec(request: HttpRequest) {
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

    fun logHttpRequest(request: HttpRequest) {
        logInput("${request.method} ${request.url}")
        request.headers.forEach { (k, v) -> logInput("$k: $v") }
        logInput("")
    }

    fun logHttpResponse(response: HttpResponse) {
        logOutput("${response.version} ${response.code} (${response.body.size} bytes)")
        response.headers.forEach { (k, v) -> logOutput("$k: $v") }
        logOutput("")

        if (!verbose && outputHeaders) {
            log("${response.version} ${response.code} (${response.body.size} bytes)")
            response.headers.forEach { (k, v) -> log("$k: $v") }
            log("")
        }
    }

    fun logCookies(cookies: List<Cookie>) {
        logInfo("Cookie store:")
        cookies.forEach { (k, v) -> logInfo("$k: $v") }
    }

    fun logCommand(command: Command) {
        logInfo("[Experimental] Run command $command")
    }

    fun logOptions(options: Options) {
        logInfo("Options:")
        logInfo(" include: ${options.outputHeaders}")
        logInfo(" variables:")
        options.variables.forEach { (k, v) -> logInfo("   $k -> $v") }
        logInfo(" fileRoot: ${options.fileRoot}")
        logInfo(" insecure: ${options.allowsInsecure}")
        logInfo(" proxy: ${options.proxy ?: ""}")
        logInfo(" toEntry: ${options.toEntry ?: ""}")
        logInfo(" compressed: ${options.compressed}")
        logInfo(" user: ${options.user ?: ""}")
        logInfo(" connectTimeout: ${options.connectTimeoutInSecond}")
    }

    private fun log(text: String) {
        println(text)
    }

    private fun logInfo(text: String) {
        if (!verbose) {
            return
        }
        println("* $text")
    }

    private fun logInput(text: String) {
        if (!verbose) {
            return
        }
        println("> $text")
    }

    private fun logOutput(text: String) {
        if (!verbose) {
            return
        }
        println("< $text")
    }

}
