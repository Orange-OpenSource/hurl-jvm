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

class Properties(val resourcePath: String) {

    val get: Map<String, String>
    get() {
        val loader = ClassLoader.getSystemClassLoader()
        val resource = loader.getResource(resourcePath) ?: return emptyMap()
        val file = resource.openStream()
        val properties = java.util.Properties()
        properties.load(file)
        val props =  properties.mapNotNull {
            val key = it.key as? String?
            val value = it.value as? String
            if (key != null && value != null) {
                key to value
            } else {
                null
            }
        }.toMap()
        file.close()
        return props
    }
}