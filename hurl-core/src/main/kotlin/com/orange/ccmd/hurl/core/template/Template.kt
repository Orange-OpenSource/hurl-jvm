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

package com.orange.ccmd.hurl.core.template

import com.orange.ccmd.hurl.core.parser.Position
import com.orange.ccmd.hurl.core.run.QueryBooleanResult
import com.orange.ccmd.hurl.core.run.QueryNumberResult
import com.orange.ccmd.hurl.core.run.QueryStringResult
import com.orange.ccmd.hurl.core.run.VariableJar

class Template {

    /**
     * Render the variable inside the template string.
     * If the template string doesn't contains any templating, [text] is returned.
     * If there is any undefined variable ({{x}} where x is not a key in variables), [InvalidVariableException] is thrown.
     * @param text template string
     * @param variables variables to use in template
     * @position position of the node to render
     */
    companion object {
        fun render(text: String, variables: VariableJar, position: Position): String {
            val regex = Regex("""\{{2,3}([\sa-zA-Z0-9_\-]+)\}{2,3}""")
            var output = text
            while (true) {
                val match: MatchResult = regex.find(output) ?: return output
                val range = match.range
                val name = match.groupValues[1]
                val variable = variables[name] ?: throw InvalidVariableException(
                    name = name,
                    position = position,
                    reason = "undefined variable"
                )
                val value = when (variable) {
                    is QueryBooleanResult -> variable.value.toString()
                    is QueryNumberResult -> variable.value.toString()
                    is QueryStringResult -> variable.value
                    else -> throw throw InvalidVariableException(
                        name = name,
                        position = position,
                        reason = "invalid variable ${variable.text()}"
                    )
                }
                output = output.replaceRange(
                    range = range,
                    replacement = value
                )
            }
        }

    }
}
