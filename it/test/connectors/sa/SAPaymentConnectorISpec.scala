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

package connectors.sa

import iUtils.{BaseISpec, WireMockStubs}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.sources.sa.{SAPayment, SAPaymentReturn, SAPaymentsConnector}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SAPaymentConnectorISpec extends BaseISpec with WireMockStubs {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def await[A](future: Future[A]): A = Await.result(future, 50.second)
  private val connector : SAPaymentsConnector = app.injector.instanceOf[SAPaymentsConnector]

  private val validSa = SAPaymentReturn(List(SAPayment(3800, Some(LocalDate.parse("2011-01-31")), Some("PYT")), SAPayment(1515, Some(LocalDate.parse("2011-07-31")), Some("BCC")), SAPayment(0, Some(LocalDate.parse("2011-05-31")), Some("BCC"))))
  private val utr = "1234567890"
  private val url = s"/individuals/self-assessment/payments/utr/$utr"
  private val sampleArrayOfResponsesString: String =
    """
      |{
      |  "paymentsList": [
      |    {
      |      "createdDate": "2011-01-31",
      |      "transactionCode": "PYT",
      |      "amount": {
      |        "amount": 3800,
      |        "currency": "GBP"
      |      },
      |      "transactionId": {
      |        "tieBreaker": 9534,
      |        "sequenceNumber": null,
      |        "creationDate": "2011-01-31"
      |      },
      |      "taxYearEnd": "null"
      |    },
      |    {
      |      "createdDate": "2011-07-31",
      |      "transactionCode": "BCC",
      |      "amount": {
      |        "amount": 1515,
      |        "currency": "GBP"
      |      },
      |      "transactionId": {
      |        "tieBreaker": 1234,
      |        "sequenceNumber": null,
      |        "creationDate": "2011-07-31"
      |      },
      |      "taxYearEnd": "2012-04-05"
      |    },
      |    {
      |      "createdDate": "2011-05-31",
      |      "transactionCode": "BCC",
      |      "amount": {
      |        "amount": 0,
      |        "currency": "GBP"
      |      },
      |      "transactionId": {
      |        "tieBreaker": 1234,
      |        "sequenceNumber": null,
      |        "creationDate": "2011-05-31"
      |      },
      |      "taxYearEnd": "2012-04-05"
      |    }
      |  ]
      |}
      |""".stripMargin
  private val responseBody: String = Json.parse(sampleArrayOfResponsesString).toString()

  "get sa payment returns" should {
    "successfully obtain a return" in {
      stubGetWithResponseBody(url, OK, responseBody)
      val result: Seq[SAPaymentReturn] = await(connector.getReturns(SaUtr(utr)))

      result shouldBe Seq(validSa)
    }

    "return an empty List() if Get request throws UpstreamErrorResponse for NOT_FOUND status" in {
      stubGetWithResponseBody(url, NOT_FOUND, responseBody)
      val result: Seq[SAPaymentReturn] = await(connector.getReturns(SaUtr(utr)))

      result shouldBe List.empty
    }
  }

}
