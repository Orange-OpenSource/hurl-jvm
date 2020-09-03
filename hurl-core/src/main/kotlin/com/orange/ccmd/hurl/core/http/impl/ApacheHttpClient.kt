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

package com.orange.ccmd.hurl.core.http.impl

import com.orange.ccmd.hurl.core.http.BinaryRequestBody
import com.orange.ccmd.hurl.core.http.CONTENT_LENGTH
import com.orange.ccmd.hurl.core.http.CONTENT_TYPE
import com.orange.ccmd.hurl.core.http.Cookie
import com.orange.ccmd.hurl.core.http.FileFormData
import com.orange.ccmd.hurl.core.http.HttpClient
import com.orange.ccmd.hurl.core.http.HttpRequest
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.http.HttpResult
import com.orange.ccmd.hurl.core.http.JsonRequestBody
import com.orange.ccmd.hurl.core.http.Mime
import com.orange.ccmd.hurl.core.http.Proxy
import com.orange.ccmd.hurl.core.http.TextFormData
import com.orange.ccmd.hurl.core.http.USER_AGENT
import com.orange.ccmd.hurl.core.http.XmlRequestBody
import org.apache.http.HttpHost
import org.apache.http.client.CookieStore
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.entity.ContentType
import org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicNameValuePair
import org.apache.http.ssl.SSLContextBuilder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8

/**
 * Implements {@link HttpClient} with Apache Http client.
 * Given a {@link HttpRequestSpec}, this client can synchronously execute
 * an HTTP request and returns a {@link HttpResponse}.
 */
internal class ApacheHttpClient(
    val allowsInsecure: Boolean = false,
    val httpProxy: Proxy? = null
): HttpClient {
    private val client: CloseableHttpClient
    private val cookieStore: CookieStore
    private val preparedRequestInterceptor: PreparedRequestInterceptor = PreparedRequestInterceptor()

    init {
        cookieStore = BasicCookieStore()

        var builder = HttpClients.custom()

        if (allowsInsecure) {
            val sslContextBuilder = SSLContextBuilder()
            sslContextBuilder.loadTrustMaterial(null) { _, _ -> true }
            val sslsf = SSLConnectionSocketFactory(sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE)
            builder.setSSLSocketFactory(sslsf)
        }

        val globalConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.STANDARD)
            .build()

        builder = builder
            .setDefaultCookieStore(cookieStore)
            .disableRedirectHandling()
            .setDefaultRequestConfig(globalConfig)
        // Configure proxy if a proxy have been provided:
        if (httpProxy != null) {
            val proxyHost = HttpHost(httpProxy.host, httpProxy.port)
            val routePlanner = DefaultProxyRoutePlanner(proxyHost)
            builder = builder.setRoutePlanner(routePlanner)
        }

        // Add interceptor to the client so we can fully log the request
        // (for instance, all headers set by the Apache implementation).
        builder.addInterceptorLast(preparedRequestInterceptor)

        client = builder.build()
    }

    override fun execute(request: HttpRequest): HttpResult {

        val builder = RequestBuilder.create(request.method)

        request.prepareUri(builder = builder)
        request.prepareHeaders(builder = builder)
        request.prepareBody(builder = builder)
        request.prepareCookies(builder = builder, cookieStore = cookieStore)

        val resp = if (httpProxy == null) {
            val req = builder.build()
            client.execute(req)
        } else {
            val uri = builder.uri
            val targetHost = HttpHost(uri.host, uri.port, uri.scheme)
            val req = builder.build()
            client.execute(targetHost, req)
        }

        // We get the request log to have the final real list of HTTP headers,
        // specified by the spec, and added by the http client.
        val requestLog = preparedRequestInterceptor.preparedRequestLog ?: throw IllegalStateException("HTTP request not executed")

        // Construct the HttpResponse from the apache response object.
        val contentType = ContentType.getOrDefault(resp.entity)
        val respBody = resp.entity?.content?.readBytes() ?: ByteArray(size = 0)
        val respHeaders = resp.allHeaders.map { it.name to it.value }
        val statusCode = resp.statusLine.statusCode
        val version = resp.statusLine.protocolVersion.toString()
        resp.close()

        val response = HttpResponse(
            version = version,
            code = statusCode,
            headers = respHeaders,
            charset = contentType.charset ?: Charsets.UTF_8,
            mimeType = contentType.mimeType,
            body = respBody
        )

        val cookies = cookieStore.cookies.map { Cookie(it.name, it.value) }

        return HttpResult(request = request, requestLog = requestLog, response = response, cookies = cookies)
    }
}

