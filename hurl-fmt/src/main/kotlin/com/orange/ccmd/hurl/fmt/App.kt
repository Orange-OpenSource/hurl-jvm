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

package com.orange.ccmd.hurl.fmt

import com.orange.ccmd.hurl.core.ast.HurlParser
import com.orange.ccmd.hurl.core.utils.FAILED
import com.orange.ccmd.hurl.core.utils.OK
import com.orange.ccmd.hurl.core.utils.Properties
import com.orange.ccmd.hurl.core.utils.lineAt
import com.orange.ccmd.hurl.core.utils.logError
import com.orange.ccmd.hurl.fmt.highlight.HtmlFormatter
import com.orange.ccmd.hurl.fmt.highlight.TermFormatter
import com.orange.ccmd.hurl.fmt.lint.LintFormatter
import java.io.File
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger as JulLogger


class App {

    /**
     * Entry point for the cli.
     *
     * The return code can be:
     * - 0: OK,
     * - 1: FAILED,
     *
     * @return an Int representing an error code
     */
    fun main(args: Array<String>): Int {

        val parser = ArgsParser()
        val (positional, options) = try {
            parser.parse(args)
        } catch (e: IllegalArgumentException) {
            println(e.message)
            return FAILED
        }
        if (options.help) {
            parser.printHelp()
            return OK
        }
        if (options.version) {
            println("hurlfmt (jar) $version")
            return OK
        }


        val stdin = "-"
        configureLogging(verbose = options.verbose)

        if (options.inplace && stdin in positional) {
            println("inplace option not compatible with stdin (-).")
            return FAILED
        }

        for (f in positional) {
            val input = if (f == stdin) {
                stdIn()
            } else {
                // TODO: catch exception if file doesnt exist
                File(f).readText()
            }
            val output = formatFile(text = input, fileName = f, format = options.format, theme = options.theme) ?: return FAILED
            if (!options.inplace) {
                println(output)
            } else {
                File(f).writeText(output)
            }
        }

        return OK
    }

    internal val version: String
        get() = Properties("application.properties").get["version"] ?: "undefined"

    internal fun configureLogging(verbose: Boolean) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5\$s%n")
        val julLogger = JulLogger.getLogger("com.orange.ccmd.hurl")
        julLogger.level = Level.FINE
        julLogger.useParentHandlers = false

        val handler = ConsoleHandler()
        handler.level = if (verbose) {
            Level.FINE
        } else {
            Level.INFO
        }
        julLogger.addHandler(handler)
    }

    internal fun formatFile(text: String, fileName: String, format: String, theme: String): String? {

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
            return null
        }

        val formatter: Formatter = when (format) {
            "term" -> TermFormatter(showWhitespaces = false)
            "termws" -> TermFormatter(showWhitespaces = true)
            "html" -> HtmlFormatter(theme = theme)
            "lint" -> LintFormatter()
            else -> return null
        }

        return formatter.format(hurlFile = hurl)
    }

    private fun stdIn(): String {
        val scanner = Scanner(System.`in`)
        val lines = mutableListOf<String>()
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine())
        }
        return lines.joinToString(separator = "\n")
    }
}