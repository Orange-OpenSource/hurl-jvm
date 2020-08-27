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

import java.util.HashMap


import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

import org.w3c.dom.Document
import org.w3c.dom.Node

class W3CDom {

    companion object {
        /**
         * Returns a W3C DOM that exposes the same content as the supplied Jsoup document into a W3C DOM.
         * @param jsoupDocument The Jsoup document to convert.
         * @return A W3C Document.
         */
        fun fromJsoup(jsoupDocument: org.jsoup.nodes.Document): Document? {
            val document: Document?
            try {
                /* Obtain the document builder for the configured XML parser. */
                val docBuilderFactory = DocumentBuilderFactory.newInstance()
                val docBuilder = docBuilderFactory.newDocumentBuilder()

                /* Create a document to contain the content. */
                document = docBuilder.newDocument()
                createDOM(
                    jsoupDocument,
                    document,
                    document,
                    HashMap()
                )

            } catch (pce: ParserConfigurationException) {
                throw RuntimeException(pce)
            }
            return document
        }

        /**
         * The internal helper that copies content from the specified Jsoup <tt>Node</tt> into a W3C [Node].
         * @param node The Jsoup node containing the content to copy to the specified W3C [Node].
         * @param out The W3C [Node] that receives the DOM content.
         */
        private fun createDOM(node: org.jsoup.nodes.Node, out: Node?, doc: Document?, ns: MutableMap<String, String>) {

            when (node) {
                is org.jsoup.nodes.Document -> node.childNodes().forEach {
                    createDOM(
                        it,
                        out,
                        doc,
                        ns
                    )
                }
                is org.jsoup.nodes.Element -> {
                    val _e = doc!!.createElement(node.tagName())
                    out!!.appendChild(_e)
                    val atts = node.attributes()

                    for (a in atts) {
                        var attName = a.key
                        //omit xhtml namespace
                        if (attName == "xmlns") {
                            continue
                        }
                        val attPrefix =
                            getNSPrefix(
                                attName
                            )
                        if (attPrefix != null) {
                            if (attPrefix == "xmlns") {
                                val localName =
                                    getLocalName(
                                        attName
                                    )
                                if (localName != null) {
                                    ns[localName] = a.value
                                }
                            } else if (attPrefix != "xml") {
                                val namespace = ns[attPrefix]
                                if (namespace == null) {
                                    //fix attribute names looking like qnames
                                    attName = attName.replace(':', '_')
                                }
                            }
                        } else {
                            // valid xml attribute names are: ^[a-zA-Z_:][-a-zA-Z0-9_:.]
                            attName = attName.replace("[^-a-zA-Z0-9_:.]".toRegex(), "")
                        }
                        _e.setAttribute(attName, a.value)
                    }

                    for (n in node.childNodes()) {
                        createDOM(
                            n,
                            _e,
                            doc,
                            ns
                        )
                    }

                }
                is org.jsoup.nodes.TextNode -> {
                    // FIXME: why this test ?
                    if (out !is Document) {
                        out!!.appendChild(doc!!.createTextNode(node.wholeText))
                    }
                }
                is org.jsoup.nodes.Comment -> {
                    out!!.appendChild(doc!!.createComment(node.data))
                }
                is org.jsoup.nodes.DataNode -> {
                    out!!.appendChild(doc!!.createTextNode(node.wholeData))
                }
            }
        }

        // some hacks for handling namespace in jsoup2DOM conversion
        private fun getNSPrefix(name: String?): String? {
            if (name != null) {
                val pos = name.indexOf(':')
                if (pos > 0) {
                    return name.substring(0, pos)
                }
            }
            return null
        }

        private fun getLocalName(name: String?): String? {
            if (name != null) {
                val pos = name.lastIndexOf(':')
                if (pos > 0) {
                    return name.substring(pos + 1)
                }
            }
            return name
        }
    }

}