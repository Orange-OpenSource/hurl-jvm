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

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import com.orange.ccmd.hurl.fmt.Options as HurlFmtOptions

/**
 * Parse positional and optional arguments of hurlfmt cli.
 */
class ArgsParser {

    private val defaultOptions = HurlFmtOptions()

    private val helpOption: Option = Option.builder("h")
        .longOpt("help")
        .hasArg(false)
        .desc("This help text")
        .build()
    private val formatOption: Option = Option.builder("f")
        .longOpt("format")
        .hasArg()
        .desc("Type of the formatter, values are [${defaultOptions.formatValues.joinToString()}], default is ${defaultOptions.format}")
        .build()
    private val themeOption: Option = Option.builder("t")
        .longOpt("theme")
        .hasArg()
        .desc("Type of the html theme, values are [${defaultOptions.themeValues.joinToString()}], default is ${defaultOptions.theme}")
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


    /**
     * Parse args and return positional and optional arguments.
     *
     * @return a pair of positional arguments (list of string) and options
     */
    fun parse(args: Array<String>): Pair<List<String>, HurlFmtOptions> {
        val parser = DefaultParser()
        val line = try {
            parser.parse(options, args)
        } catch (e: ParseException) {
            throw IllegalArgumentException(e.message)
        } ?: throw IllegalArgumentException("Invalid arguments line")

        val format = line.getOptionValue(formatOption.longOpt, defaultOptions.format)
        val formats = defaultOptions.formatValues
        if (format !in formats) {
            throw IllegalArgumentException("$format is not a valid value, choices are [${formats.joinToString()}]")
        }

        val theme = line.getOptionValue(themeOption.longOpt, defaultOptions.theme)
        val themes = defaultOptions.themeValues
        if (theme !in themes) {
            throw IllegalArgumentException("$theme is not a valid value, choices are [${themes.joinToString()}]")
        }

        val positional = line.args?.toList() ?: emptyList()
        val options = HurlFmtOptions(
            help = if (line.hasOption(helpOption.longOpt)) true else defaultOptions.help,
            version = if (line.hasOption(versionOption.longOpt)) true else defaultOptions.version,
            verbose = if (line.hasOption(verboseOption.longOpt)) true else defaultOptions.verbose,
            format = format,
            theme = theme,
        )
        return positional to options
    }

    fun printHelp() {
        val formatter = HelpFormatter()
        formatter.printHelp("java -jar hurlfmt.jar file", options)
    }

}