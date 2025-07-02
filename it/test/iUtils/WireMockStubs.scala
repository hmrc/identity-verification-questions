/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iUtils

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.{Fault, HttpHeader}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import iUtils.TestData.SCPEmailTestData
import play.api.libs.json.{JsValue, Json}

trait WireMockStubs extends SCPEmailTestData {
  def rtiProxyReturnOk(payments: JsValue): StubMapping =
    stubFor(
      get(
        urlMatching("/rti/individual/payments/nino/AA000000/tax-year/([0-9]{2}+(-[0-9]{2}))"))
        .willReturn(
          okJson(
            Json.toJson(payments).toString()
          )
        )
    )

  def p60ProxyReturnNotFound: StubMapping =
    stubFor(
      get(
        urlMatching("/rti/individual/payments/nino/AA000000/tax-year/([0-9]{2}+(-[0-9]{2}))"))
        .willReturn(
          notFound()
        )
    )

  def p60ProxyReturnError: StubMapping =
    stubFor(
      get(
        urlMatching("/rti/individual/payments/nino/AA000000/tax-year/([0-9]{2}+(-[0-9]{2}))"))
        .willReturn(
          serverError()
        )
    )

  def ivReturnOk: StubMapping =
    stubFor(
      get(
        urlEqualTo(s"/identity-verification/nino?nino=$nino"))
        .willReturn(
          okJson(ninoClStoreResponseJson.toString())
        )
    )

  def basGatewayStub: StubMapping =
    stubFor(
      get(
        urlEqualTo(s"/bas-proxy/credentials/$credId"))
        .willReturn(
          okJson(accountInfoResponseJson.toString())
        )
    )

  def stubPostWithoutResponseBody(url: String, status: Int, requestBody: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(post(urlEqualTo(url)).withRequestBody(equalToJson(requestBody))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def stubPostWithError(url: String, requestBody: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(post(urlEqualTo(url)).withRequestBody(equalToJson(requestBody))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders
      .willReturn(
        aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)
      )
    )
  }

  def stubPostWithResponseBody(url: String, status: Int, requestBody: String, response: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(post(urlEqualTo(url)).withRequestBody(equalToJson(requestBody))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
          .withHeader("Content-Type", "application/json; charset=utf-8")))

  }

  def stubGetWithResponseBody(url: String, status: Int, response: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(get(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def stubGetWithoutResponseBody(url: String, status: Int): StubMapping =
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)))

  def stubGetWithError(url: String): StubMapping =
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse().withFault(Fault.EMPTY_RESPONSE)))
}
