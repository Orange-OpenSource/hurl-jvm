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

package com.orange.ccmd.hurl.core.query.xpath

import com.orange.ccmd.hurl.core.query.InvalidQueryException
import com.sun.org.apache.xpath.internal.XPathAPI
import com.sun.org.apache.xpath.internal.objects.XBoolean
import com.sun.org.apache.xpath.internal.objects.XNodeSet
import com.sun.org.apache.xpath.internal.objects.XNumber
import com.sun.org.apache.xpath.internal.objects.XString
import org.jsoup.Jsoup


sealed class XPathResult

data class XPathBooleanResult(val value: Boolean) : XPathResult()

data class XPathNumberResult(val value: Number) : XPathResult()

data class XPathStringResult(val value: String) : XPathResult()

data class XPathNodeSetResult(val size: Int): XPathResult()

class XPath {
    companion object {
        fun evaluateHtml(expr: String, body: String): XPathResult {

            if (body.isEmpty()) {
                throw InvalidQueryException("invalid query, empty body")
            }

            try {
                val jsoup = Jsoup.parse(body)
                val doc = W3CDom.fromJsoup(jsoup)
                val ret = XPathAPI.eval(doc, expr)
                return when (ret) {
                    is XString -> XPathStringResult(value = ret.str())
                    is XBoolean -> XPathBooleanResult(value = ret.bool())
                    is XNumber -> XPathNumberResult(value = ret.num())
                    is XNodeSet -> XPathNodeSetResult(size = ret.nodelist().length)
                    else -> { throw InvalidQueryException("invalid XPath return type")
                    }
                }
            } catch (e: Exception) {
                throw InvalidQueryException("$e")
            }
        }
    }
}