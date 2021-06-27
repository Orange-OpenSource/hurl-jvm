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

import com.orange.ccmd.hurl.core.ast.Capture
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.query.InvalidQueryException
import com.orange.ccmd.hurl.core.variable.VariableJar
import com.orange.ccmd.hurl.core.template.InvalidVariableException


internal fun Capture.eval(response: HttpResponse, variables: VariableJar): EntryStepResult {
    val name = name.value

    val value = try {
        query.eval(response = response, variables = variables)
    } catch (e: InvalidQueryException) {
        return CaptureResult(succeeded = false, position = begin, variable = name)
    } catch (e: InvalidSubqueryException) {
        return CaptureResult(succeeded = false, position = begin, variable = name)
    }
    catch (e: InvalidVariableException) {
        return InvalidVariableResult(position = e.position, reason = e.reason)
    }

    // If the capture has a subquery, we evaluate it to get the final value
    // Only captures that are QueryStingResult are supported.
    /*val value = if (subquery != null) {
        if (result !is QueryStringResult) {
            return InvalidVariableResult(position = subquery.begin, reason = "invalid type for subquery ${result.text()}")
        }

        try {
            subquery.eval(text = result.value, variables = variables)
        } catch (e: InvalidSubqueryException) {
            return CaptureResult(succeeded = false, position = subquery.begin, variable = name)
        } catch (e: InvalidVariableException) {
            return InvalidVariableResult(position = e.position, reason = e.reason)
        }
    } else {
        result
    }*/

    return CaptureResult(
        succeeded = true,
        position = begin,
        variable = name,
        value = value
    )
}