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

package com.orange.ccmd.hurl.core.utils

import com.orange.ccmd.hurl.core.ast.Position

fun logError(
    fileName: String,
    line: String,
    message: String,
    position: Position,
    showPosition: Boolean = true
) {
    var text = "$fileName${position.text}: ".ansi.fg.bold
    text += "error: ".ansi.fg.redBold
    text += message.ansi.fg.bold
    println(text)
    println(line)
    if (showPosition) {
        println(" ".repeat(position.column - 1) + "^".ansi.fg.greenBold)
    } else {
        println("")
    }
}