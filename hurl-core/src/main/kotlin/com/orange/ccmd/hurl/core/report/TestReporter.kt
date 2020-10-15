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

package com.orange.ccmd.hurl.core.report

import com.orange.ccmd.hurl.core.parser.Error
import com.orange.ccmd.hurl.core.run.RunResult
import com.orange.ccmd.hurl.core.run.InvalidVariableResult
import com.orange.ccmd.hurl.core.utils.ansi
import com.orange.ccmd.hurl.core.utils.lineAt
import com.orange.ccmd.hurl.core.utils.logError
import java.io.File

class TestReporter(val text: String, val fileName: String): Reporter {

    override fun reportStart() {
        val file = "$fileName:".ansi.fg.bold
        val state = "RUNNING".ansi.fg.blueBold
        println("$file $state")
    }

    override fun reportSyntaxError(error: Error) {
        logError(
            fileName = fileName,
            line =  text.lineAt(error.position.line),
            message = error.message,
            position = error.position,
            showPosition = true
        )
     }

    override fun reportResult(result: RunResult) {
        result.entryResults
            .flatMap { it.results }
            .filterNot { it.succeeded }
            .forEach {
                val line = text.lineAt(it.position.line)
                logError(
                    fileName = fileName,
                    line = line,
                    message = it.message,
                    position = it.position,
                    showPosition = it is InvalidVariableResult
                )
        }

        val file = "$fileName:".ansi.fg.bold
        val state = if (result.succeeded) {
            "SUCCESS".ansi.fg.greenBold
        } else {
            "FAILED".ansi.fg.redBold
        }
        println("$file $state in ${result.duration.toMillis()} ms")
    }
}