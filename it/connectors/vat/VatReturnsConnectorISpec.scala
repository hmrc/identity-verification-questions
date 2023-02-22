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

package connectors.vat

import com.github.tomakehurst.wiremock.client.WireMock.{get, okJson, stubFor, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import iUtils.BaseISpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.{Selection, VatReturnSubmission}
import uk.gov.hmrc.identityverificationquestions.sources.vat.VatReturnsConnector

import scala.concurrent.ExecutionContext.Implicits.global

class VatReturnsConnectorISpec extends BaseISpec  {


  override def fakeApplication(): Application = GuiceApplicationBuilder().configure(
    replaceExternalDependenciesWithMockServers
      ++ Map("metrics.jvm" -> false)
  ).build()


  val vrn: String = "123456789"
  val periodKey : String = "22YA"
  val vatReturnSubmissionResponseJson: JsValue = Json.parse(
    s"""{
      |"periodKey": "$periodKey",
      |"vatDueSales": 1000,
      |"vatDueAcquisitions": 1000,
      |"vatDueTotal": 1000,
      |"vatReclaimedCurrPeriod": 1000,
      |"vatDueNet": 1000,
      |"totalValueSalesExVAT": 1000,
      |"totalValuePurchasesExVAT": 500.50,
      |"totalValueGoodsSuppliedExVAT": 1000,
      |"totalAllAcquisitionsExVAT": 1000
      |}""".stripMargin
  )
  def vatStub: StubMapping =
    stubFor(
      get(
        urlEqualTo(s"/vat/returns/vrn/$vrn?period-key=$periodKey"))
        .willReturn(
          okJson(vatReturnSubmissionResponseJson.toString())
        )
    )

  "get vatReturns data" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    "successfully obtain the vatReturnSubmission data" in {
      vatStub
      val expectedVatReturnSubmissionData = Seq(VatReturnSubmission("22YA", BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("500.50"), BigDecimal("1000"), BigDecimal("1000")))
      val vrnIdentifier: Vrn = Vrn("123456789")
      val selectionVrn: Selection = Selection(vrnIdentifier)

      val connector: VatReturnsConnector = fakeApplication().injector.instanceOf[VatReturnsConnector]

      val result = await(connector.getRecords(selectionVrn))

      result shouldBe expectedVatReturnSubmissionData
    }

  }
}
