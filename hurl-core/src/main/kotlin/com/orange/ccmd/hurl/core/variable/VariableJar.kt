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

package com.orange.ccmd.hurl.core.variable

/**
 * Container of variables.
 * Variables can be injected view command line options or
 * by using captures in the Hurl file.
 */
class VariableJar {

    private val variables: MutableMap<String, Var> = mutableMapOf()

    /**
     * Returns a variable for a given name.
     */
    operator fun get(key: String): Var? = variables[key]

    /**
     * Add a variable to the jay
     * @param name name of the variable
     * @param value value of the variable
     */
    fun add(name: String, value: Any?) {
        val variable = when (value) {
            is Boolean -> BoolVar(name = name, value = value)
            is Number -> NumberVar(name = name, value = value)
            is String -> StringVar(name = name, value = value)
            else -> ObjectVar(name = name, value = value)
        }
        variables[variable.name] = variable
    }

    companion object {
        /**
         * Create a VariableJar from a map of name value
         * @param variables map of key value to populate the jar
         * @return a VariableJar populated with [variables]
         */
        fun from(variables: Map<String, String>): VariableJar {

            // TODO: we support for the moment only string variable creation.
            //  We should analyse values to create bool, int, null when possible (à la YAML).
            val variableJar = VariableJar()
            variables.forEach { (k, v) -> variableJar.add(name = k, value = v) }
            return variableJar
        }
    }

}