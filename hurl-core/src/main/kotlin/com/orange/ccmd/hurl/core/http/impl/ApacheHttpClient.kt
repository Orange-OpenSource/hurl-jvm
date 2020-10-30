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

import com.orange.ccmd.hurl.core.http.BasicAuthentification
import com.orange.ccmd.hurl.core.http.BinaryRequestBody
import com.orange.ccmd.hurl.core.http.Cookie
import com.orange.ccmd.hurl.core.http.Encoding
import com.orange.ccmd.hurl.core.http.FileFormData
import com.orange.ccmd.hurl.core.http.HeaderNames.ACCEPT_ENCODING
import com.orange.ccmd.hurl.core.http.HeaderNames.AUTHORIZATION
import com.orange.ccmd.hurl.core.http.HeaderNames.CONTENT_ENCODING
import com.orange.ccmd.hurl.core.http.HeaderNames.CONTENT_TYPE
import com.orange.ccmd.hurl.core.http.HeaderNames.COOKIE
import com.orange.ccmd.hurl.core.http.HeaderNames.USER_AGENT
import com.orange.ccmd.hurl.core.http.HttpClient
import com.orange.ccmd.hurl.core.http.HttpRequest
import com.orange.ccmd.hurl.core.http.HttpResponse
import com.orange.ccmd.hurl.core.http.HttpResult
import com.orange.ccmd.hurl.core.http.JsonRequestBody
import com.orange.ccmd.hurl.core.http.Mime
import com.orange.ccmd.hurl.core.http.Proxy
import com.orange.ccmd.hurl.core.http.TextFormData
import com.orange.ccmd.hurl.core.http.XmlRequestBody
import com.orange.ccmd.hurl.core.utils.addTimer
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
import org.apache.http.impl.cookie.RFC6265StrictSpec
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
    val httpProxy: Proxy? = null,
    val authentification: BasicAuthentification? = null,
    val compressed: Boolean = false,
    val connectTimeoutInSecond: Int = 60,
    val maxTime: Int? = null
) : HttpClient {
    private val client: CloseableHttpClient
    private val cookieStore: CookieStore
    private val requestInterceptor: CapturedRequestInterceptor = CapturedRequestInterceptor()


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
            .setConnectTimeout(connectTimeoutInSecond * 1000)
            .build()

        builder = builder
            .setDefaultCookieStore(cookieStore)
            .disableRedirectHandling()
            .disableContentCompression()
            .setDefaultRequestConfig(globalConfig)
        // Configure proxy if a proxy have been provided:
        if (httpProxy != null) {
            val proxyHost = HttpHost(httpProxy.host, httpProxy.port)
            val routePlanner = DefaultProxyRoutePlanner(proxyHost)
            builder = builder.setRoutePlanner(routePlanner)
        }

        // Add interceptor to merged multiple Cookie header in one cookie
        builder.addInterceptorLast(CookieRequestInterceptor())

        // Add interceptor to the client so we can fully log the request
        // (for instance, all headers set by the Apache implementation).
        builder.addInterceptorLast(requestInterceptor)

        client = builder.build()
    }

    override fun execute(request: HttpRequest): HttpResult {

        val builder = RequestBuilder.create(request.method)

        request.prepareUri(builder = builder)
        request.prepareHeaders(builder = builder, authentification = authentification, compressed = compressed)
        request.prepareBody(builder = builder)
        request.prepareCookies(builder = builder)


        val uri = builder.uri
        val targetHost = if (httpProxy == null) { null } else { HttpHost(uri.host, uri.port, uri.scheme) }
        val req = builder.build()
        if (maxTime != null) {
            addTimer(maxTime) {
                req.abort()
            }
        }
        val resp = if (httpProxy == null) {
            client.execute(req)
        } else {
            client.execute(targetHost, req)
        }

        // We get the request log to have the final real list of HTTP headers,
        // specified by the spec, and added by the http client.
        val finalizedRequest =
            requestInterceptor.capturedRequest ?: throw IllegalStateException("HTTP request not executed")

        // Construct the HttpResponse from the apache response object.
        val contentType = ContentType.getOrDefault(resp.entity)
        val respBody = resp.entity?.content?.readBytes() ?: ByteArray(size = 0)
        val respHeaders = resp.allHeaders.map { it.name to it.value }
        val statusCode = resp.statusLine.statusCode
        val version = resp.statusLine.protocolVersion.toString()
        resp.close()

        val contentEncodingHeader = getHeader(headers = respHeaders, name = CONTENT_ENCODING)
        val encodings = contentEncodingHeader
            ?.second
            ?.split(",")
            ?.mapNotNull { Encoding.fromValue(it.trim()) }

        val response = HttpResponse(
            version = version,
            code = statusCode,
            headers = respHeaders,
            charset = contentType.charset ?: Charsets.UTF_8,
            mimeType = contentType.mimeType,
            body = respBody,
            encodings = encodings ?: emptyList()
        )

        val cookies = cookieStore.cookies.map {
            Cookie(
                domain = it.domain,
                path = it.value,
                secure = it.isSecure,
                expires = it.expiryDate,
                name = it.name,
                value = it.value,
            )
        }

        return HttpResult(
            request = request,
            finalizedRequest = finalizedRequest,
            response = response,
            cookies = cookies
        )
    }

    private fun getHeader(headers: List<Pair<String, String>>, name: String): Pair<String, String>? {
        return headers.firstOrNull { it.first.toLowerCase() == name.toLowerCase() }
    }


    override fun addCookie(cookie: Cookie) {
        val cookieImp = BasicClientCookie(cookie.name, cookie.value)
        cookieImp.domain = cookie.domain
        cookieImp.path = cookie.path
        if (cookie.secure != null) {
            cookieImp.isSecure = cookie.secure
        }
        if (cookie.expires != null) {
            cookieImp.expiryDate = cookie.expires
        }
        cookieStore.addCookie(cookieImp)
    }

    override fun clearCookieStorage() {
        cookieStore.clear()
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

/**
 * Add HTTP headers to a request builder
 * @param builder a request builder
 * @param authentification an optional basic authentification
 * @param compressed request a compressed response
 */
internal fun HttpRequest.prepareHeaders(
    builder: RequestBuilder,
    authentification: BasicAuthentification? = null,
    compressed: Boolean = false
) {

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

    // If no header Content-Type has been specified, we infer a default Content-Type
    // header depending on the body type specified.
    if (headersForName(CONTENT_TYPE).isEmpty()) {
        when (body) {
            is JsonRequestBody -> {
                builder.addHeader(CONTENT_TYPE, "application/json")
            }
            is XmlRequestBody -> {
                builder.addHeader(CONTENT_TYPE, "text/xml")
            }
            is BinaryRequestBody -> {
            }
        }
    }

    if (compressed) {
        builder.addHeader(ACCEPT_ENCODING, "br, gzip, deflate")
    }

    if (authentification != null) {
        builder.addHeader(AUTHORIZATION, authentification.headerValue)
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

/**
 * Add request cookies on existent cookie store.
 *
 * The cookies are only added for this request, and are not part of the cookie store.
 */
internal fun HttpRequest.prepareCookies(builder: RequestBuilder) {

    val overriddenCookies = cookies.map { BasicClientCookie(it.name, it.value) }
    if (overriddenCookies.isEmpty()) {
        return
    }
    val spec = RFC6265StrictSpec()
    val headers = spec.formatCookies(overriddenCookies)
    headers.forEach {
        builder.addHeader(COOKIE, it.value)
    }
}