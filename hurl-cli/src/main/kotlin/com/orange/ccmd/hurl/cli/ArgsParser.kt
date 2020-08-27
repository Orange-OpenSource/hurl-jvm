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

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException


class ArgsParser {

    private var line: CommandLine? = null
    private val helpOption: Option = Option.builder("h")
        .longOpt("help")
        .hasArg(false)
        .desc("This help text")
        .build()
    private val variableOption: Option = Option.builder()
        .longOpt("variable")
        .hasArg()
        .desc("Define variable (example: --variable answer=42)")
        .build()
    private val fileRootOption: Option = Option.builder()
        .longOpt("file-root")
        .hasArg()
        .desc("Specify the root directory for file inclusions")
        .build()
    private val versionOption: Option = Option.builder("V")
        .longOpt("version")
        .hasArg(false)
        .desc("Show version number and quit")
        .build()
    private val verboseOption: Option = Option.builder("v")
        .longOpt("verbose")
        .hasArg(false)
        .desc("Make the operation more talkative")
        .build()
    private val insecureOption: Option = Option.builder("k")
        .longOpt("insecure")
        .hasArg(false)
        .desc("Allow connections to SSL sites without certs")
        .build()
    private val proxyOption: Option = Option.builder("x")
        .longOpt("proxy")
        .hasArg()
        .desc("[PROTOCOL://]HOST[:PORT] Use proxy on given port, only http proxy is supported")
        .build()
    private val includeOption: Option = Option.builder("i")
        .longOpt("include")
        .hasArg(false)
        .desc("Include HTTP headers in the output")
        .build()

    private val options: Options = Options()

    init {
        with(options) {
            addOption(helpOption)
            addOption(versionOption)
            addOption(verboseOption)
            addOption(insecureOption)
            addOption(proxyOption)
            addOption(variableOption)
            addOption(fileRootOption)
            addOption(includeOption)
        }
    }

    fun parse(args: Array<String>) {
        val parser = DefaultParser()
        line = try {
            parser.parse(options, args)
        } catch (e: ParseException) {
            throw IllegalArgumentException(e.message)
        }
    }

    val verbose: Boolean
        get() = line?.hasOption(verboseOption.longOpt) ?: false

    val help: Boolean
        get() = line?.hasOption(helpOption.longOpt) ?: false

    val version: Boolean
        get() = line?.hasOption(versionOption.longOpt) ?: false

    val insecure: Boolean
        get() = line?.hasOption(insecureOption.longOpt) ?: false

    val proxy: String?
        get() {
            val curLine = line
            if (curLine == null || !curLine.hasOption(proxyOption.longOpt)) {
                return null
            }
            return curLine.getOptionValue(proxyOption.longOpt)
        }

    val variables: Map<String, String>
        get() {
            val curLine = line
            if (curLine == null || !curLine.hasOption(variableOption.longOpt)) {
                return emptyMap()
            }
            return curLine.getOptionValues(variableOption.longOpt).map {
                val index = it.indexOf("=")
                if (index == -1) {
                    throw ParseException("variable $it not valid")
                }
                it.substring(startIndex = 0, endIndex = index) to it.substring(index + 1)
            }.toMap()
        }

    val fileRoot: String?
        get() {
            val curLine = line
            if (curLine == null || !curLine.hasOption(fileRootOption.longOpt)) {
                return null
            }
            return curLine.getOptionValue(fileRootOption.longOpt)
        }

    val include: Boolean
        get() = line?.hasOption(includeOption.longOpt) ?: false

    val args: List<String>
        get() = line?.args?.toList() ?: emptyList()

    fun printHelp() {
        val formatter = HelpFormatter()
        formatter.printHelp("java -jar hurl.jar file", options)
    }


}