/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors.ntc

import ch.qos.logback.classic.Level
import iUtils.{BaseISpec, LogCapturing, WireMockStubs}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.taxcredit._
import uk.gov.hmrc.identityverificationquestions.sources.ntc.NtcConnector

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class NtcConnectorISpec extends BaseISpec with LogCapturing with WireMockStubs {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val connector: NtcConnector = app.injector.instanceOf[NtcConnector]

  private val ninoIdentifier: Nino = Nino("AA000003D")
  private val selectionNino: Selection = Selection(ninoIdentifier)
  private val url = s"/national-tax-credits/citizens/${ninoIdentifier.value}/verification-data"

  private val date = LocalDate.parse("2024-12-01")

  private val jsonParse: String =
    """
      |{
      |  "applicant1": {
      |    "bankOrBuildingSociety": {
      |      "accountNumber": "12345678",
      |      "modifiedBacsAccountNumber": "87654321"
      |    }
      |  },
      |  "applicant2": {
      |    "bankOrBuildingSociety": {
      |      "accountNumber": "23456789",
      |      "modifiedBacsAccountNumber": null
      |    }
      |  },
      |  "previousPayment": [
      |    {
      |      "subjectDate": "2024-12-01",
      |      "amount": -150.75,
      |      "taxCreditId": "CTC",
      |      "paymentType": "REGULAR"
      |    },
      |    {
      |      "subjectDate": "2024-12-01",
      |      "amount": -200.00,
      |      "taxCreditId": "WTC",
      |      "paymentType": "REGULAR"
      |    }
      |  ]
      |}
      |""".stripMargin
  private val responseBody: String = Json.parse(jsonParse).toString()

  "get ntc returns" should {
    "successfully obtain data for valid Nino" in {
      stubGetWithResponseBody(url, OK, responseBody)
      val result: Seq[TaxCreditRecord] = await(connector.getRecords(selectionNino))

      result.toList.head shouldBe TaxCreditPayment(date, BigDecimal("150.75"), CTC)

      val sumPaymentsOnly: Seq[TaxCreditPayment] = result.collect {
        case payment: TaxCreditPayment if payment.taxCreditId == Sum => payment
      }

      sumPaymentsOnly.head shouldBe TaxCreditPayment(date, BigDecimal(350.75), Sum)
    }

    "return an empty List() if Get request throws UpstreamErrorResponse for NOT_FOUND status" in {
      withCaptureOfLoggingFrom[NtcConnector] { logs =>
        stubGetWithResponseBody(url, NOT_FOUND, responseBody)
        val result: Seq[TaxCreditRecord] = await(connector.getRecords(selectionNino))
        result shouldBe List.empty

        val infoLogs = logs.filter(_.getLevel == Level.INFO)
        infoLogs.size shouldBe 1
        infoLogs.head.getMessage should include ("taxCreditService is not available for user:")
      }
    }
  }
}
