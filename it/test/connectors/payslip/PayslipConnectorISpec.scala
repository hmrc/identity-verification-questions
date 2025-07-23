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

package connectors.payslip

import ch.qos.logback.classic.Level
import iUtils.{BaseISpec, LogCapturing, TestTaxYearBuilder, WireMockStubs}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipConnector

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class PayslipConnectorISpec extends BaseISpec with LogCapturing with WireMockStubs with TestTaxYearBuilder {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)

  private val paymentDate: LocalDate = LocalDate.now().minusMonths(1).minusDays(25)
  private val jsonParse: String =
    s"""
      |{
      |  "individual": {
      |    "employments": {
      |      "employment": [
      |        {
      |          "payments": {
      |            "inYear": [
      |              {
      |                "payId": "20425",
      |                "leavingDate": "2012-06-22",
      |                "payFreq": "IO",
      |                "mandatoryMonetaryAmount": [
      |                  {
      |                    "type": "TaxablePayYTD",
      |                    "amount": 0
      |                  },
      |                  {
      |                    "type": "TaxablePay",
      |                    "amount": 102.02
      |                  },
      |                  {
      |                    "type": "TotalTaxYTD",
      |                    "amount": 130.99
      |                  },
      |                  {
      |                    "type": "TaxDeductedOrRefunded",
      |                    "amount": 155.02
      |                  }
      |                ],
      |                "niLettersAndValues": [
      |                  {
      |                    "niFigure": [
      |                      {
      |                        "type": "EmpeeContribnsInPd",
      |                        "amount": 100.02
      |                      },
      |                      {
      |                        "type": "EmpeeContribnsYTD",
      |                        "amount": 130.99
      |                      }
      |                    ]
      |                  }
      |                ],
      |                "starter": {
      |                  "startDate": "2011-08-13"
      |                },
      |                "pmtDate": "$paymentDate",
      |                "rcvdDate": "2015-04-06",
      |                "taxYear": "14-15"
      |              }
      |            ]
      |          }
      |        }
      |      ]
      |    }
      |  }
      |}
      |""".stripMargin
  private val responseBody: String = Json.parse(jsonParse).toString()

  "get payslip returns" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    lazy val connector: PayslipConnector = app.injector.instanceOf[PayslipConnector]

    val ninoIdentifier: Nino = Nino("AA000003D")
    val selectionNino: Selection = Selection(ninoIdentifier)
    val url = s"/rti/individual/payments/nino/${ninoIdentifier.withoutSuffix}/tax-year/${currentTaxYear.yearForUrl}"

    "successfully obtain data for valid NINO" in {
      stubGetWithResponseBody(url, OK, responseBody)
      val result: Seq[Payment] = await(connector.getRecords(selectionNino))

      println("Local Date = " + LocalDate.now().minusMonths(1).minusDays(25))

      result shouldBe List(Payment(paymentDate, Some(0), Some(130.99), Some(155.02), Some(100.02), None, None, None, None, None, None, Some(LocalDate.parse("2012-06-22")), Some(130.99)))
    }

    "return empty Seq() AND warnLogs if no NINO is given" in {
      withCaptureOfLoggingFrom[PayslipConnector] { logs =>
        stubGetWithResponseBody(url, OK, responseBody)
        val payeRef: EmpRef = EmpRef("fakeTaxOfficeNumber", "fakeTaxOfficeReference")
        val selectionPayeRef: Selection = Selection(payeRef)
        val result: Seq[Payment] = await(connector.getRecords(selectionPayeRef))

        result shouldBe Seq.empty

        val warnLogs = logs.filter(_.getLevel == Level.WARN)
        warnLogs.size shouldBe 1
        warnLogs.head.getMessage should include (s"payslipService, No nino identifier for selection: $selectionPayeRef")
      }
    }

    "return UpstreamErrorResponse and empty Seq() when P60 data not found" in {
      withCaptureOfLoggingFrom[PayslipConnector] { logs =>
        stubGetWithResponseBody(url, NOT_FOUND, responseBody)
        val result: Seq[Payment] = await(connector.getRecords(selectionNino))
        result shouldBe Seq.empty

        val infoLogs = logs.filter(_.getLevel == Level.INFO)
        infoLogs.size shouldBe 1
        infoLogs.head.getMessage should include ("payslipService is not available for user:")
      }
    }
  }
}
