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

import com.github.tomakehurst.wiremock.client.WireMock.{get, notFound, okJson, serverError, stubFor, urlEqualTo, urlMatching}
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
}
