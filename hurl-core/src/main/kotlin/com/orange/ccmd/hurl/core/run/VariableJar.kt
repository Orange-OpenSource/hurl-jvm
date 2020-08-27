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

package com.orange.ccmd.hurl.core.run


class VariableJar {

    private val variables: MutableMap<String, QueryResult> = mutableMapOf()

    operator fun get(key: String): QueryResult? = variables[key]

    operator fun set(key: String, value: QueryResult) {
        variables[key] = value
    }

    operator fun set(key: String, value: String) {
        variables[key] = QueryStringResult(value)
    }

    override fun toString(): String {
        return "VariableJar(variables=$variables)"
    }

    fun addAll(map: Map<String, QueryResult>) {
        map.forEach { (k, v) -> variables[k] = v }
    }

    companion object {
        fun from(variables: Map<String, String>): VariableJar {
            val variableJar = VariableJar()
            variables.forEach { (k, v) -> variableJar[k] = v }
            return variableJar
        }
    }

}