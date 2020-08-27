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
import com.orange.ccmd.hurl.core.report.StdOutReporter
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
            proxy: String?
        ): Boolean {

            val fileName = file.absoluteFile.name
            val text = file.readText()
            val reporter = StdOutReporter(text = text, fileName = fileName)

            reporter.reportStart()

            // Parse the input files and report any error.
            val parser = HurlParser(text = text)
            val hurl = parser.parse()
            if (hurl == null) {
                reporter.reportSyntaxError(error = parser.rootError)
                return false
            }

            // Run the file and report run results.
            val runner = Runner(
                hurlFile = hurl,
                variables = variables,
                fileRoot = fileRoot,
                outputHeaders = outputHeaders,
                verbose = verbose,
                allowsInsecure = allowsInsecure,
                proxy = proxy
            )
            val result = runner.run()

            reporter.reportResult(result = result)
            return result.succeeded
        }
    }
}