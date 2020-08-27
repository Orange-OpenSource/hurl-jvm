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

import org.apache.commons.cli.*
import java.lang.IllegalArgumentException

class ArgsParser {
    private var line: CommandLine? = null
    private val helpOption: Option = Option.builder("h")
        .longOpt("help")
        .hasArg(false)
        .desc("This help text")
        .build()
    private val helpDefault = false
    private val formatValues: List<String> = listOf("termws", "term", "lint", "html")
    private val formatDefault = "termws"
    private val formatOption: Option = Option.builder("f")
        .longOpt("format")
        .hasArg()
        .desc("Type of the formatter, values are [${formatValues.joinToString(", ")}], default is $formatDefault")
        .build()
    private val themeValues: List<String> = listOf("dark16", "dark256", "light256")
    private val themeDefault = "dark256"
    private val themeOption: Option = Option.builder("t")
        .longOpt("theme")
        .hasArg()
        .desc("Type of the html theme, values are [${themeValues.joinToString(", ")}], default is $themeDefault")
        .build()

    private val versionOption: Option = Option.builder("V")
        .longOpt("version")
        .hasArg(false)
        .desc("Show version number and quit")
        .build()
    private val versionDefault = false
    private val verboseOption: Option = Option.builder("v")
        .longOpt("verbose")
        .hasArg(false)
        .desc("Make the operation more talkative")
        .build()
    private val verboseDefault = false
    private val inplaceOption: Option = Option.builder("i")
        .longOpt("inplace")
        .hasArg(false)
        .desc("Write result to source file instead of stdout")
        .build()
    private val inplaceDefault = false

    private val options: Options = Options()

    init {
        with(options) {
            addOption(helpOption)
            addOption(formatOption)
            addOption(themeOption)
            addOption(versionOption)
            addOption(verboseOption)
            addOption(inplaceOption)
        }
    }

    fun parse(args: Array<String>) {
        val parser = DefaultParser()
        line = try {
            parser.parse(options, args)
        } catch (e: ParseException) {
            throw IllegalArgumentException(e.message)
        }

        // Test enum format
        if (format !in formatValues) {
            throw IllegalArgumentException("$format is not a valid value, choices are [${formatValues.joinToString(", ")}]")
        }

        // Test enum theme
        if (theme !in themeValues) {
            throw IllegalArgumentException("$theme is not a valid value, choices are [${themeValues.joinToString(", ")}]")
        }

    }

    val verbose: Boolean
        get() = line?.hasOption("verbose") ?: verboseDefault

    val help: Boolean
        get() = line?.hasOption("help") ?: helpDefault

    val format: String
        get() {
            val l = line
            return if (l != null && l.hasOption("format")) {
                l.getOptionValue("format")
            } else {
                formatDefault
            }
        }

    val theme: String
        get() {
            val l = line
            return if (l != null && l.hasOption("theme")) {
                l.getOptionValue("theme")
            } else {
                themeDefault
            }
        }

    val version: Boolean
        get() = line?.hasOption("version") ?: versionDefault

    val args: List<String>
        get() = line?.args?.toList() ?: emptyList()

    val inplace: Boolean
        get() = line?.hasOption("inplace") ?: inplaceDefault

    fun printHelp() {
        val formatter = HelpFormatter()
        formatter.printHelp("java -jar hurlfmt.jar file", options)
    }

}