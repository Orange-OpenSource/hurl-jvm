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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger as JulLogger


class App {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun main(args: Array<String>): Int {
        val parser = ArgsParser()
        try {
            parser.parse(args)
        } catch (e: IllegalArgumentException) {
            println(e.message)
            return OPTIONS_PARSING_ERROR.value
        }

        if (parser.help) {
            parser.printHelp()
            return SUCCESS.value
        }
        if (parser.version) {
            println("hurl (jar) $version")
            return SUCCESS.value
        }

        val verbose = parser.verbose
        val variables = parser.variables
        val allowsInsecure = parser.insecure
        val fileRootName = parser.fileRoot
        val proxy = parser.proxy
        val include = parser.include

        configureLogging(verbose = verbose)

        logger.debug("* version : $version")
        logger.debug("* verbose : $verbose")
        logger.debug("* include : $include")
        logger.debug("* variables :")
        variables.forEach { (k, v) -> logger.info("*   $k -> $v") }
        logger.debug("* fileRootName : $fileRootName")
        logger.debug("* allowsInsecure : $allowsInsecure")
        logger.debug("* proxy : $proxy")

        var returnCode = SUCCESS

        for (fileName in parser.args) {
            val file = File(fileName)
            val absoluteFile = file.absoluteFile
            val fileRoot = if (fileRootName != null) {
                File(fileRootName)
            } else {
                absoluteFile.parentFile
            }

            val ret = CliHelper.run(
                file = absoluteFile,
                variables = variables,
                fileRoot = fileRoot,
                outputHeaders = include,
                verbose = verbose,
                allowsInsecure = allowsInsecure,
                proxy = proxy
            )
            if (ret != SUCCESS) {
                returnCode = ret
            }
        }

        return returnCode.value
    }

    internal val version: String
        get() = Properties("application.properties").get["version"] ?: "undefined"

    internal fun configureLogging(verbose: Boolean) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5\$s%n")
        val julLogger = JulLogger.getLogger("com.orange.ccmd.hurl")
        julLogger.level = Level.FINE
        julLogger.useParentHandlers = false

        val handler = ConsoleHandler()
        // level quiet -> Level.OFF
        handler.level = if (verbose) { Level.FINE } else { Level.INFO }
        julLogger.addHandler(handler)
    }



}