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

// ANSI escape code https://en.wikipedia.org/wiki/ANSI_escape_code

val String.ansi: Ansi
    get() = Ansi(this)

data class Ansi(val text: String) {
    val fg: Foreground = Foreground(text)
}

data class Foreground(val text: String) {

    fun color(index: Int, bold: Boolean = false): String  {
        var ret = "\u001B[${index}"
        if (bold) {
            ret += ";1"
        }
        ret += "m$text\u001B[0m"
        return ret
    }

    val bold: String = "\u001B[1m$text\u001B[0m"

    val black: String = color(30)
    val blackBold: String = color(30, bold = true)
    val red: String = color(31)
    val redBold: String = color(31, bold = true)
    val green: String = color(32)
    val greenBold: String = color(32, bold = true)
    val yellow: String = color(33)
    val yellowBold: String = color(33, bold = true)
    val blue: String = color(34)
    val blueBold: String = color(34, bold = true)
    val magenta: String = color(35)
    val magentaBold: String = color(35, bold = true)
    val cyan: String = color(36)
    val cyanBold: String = color(36, bold = true)
    val white: String = color(37)
    val whiteBold: String = color(37, bold = true)
    val brightBlack: String = color(90)
    val brightBlackBold: String = color(90, bold = true)
    val brightRed: String = color(91)
    val brightRedBold: String = color(91, bold = true)
    val brightGreen: String = color(92)
    val brightGreenBold: String = color(92, bold = true)
    val brightYellow: String = color(93)
    val brightYellowBold: String = color(93, bold = true)
    val brightBlue: String = color(94)
    val brightBlueBold: String = color(94, bold = true)
    val brightMagenta: String = color(95)
    val brightMagentaBold: String = color(95, bold = true)
    val brightCyan: String = color(96)
    val brightCyanBold: String = color(96, bold = true)
    val brightWhite: String = color(97)
    val brightWhiteBold: String = color(97, bold = true)

}
