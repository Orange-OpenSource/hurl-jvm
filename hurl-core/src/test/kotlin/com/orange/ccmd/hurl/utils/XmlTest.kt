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

package com.orange.ccmd.hurl.utils

import com.orange.ccmd.hurl.core.utils.getXmlByteCount
import com.orange.ccmd.hurl.core.utils.isValidXml
import com.orange.ccmd.hurl.safeName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

internal class XmlTest {

    @TestFactory
    fun `return the number of bytes from a valid xml`() = listOf(

        """<?xml version="1.0" encoding="UTF-8"?>
<!-- Some Comment -->
<BookCatalogue xmlns="http://www.publishing.org">
<Book>
    <Title>Yogasana Vijnana: the Science of Yoga</Title>
    <Author>Dhirendra Brahmachari</Author>
    <Date>1966</Date>
    <ISBN>81-40-34319-4</ISBN>
    <Publisher>Dhirendra Yoga Publications</Publisher>
    <Cost currency="INR">11.50</Cost>
</Book>
<Book>
    <Title>The First and Last Freedom</Title>
    <Author>J. Krishnamurti</Author>
    <Date>1954</Date>
    <ISBN>0-06-064831-7</ISBN>
    <Publisher>Harper &amp; Row</Publisher>
    <Cost currency="USD">2.95</Cost>
</Book>
</BookCatalogue>abced""" to 620,
        """<?xml version="1.0" encoding="UTF-8"?>
<BookCatalogue xmlns="http://www.publishing.org">
</BookCatalogue>abced""" to 105,
        """<?xml version="1.0" encoding="UTF-8"?>
<fruit>
    <fruit>
    </fruit>
</fruit>xxxxx""" to 80,
    """<?xml version="1.0" encoding="UTF-8"?><note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>xxx""".trimIndent() to 150,
            """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:m="http://www.example.org">
    <soap:Header></soap:Header>
    <soap:Body>
        <m:GetStockPrice>
            <m:StockName>GOOG</m:StockName>
            </m:GetStockPrice>
    </soap:Body>
</soap:Envelope>
""" to 323,
    """<?xml version="1.0" encoding="UTF-8"?><drink>café</drink>""".trimMargin() to 58,
        """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:m="http://www.example.org">
  <soap:Header>
  </soap:Header>
  <soap:Body>
    <m:GetStockPrice>
      <m:StockName>GOOG</m:StockName>
    </m:GetStockPrice>
  </soap:Body>
</soap:Envelope>""" to 302
    ).map { (text, byteCount) ->
        DynamicTest.dynamicTest(text.safeName()) {
            assertEquals(getXmlByteCount(text = text), byteCount)
        }
    }

    @TestFactory
    fun `validity of a xml text`() = listOf(
        """<?xml version="1.0" encoding="UTF-8"?><drink>café</drink>""" to true,
        "toto" to false,
        """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:m="http://www.example.org">
  <soap:Header>
  </soap:Header>
  <soap:Body>
    <m:GetStockPrice>
      <m:StockName>GOOG</m:StockName>
    </m:GetStockPrice>
  </soap:Body>
</soap:Envelope>""" to true
    ).map  { (text, valid) ->
        DynamicTest.dynamicTest(text.safeName()) {
            assertEquals(isValidXml(text = text), valid)
        }
    }


}