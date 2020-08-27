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

import com.orange.ccmd.hurl.core.ast.Position
import java.lang.Exception


sealed class EntryStepResult(val succeeded: Boolean, val position: Position, val message: String = "")

class AssertResult(succeeded: Boolean, position: Position, message: String) :
    EntryStepResult(
        succeeded = succeeded,
        position = position,
        message = message
    )

class CaptureResult(succeeded: Boolean, position: Position, val variable: String, val value: QueryResult? = null) :
    EntryStepResult(
        succeeded = succeeded,
        position = position,
        message = if (succeeded) {
            "capture variable '$variable' succeeded"
        } else {
            "capture variable '$variable' failed"
        }
    )

class InvalidVariableResult(position: Position, val reason: String) :
    EntryStepResult(
        succeeded = false,
        position = position,
        message = reason
    )

class RuntimeErrorResult(position: Position, error: Exception) :
    EntryStepResult(
        succeeded = false,
        position = position,
        message = "runtime error ${error.message}"
    )