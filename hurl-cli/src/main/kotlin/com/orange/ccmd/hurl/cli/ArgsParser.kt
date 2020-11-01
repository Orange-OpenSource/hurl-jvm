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

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import com.orange.ccmd.hurl.cli.Options as HurlOptions

/**
 * Parse positional and optional arguments of hurl cli.
 */
class ArgsParser {

    private val helpOption: Option = Option.builder("h")
        .longOpt("help")
        .hasArg(false)
        .desc("This help text")
        .build()
    private val colorOption: Option = Option.builder()
        .longOpt("color")
        .hasArg(false)
        .desc("Colorize output (not yet implemented)")
        .build()
    private val noColorOption: Option = Option.builder()
        .longOpt("no-color")
        .hasArg(false)
        .desc("Do not colorize output (not yet implemented)")
        .build()
    private val variableOption: Option = Option.builder()
        .longOpt("variable")
        .hasArg()
        .argName("name=value")
        .desc("Define variable (example: --variable answer=42)")
        .build()
    private val followRedirectOption: Option = Option.builder("L")
        .longOpt("location")
        .hasArg(false)
        .desc("Follow redirect")
        .build()
    private val fileRootOption: Option = Option.builder()
        .longOpt("file-root")
        .hasArg()
        .argName("file")
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
        .argName("[protocol://]host[:port]")
        .desc("Use proxy on given port, only http proxy is supported")
        .build()
    private val includeOption: Option = Option.builder("i")
        .longOpt("include")
        .hasArg(false)
        .desc("Include HTTP headers in the output")
        .build()
    private val toEntryOption: Option = Option.builder()
        .longOpt("to-entry")
        .hasArg()
        .argName("entry-number")
        .desc("Execute Hurl file to <entry-number> (starting at 1). Ignore the remaining of the file")
        .build()
    private val compressedOption: Option = Option.builder()
        .longOpt("compressed")
        .hasArg(false)
        .desc("Request a compressed response using one of the algorithms br, gzip, deflate and automatically decompress the content")
        .build()
    private val outputFileOption: Option = Option.builder("o")
        .longOpt("output")
        .hasArg()
        .argName("file")
        .desc("Write output to <file> instead of stdout")
        .build()
    private val userOption: Option = Option.builder("u")
        .longOpt("user")
        .hasArg()
        .argName("user:password")
        .desc("Specify the user name and password to use for server authentication (currently only Basic Authentication is supported)")
        .build()
    private val connectTimeoutOption: Option = Option.builder()
        .longOpt("connect-timeout")
        .hasArg()
        .argName("SECONDS")
        .desc("Maximum time in seconds that you allow Hurl’s connection to take.\nSee also -m, --max-time option.")
        .build()
    private val maxTimeOption: Option = Option.builder("m")
        .longOpt("max-time")
        .hasArg()
        .argName("SECONDS")
        .desc("Maximum time in seconds that you allow a request/response to take. This is the standard timeout.\nSee also --connect-timeout option.")
        .build()
    private val jsonReportOption: Option = Option.builder()
        .longOpt("json")
        .hasArg()
        .argName("file")
        .desc("Write full session(s) to a json file. The format is very closed to HAR format.")
        .build()

    private val options: Options = Options()

    init {
        with(options) {
            addOption(helpOption)
            addOption(versionOption)
            addOption(verboseOption)
            addOption(colorOption)
            addOption(noColorOption)
            addOption(followRedirectOption)
            addOption(insecureOption)
            addOption(proxyOption)
            addOption(variableOption)
            addOption(fileRootOption)
            addOption(includeOption)
            addOption(toEntryOption)
            addOption(compressedOption)
            addOption(outputFileOption)
            addOption(userOption)
            addOption(connectTimeoutOption)
            addOption(maxTimeOption)
            addOption(jsonReportOption)
        }
    }

    /**
     * Parse args and return positional and optional arguments.
     *
     * @return a pair of positional arguments (list of string) and options
     */
    fun parse(args: Array<String>): Pair<List<String>, HurlOptions> {
        val parser = DefaultParser()
        val line = try {
            parser.parse(options, args)
        } catch (e: ParseException) {
            throw IllegalArgumentException(e.message)
        } ?: throw IllegalArgumentException("Invalid arguments line")

        val defaultOptions = HurlOptions()

        val variables = line.getOptionValues(variableOption.longOpt)
            ?.map {
                val index = it.indexOf("=")
                if (index == -1) {
                    throw IllegalArgumentException("variable $it not valid")
                }
                it.substring(startIndex = 0, endIndex = index) to it.substring(index + 1)
            }?.toMap()

        val toEntry = try {
            line.getOptionValue(toEntryOption.longOpt)?.toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid to-entry parameter")
        }

        val connectTimeout = try {
            line.getOptionValue(connectTimeoutOption.longOpt)?.toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid connect-timeout parameter")
        }

        val maxTime = try {
            line.getOptionValue(maxTimeOption.longOpt)?.toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid max-time parameter")
        }

        val user = line.getOptionValue(userOption.longOpt)
        if (user != null && ":" !in user) {
            throw IllegalArgumentException("user option should be <user:password>")
        }

        val positional = line.args?.toList() ?: emptyList()
        val options = HurlOptions(
            help = if (line.hasOption(helpOption.longOpt)) true else defaultOptions.help,
            version = if (line.hasOption(versionOption.longOpt)) true else defaultOptions.version,
            verbose = if (line.hasOption(verboseOption.longOpt)) true else defaultOptions.verbose,
            followRedirect = if (line.hasOption(followRedirectOption.longOpt)) true else defaultOptions.followRedirect,
            insecure = if (line.hasOption(insecureOption.longOpt)) true else defaultOptions.insecure,
            proxy = line.getOptionValue(proxyOption.longOpt, defaultOptions.proxy),
            variables = variables ?: defaultOptions.variables,
            fileRoot = line.getOptionValue(fileRootOption.longOpt, defaultOptions.fileRoot),
            include = if (line.hasOption(includeOption.longOpt)) true else defaultOptions.include,
            toEntry = toEntry ?: defaultOptions.toEntry,
            compressed = if (line.hasOption(compressedOption.longOpt)) true else defaultOptions.compressed,
            outputFile = line.getOptionValue(outputFileOption.longOpt, defaultOptions.outputFile),
            user = user ?: defaultOptions.user,
            connectTimeoutInSecond = connectTimeout ?: defaultOptions.connectTimeoutInSecond,
            maxTime = maxTime ?: defaultOptions.maxTime,
            jsonReport = line.getOptionValue(jsonReportOption.longOpt, defaultOptions.outputFile)
        )
        return positional to options
    }


    fun printHelp() {
        val formatter = HelpFormatter()
        formatter.printHelp("java -jar hurl.jar file", options)
    }

}