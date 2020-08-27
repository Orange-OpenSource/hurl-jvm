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

sealed class QueryResult {
    abstract fun text(): String;
}

data class QueryBooleanResult(val value: Boolean) : QueryResult() {
    override fun text(): String = "boolean <$value>"
}

data class QueryNumberResult(val value: Number): QueryResult() {
    override fun text(): String = "number <$value>"
}

data class QueryStringResult(val value: String): QueryResult() {
    override fun text(): String = "string <$value>"
}

data class QueryListResult(val size: Int): QueryResult() {
    override fun text(): String = "list(size=$size)"
}

data class QueryNodeSetResult(val size: Int): QueryResult() {
    override fun text(): String = "nodeset(size=$size)"
}

/**
 * value can be null: for instance a jsonpath whose result is null
 * {
 *  "a_null": null
 * }
 *  jsonpath $['a_null'] return a QueryObjectResult whose value is null
 */
data class QueryObjectResult(val value: Any?): QueryResult() {
    override fun text(): String = "object <$value>"
}

class QueryNoneResult : QueryResult() {

    override fun text(): String {
        return ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryNoneResult) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}