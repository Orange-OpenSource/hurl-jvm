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

package com.orange.ccmd.hurl.core.query.jsonpath

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.PathNotFoundException
import com.orange.ccmd.hurl.core.query.InvalidQueryException


class JsonPath {
    companion object {
        fun evaluate(expr: String, json: String): JsonPathResult {
            val ret: List<Any> = try {
                val conf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST)
                JsonPath.using(conf).parse(json).read(expr)
            } catch (e: PathNotFoundException) {
                return JsonPathNotFound
            } catch (e: Exception) {
                throw InvalidQueryException("$e")
            }

            // If the jsonpath expression return only one element, we try coerce this result
            // into a string, number of boolean if it possible.
            return if (ret.size == 1) {
                JsonPathOk(result = toJson(ret[0]))
            } else {
                JsonPathOk(result = toJson(ret))
            }
        }
    }
}