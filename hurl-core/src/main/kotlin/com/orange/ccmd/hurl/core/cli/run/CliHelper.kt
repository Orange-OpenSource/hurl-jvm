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

package com.orange.ccmd.hurl.core.cli.run

import com.orange.ccmd.hurl.core.ast.HurlParser
import com.orange.ccmd.hurl.core.run.Options
import com.orange.ccmd.hurl.core.run.Runner
import com.orange.ccmd.hurl.core.utils.lineAt
import com.orange.ccmd.hurl.core.utils.logError
import java.io.File

/**
 * Helper for running an Hurl file
 */
class CliHelper {

    companion object {

        /**
         * Runs an Hurl file.
         *
         */
        fun run(
            file: File,
            variables: Map<String, String>,
            fileRoot: File,
            outputHeaders: Boolean,
            verbose: Boolean,
            allowsInsecure: Boolean,
            proxy: String?,
            followsRedirect: Boolean,
            toEntry: Int?,
            compressed: Boolean,
            outputFile: File?,
            user: String?,
            connectTimeoutInSecond: Int,
            maxTime: Int?
        ): CliReturnCode {

            val fileName = file.absoluteFile.name
            val text = file.readText()

            // Parse the input files and report any error.
            val parser = HurlParser(text = text)
            val hurl = parser.parse()
            if (hurl == null) {
                val error = parser.rootError
                logError(
                    fileName = fileName,
                    line = text.lineAt(error.position.line),
                    message = error.message,
                    position = error.position,
                    showPosition = true
                )
                return CliReturnCode.INPUT_FILE_PARSING_ERROR
            }

            /*
            val file = "$fileName:".ansi.fg.bold
            val state = "RUNNING".ansi.fg.blueBold
            println("$file $state")
             */

            // Run the file and report run results.
            val runner = Runner(
                hurlFile = hurl,
                options = Options(
                    variables = variables,
                    fileRoot = fileRoot,
                    outputHeaders = outputHeaders,
                    verbose = verbose,
                    allowsInsecure = allowsInsecure,
                    proxy = proxy,
                    followsRedirect = followsRedirect,
                    toEntry = toEntry,
                    compressed = compressed,
                    user = user,
                    connectTimeoutInSecond = connectTimeoutInSecond,
                    maxTime = maxTime
                )
            )


            val result = runner.run()

            /*
            val file = "$fileName:".ansi.fg.bold
            val state = if (result.succeeded) {
                "SUCCESS".ansi.fg.greenBold
            } else {
                "FAILED".ansi.fg.redBold
            }
            */
            return when {
                result.succeeded -> {
                    // If the run is successful, we dump the bytes body response
                    val lastHttpResponse = result.entryResults.lastOrNull()?.httpResponse ?: return CliReturnCode.SUCCESS
                    val body = if (compressed) {
                        try {
                            lastHttpResponse.getDecompressedBody()
                        } catch (e: IllegalArgumentException) {
                            return CliReturnCode.RUNTIME_ERROR
                        }
                    } else {
                        lastHttpResponse.body
                    }

                        if (outputFile != null) {
                        // To support /dev/null on all platform, including Windows one,
                        // we explicitly disable writing on /dev/null (and nul https://gcc.gnu.org/legacy-ml/gcc-patches/2005-05/msg01793.html)
                        if (outputFile.path != "/dev/null" && outputFile.path != "nul") {
                            outputFile.writeBytes(body)
                        }
                    } else {
                        System.out.write(body)
                    }
                    CliReturnCode.SUCCESS
                }
                !result.succeeded && result.entryResults.flatMap { it.errors }
                    .isNotEmpty() -> CliReturnCode.RUNTIME_ERROR
                !result.succeeded && result.entryResults.flatMap { it.asserts }
                    .any { !it.succeeded } -> CliReturnCode.ASSERT_ERROR
                else -> CliReturnCode.UNKNOWN_ERROR
            }
        }
    }
}