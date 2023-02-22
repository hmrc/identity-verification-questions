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

package uk.gov.hmrc.identityverificationquestions.sources.vat

import Utils.UnitSpec
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpGet, HttpReads}
import uk.gov.hmrc.identityverificationquestions.config.{AppConfig, HodConf}
import uk.gov.hmrc.identityverificationquestions.models.{Selection, ServiceName, VatReturnSubmission}

import scala.concurrent.{ExecutionContext, Future}

class VatReturnsConnectorSpec extends UnitSpec {

  "the VatReturnConnector" should {
    "return the VatReturnSubmission records" in new Setup {
      (mockAppConfig.hodConfiguration(_:ServiceName)).expects(*).returning(Right(HodConf("theLocalDevToken","localDev")))
      (mockAppConfig.serviceBaseUrl(_:String)).expects("vatService").returning("http://localhost:7780")
      (httpClientMock.GET[VatReturnSubmission](_: String, _: Seq[(String, String)], _: Seq[(String, String)])
        (_: HttpReads[VatReturnSubmission], _: HeaderCarrier, _: ExecutionContext))
        .expects(s"http://localhost:7780/vat/returns/vrn/${validVrn.vrn}", *, *, *, *, *)
        .returning(Future.successful(validVatReturnSubmission))

      val selection: Selection = Selection(None, None, None, None, Some(validVrn))
      vatReturnsConnector.getRecords(selection).futureValue should contain(validVatReturnSubmission)
    }
  }

  trait Setup {
    val httpClientMock: HttpGet = mock[HttpGet]
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    val vatReturnsConnector: VatReturnsConnector = new VatReturnsConnector(httpClientMock)

    implicit val hc: HeaderCarrier = HeaderCarrier().copy(authorization = Some(Authorization(s"Bearer ")), extraHeaders = Seq("Environment" -> ""))
    implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

    val validVatReturnSubmission: VatReturnSubmission = VatReturnSubmission(
      "22YA", BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("500.50"), BigDecimal("1000"), BigDecimal("1000")
    )
    val validVrn: Vrn = Vrn("123456789")
  }

}
