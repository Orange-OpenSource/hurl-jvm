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

package com.orange.ccmd.hurl.core.parser

sealed class Error(val message: String, val position: Position) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Error) return false

        if (message != other.message) return false
        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + position.hashCode()
        return result
    }
}

class SyntaxError(message: String, position: Position) : Error(message, position) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SyntaxError) return false
        if (!super.equals(other)) return false
        return true
    }
}

class EofError(position: Position) : Error("end of file", position) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EofError) return false
        if (!super.equals(other)) return false
        return true
    }
}

/**
 * Returns the single deepest error, or null if there are multiple deepest error.
 */
internal val List<Error>.singleDeepest : Error?
get() {
    val deepest = maxByOrNull { it.position.offset } ?: return null
    val allDeepest = filter { it.position.offset == deepest.position.offset }
    return if (allDeepest.size == 1) {
        allDeepest[0]
    } else {
        null
    }
}