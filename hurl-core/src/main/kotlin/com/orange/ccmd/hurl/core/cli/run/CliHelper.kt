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
import com.orange.ccmd.hurl.core.report.ReporterType
import com.orange.ccmd.hurl.core.report.ReporterType.SIMPLE
import com.orange.ccmd.hurl.core.report.ReporterType.TEST
import com.orange.ccmd.hurl.core.report.SimpleReporter
import com.orange.ccmd.hurl.core.report.TestReporter
import com.orange.ccmd.hurl.core.run.Options
import com.orange.ccmd.hurl.core.run.Runner
import java.io.File


class CliHelper {

    companion object {

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
            reporterType: ReporterType = SIMPLE,
        ): CliReturnCode {

            val fileName = file.absoluteFile.name
            val text = file.readText()

            val reporter = when (reporterType) {
                SIMPLE -> SimpleReporter(text = text, fileName = fileName)
                TEST -> TestReporter(text = text, fileName = fileName)
            }

            reporter.reportStart()

            // Parse the input files and report any error.
            val parser = HurlParser(text = text)
            val hurl = parser.parse()
            if (hurl == null) {
                reporter.reportSyntaxError(error = parser.rootError)
                return CliReturnCode.INPUT_FILE_PARSING_ERROR
            }

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
                    compressed = compressed
                )
            )
            val result = runner.run()

            reporter.reportResult(result = result)
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