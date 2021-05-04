/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package iUtils

import com.github.tomakehurst.wiremock.client.WireMock.{get, notFound, okJson, serverError, stubFor, urlEqualTo, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import iUtils.TestData.SCPEmailTestData
import play.api.libs.json.{JsValue, Json}

trait WireMockStubs extends SCPEmailTestData {
  def p60ProxyReturnOk(payments: JsValue): StubMapping =
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
