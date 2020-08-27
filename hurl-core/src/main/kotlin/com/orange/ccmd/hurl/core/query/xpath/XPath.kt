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
import org.jsoup.Jsoup
import javax.xml.xpath.XPathEvaluationResult.XPathResultType.BOOLEAN
import javax.xml.xpath.XPathEvaluationResult.XPathResultType.NODESET
import javax.xml.xpath.XPathEvaluationResult.XPathResultType.NUMBER
import javax.xml.xpath.XPathEvaluationResult.XPathResultType.STRING
import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathNodes


class XPath {
    companion object {
        fun evaluateHtml(expr: String, body: String): XPathResult {

            if (body.isEmpty()) {
                throw InvalidQueryException("invalid query, empty body")
            }

            try {
                val jsoup = Jsoup.parse(body)
                val doc = W3CDom.fromJsoup(jsoup)
                val xpath = XPathFactory.newInstance().newXPath()
                val ret = xpath.evaluateExpression(expr, doc)
                return when (ret.type()) {
                    STRING -> XPathStringResult(value = ret.value() as String)
                    BOOLEAN -> XPathBooleanResult(value = ret.value() as Boolean)
                    NUMBER -> XPathNumberResult(value = ret.value() as Number)
                    NODESET -> {
                        val nodes = ret.value() as XPathNodes
                        XPathNodeSetResult(size = nodes.size())
                    }
                    else -> { throw InvalidQueryException("invalid XPath return type") }
                }
            } catch (e: Exception) {
                throw InvalidQueryException("$e")
            }
        }
    }
}