internal fun HttpRequest.prepareUri(builder: RequestBuilder) {
    var url = url

    // FIXME: either better explain the following code or deactivate it.
    //  I don't see any reason why I've added this lines...

    // We allow curly brace in url even if RFC3986 disallow it.
    url = url.replace("{", "%7B")
        .replace("}", "%7D")

    // We add the potential query string parameters on the url builder, and not on the request builder
    // because for PUT and POST request, Apache client transform query strings parameters to url encoded
    // body. We don't want that, in this case, we explicity want query string to be part of the url.
    var uriBuilder = URIBuilder(url)
    queryStringParams.forEach {
        uriBuilder = uriBuilder.addParameter(it.name, it.value)
    }

    val uri = uriBuilder.build()
    builder.uri = uri
}

internal fun HttpRequest.prepareHeaders(builder: RequestBuilder) {
    // TODO: Add default HTTP headers: User-Agent, Host, etc...
    builder.addHeader(USER_AGENT, "hurl-jvm/x.x.x")

    headers.forEach {
        // TODO: header in hurl can be any UTF-8 string. Usually, you can't
        // use other encoding than US-ASCII. So currently, we encode the UTF-8 bytes
        // with ISO_8859_1, conforming to the integration test headers.hurl. As a better
        // code, we should not accept headers that are not in US-ASCII format.
        val headerValueBytes = it.value.toByteArray(Charsets.UTF_8)
        val headerValue = String(headerValueBytes, Charsets.ISO_8859_1)
        builder.addHeader(it.name, headerValue)
    }

    // We add default Content-Length HTTP header only if request has no specified body.
    if (body == null && formParams.isEmpty() && multipartFormDatas.isEmpty()) {
        val contentLength = body?.data?.size ?: 0
        builder.addHeader(CONTENT_LENGTH, "$contentLength")
    }

    // If no header Content-Type has been specified, we infer a default Content-Type
    // header depending on the body type specified.
    if (headersForName(CONTENT_TYPE).isEmpty()) {
        when (body) {
            is JsonRequestBody -> { builder.addHeader(CONTENT_TYPE, "application/json") }
            is XmlRequestBody -> { builder.addHeader(CONTENT_TYPE, "text/xml") }
            is BinaryRequestBody -> {}
        }
    }
}

internal fun HttpRequest.prepareBody(builder: RequestBuilder) = when {

    multipartFormDatas.isNotEmpty() -> {
        val entityBuilder = MultipartEntityBuilder.create()
        multipartFormDatas.forEach {
            when (it) {
                // ContentType
                // See section 4.5 of RFC7578 https://tools.ietf.org/html/rfc7578#section-4.5
                // > In the case where the form data is text, the charset parameter for
                // > the "text/plain" Content-Type MAY be used to indicate the character
                // > encoding used in that part.  For example, a form with a text field in
                // > which a user typed "Joe owes <eu>100", where <eu> is the Euro symbol,
                // > might have form data returned as:
                // > --AaB03x
                // > content-disposition: form-data; name="field1"
                // > content-type: text/plain;charset=UTF-8
                // > content-transfer-encoding: quoted-printable
                // >
                // > Joe owes =E2=82=AC100.
                // > --AaB03x
                is TextFormData -> {
                    val contentType = ContentType.create("text/plain", UTF_8)
                    entityBuilder.addTextBody(it.name, it.value, contentType)
                }
                // See section 4.4 of RFC7578 https://tools.ietf.org/html/rfc7578#section-4.4
                // > Each part MAY have an (optional) "Content-Type" header field, which
                // > defaults to "text/plain".  If the contents of a file are to be sent,
                // > the file data SHOULD be labeled with an appropriate media type, if
                // > known, or "application/octet-stream".
                is FileFormData -> {
                    val knownContentType = Mime.getContentType(fileName = it.fileName)
                    val contentType = when {
                        it.contentType != null -> ContentType.create(it.contentType)
                        knownContentType != null -> ContentType.create(knownContentType)
                        else -> APPLICATION_OCTET_STREAM
                    }
                    entityBuilder.addBinaryBody(it.name, it.value, contentType, it.fileName)
                }
            }
        }
        val entity = entityBuilder.build()
        builder.entity = entity
    }
    formParams.isNotEmpty() -> {
        val params = formParams.map { BasicNameValuePair(it.name, it.value) }
        val entity = UrlEncodedFormEntity(params, Charset.forName("UTF-8"))
        builder.entity = entity
    }
    body != null -> {
        val entity = EntityBuilder.create().setBinary(body.data).build()
        builder.entity = entity
    }
    else -> {
    }
}

internal fun HttpRequest.prepareCookies(builder: RequestBuilder, cookieStore: CookieStore) {
    cookies.forEach {
        val cookie = BasicClientCookie(it.name, it.value)
        cookie.domain = builder.uri.host
        cookie.path = "/"
        cookieStore.addCookie(cookie)
    }
}