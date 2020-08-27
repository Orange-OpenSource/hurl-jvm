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

import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Return the byte count of a valid xml text in a text stream.
 * The implementation is dummy for the moment because I was unable to get valid byte count result
 * with unicode that are encoded with 2+ bytes in UTF-8...
 * First version was:
 *
 * internal fun getXmlByteCount(text: String): Int? {
 *     val xmlif = XMLInputFactory.newInstance()
 *     xmlif.setProperty(IS_NAMESPACE_AWARE, true)
 *     val bytes = text.toByteArray(charset = Charsets.UTF_8)
 *     val inputStream = ByteArrayInputStream(bytes)
 *    val xmlr = xmlif.createXMLEventReader(inputStream, "UTF-8")
 *
 *     var firstElement: StartElement? = null
 *     var bytesRead = 0
 *     var level = 0
 *
 *     try {
 *         loop@ while (xmlr.hasNext()) {
 *             val event = xmlr.nextEvent()
 *             when (event.eventType) {
 *                 XMLEvent.START_ELEMENT -> {
 *                     val startElement = event.asStartElement()
 *                    if (firstElement == null) {
 *                        firstElement = startElement
 *                    }
 *                    if (firstElement != null && firstElement.name == startElement.name) {
 *                        level += 1
 *                    }
 *                }
 *                XMLEvent.END_ELEMENT -> {
 *                    val endElement = event.asEndElement()
 *                    if (firstElement != null && firstElement.name == endElement.name) {
 *                        level -= 1
 *                    }
 *                    if (level == 0) {
 *                        bytesRead = endElement.location.characterOffset
 *                        break@loop
 *                    }
 *                }
 *            }
 *        }
 *    } catch (e: Exception) {
 *        return null
 *    }
 *
 *    return bytesRead
 *}
 */

internal fun getXmlByteCount(text: String): Int? {

    var terminatorIndex = text.indexOf(char = '>')
    if (terminatorIndex == -1) {
        return null
    }

    terminatorIndex += 1

    while (terminatorIndex != -1) {
        terminatorIndex = text.indexOf(char = '>', startIndex = terminatorIndex)
        if (terminatorIndex != -1) {
            val slice = text.take(terminatorIndex + 1)
            if (isValidXml(slice)) {
                return slice.toByteArray().size
            }
        }
        terminatorIndex += 1
    }
    return null
}

internal fun isValidXml(text: String): Boolean {
    val dbFactory = DocumentBuilderFactory.newInstance()
    dbFactory.isNamespaceAware = true
    val dBuilder = dbFactory.newDocumentBuilder()
    dBuilder.setErrorHandler(DefaultHandler())
    val xmlInput = InputSource(StringReader(text))
    try {
        dBuilder.parse(xmlInput)
    } catch (e: Exception) {
        return false
    }
    return true
}