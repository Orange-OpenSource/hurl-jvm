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

package com.orange.ccmd.hurl.cli

import com.orange.ccmd.hurl.core.cli.run.CliHelper
import com.orange.ccmd.hurl.core.cli.run.CliReturnCode.OPTIONS_PARSING_ERROR
import com.orange.ccmd.hurl.core.cli.run.CliReturnCode.SUCCESS
import com.orange.ccmd.hurl.core.utils.Properties
import java.io.File


class App {

    /**
     * Entry point for the cli.
     *
     * The return code can be:
     * - 0: SUCCESS,
     * - 1: OPTIONS_PARSING_ERROR,
     * - 2: INPUT_FILE_PARSING_ERROR,
     * - 3: RUNTIME_ERROR,
     * - 4: ASSERT_ERROR,
     * - 5: UNKNOWN_ERROR
     *
     * @return an Int representing an error code
     */
    fun main(args: Array<String>): Int {
        val parser = ArgsParser()
        val (positional, options) = try {
            parser.parse(args)
        } catch (e: IllegalArgumentException) {
            println(e.message)
            return OPTIONS_PARSING_ERROR.value
        }

        if (options.help) {
            parser.printHelp()
            return SUCCESS.value
        }
        if (options.version) {
            println("hurl (jvm) $version")
            return SUCCESS.value
        }

        var returnCode = SUCCESS

        for (fileName in positional) {
            val file = File(fileName)
            val absoluteFile = file.absoluteFile
            val fileRootName = options.fileRoot
            val fileRoot = if (fileRootName != null) {
                File(fileRootName)
            } else {
                absoluteFile.parentFile
            }

            val ret = CliHelper.run(
                file = absoluteFile,
                variables = options.variables,
                fileRoot = fileRoot,
                outputHeaders = options.include,
                verbose = options.verbose,
                allowsInsecure = options.include,
                proxy = options.proxy,
                followsRedirect = options.followRedirect,
                toEntry = options.toEntry,
                compressed = options.compressed,
                outputFile = options.outputFile?.let { File(options.outputFile) },
                user = options.user,
                connectTimeoutInSecond = options.connectTimeoutInSecond,
                maxTime = options.maxTime
            )
            if (ret != SUCCESS) {
                returnCode = ret
            }
        }

        return returnCode.value
    }

    private val version: String
        get() = Properties("application.properties").get["version"] ?: "undefined"

}