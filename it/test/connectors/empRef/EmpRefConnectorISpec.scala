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

package connectors.empRef

import ch.qos.logback.classic.Level
import iUtils.{BaseISpec, LogCapturing, WireMockStubs}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.{PayePayment, PayePaymentAmount, PayePaymentsDetails, Selection}
import uk.gov.hmrc.identityverificationquestions.sources.empRef.EmpRefConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class EmpRefConnectorISpec extends BaseISpec with LogCapturing with WireMockStubs {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)

  "EmpRefConnector" should {
    lazy val connector: EmpRefConnector = app.injector.instanceOf[EmpRefConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val validPaymentDetails = PayePaymentsDetails(Some(List(PayePayment(PayePaymentAmount(BigDecimal("1234"), "GBP"), "2025-03-03"))))
    val responseBody = Json.toJson(validPaymentDetails).toString()
    val payeRef: EmpRef = EmpRef("fakeTaxOfficeNumber", "fakeTaxOfficeReference")
    val selectionPayeRef: Selection = Selection(payeRef)
    val url = "/pay-as-you-earn/employers/fakeTaxOfficeNumber/fakeTaxOfficeReference/account/payments"

    "successfully execute a valid request" in {
      stubGetWithResponseBody(url, OK, responseBody)
      val result: Seq[PayePaymentsDetails] = await(connector.getRecords(selectionPayeRef))

      result.toList.head shouldBe validPaymentDetails
    }

    "return empty Seq() AND warnLogs if no NINO is given" in {
      withCaptureOfLoggingFrom[EmpRefConnector] { logs =>
        stubGetWithResponseBody(url, OK, responseBody)
        val ninoIdentifier: Nino = Nino("AA000003B")
        val selectionNino: Selection = Selection(ninoIdentifier)
        val result: Seq[PayePaymentsDetails] = await(connector.getRecords(selectionNino))

        result shouldBe Seq.empty

        val warnLogs = logs.filter(_.getLevel == Level.WARN)
        warnLogs.size shouldBe 1
        warnLogs.head.getMessage should include (s"desPayeService, No payeRef for selection: $selectionNino")
      }
    }

    "return PayePaymentsDetails(None) for an empty request" in {
      val emptyPaymentDetails = PayePaymentsDetails(None)

      stubGetWithResponseBody(url, OK, Json.toJson(emptyPaymentDetails).toString())
      val result: Seq[PayePaymentsDetails] = await(connector.getRecords(selectionPayeRef))

      result.toList.head shouldBe emptyPaymentDetails
    }

    "return an empty Seq() if Get request throws UpstreamErrorResponse for NOT_FOUND status" in {
      withCaptureOfLoggingFrom[EmpRefConnector] { logs =>
        stubGetWithResponseBody(url, NOT_FOUND, responseBody)
        val result: Seq[PayePaymentsDetails] = await(connector.getRecords(selectionPayeRef))
        result shouldBe Seq.empty

        val infoLogs = logs.filter(_.getLevel == Level.INFO)
        infoLogs.size shouldBe 1
        infoLogs.head.getMessage should include ("desPayeService is not available for user")
      }
    }
  }

}
