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

package connectors.p60

import ch.qos.logback.classic.Level
import iUtils.{BaseISpec, LogCapturing, TestTaxYearBuilder, WireMockStubs}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Connector

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class P60ConnectorISpec extends BaseISpec with LogCapturing with WireMockStubs with TestTaxYearBuilder {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)

  private val jsonParse: String =
    """
      |{
      |  "individual": {
      |    "employments": {
      |      "employment": [
      |        {
      |          "payments": {
      |            "inYear": [
      |              {
      |                "payId": "65553-1808",
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
      |                    "amount": 120.99
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
      |                        "amount": 0
      |                      },
      |                      {
      |                        "type": "PTtoUELYTD",
      |                        "amount": 0
      |                      }
      |                    ]
      |                  }
      |                ],
      |                "optionalMonetaryAmount": [
      |                  {
      |                    "type": "SMPYTD",
      |                    "amount": 300.02
      |                  },
      |                  {
      |                    "type": "StudentLoansYTD",
      |                    "amount": 800.02
      |                  },
      |                  {
      |                    "type": "SPBYTD",
      |                    "amount": 0
      |                  },
      |                  {
      |                    "type": "SPPYTD",
      |                    "amount": 0
      |                  },
      |                  {
      |                    "type": "SHPPYTD",
      |                    "amount": 0
      |                  },
      |                  {
      |                    "type": "PostGraduateLoansYTD",
      |                    "amount": 0
      |                  },
      |                  {
      |                    "type": "SAPYTD",
      |                    "amount": 0
      |                  }
      |                ],
      |                "starter": {
      |                  "startDate": "2013-09-07"
      |                },
      |                "pmtDate": "2015-04-06",
      |                "rcvdDate": "2015-04-06",
      |                "taxYear": "15-16"
      |              }
      |            ]
      |          },
      |          "sequenceNumber": 16
      |        }
      |      ]
      |    }
      |  }
      |}
      |""".stripMargin
  private val responseBody: String = Json.parse(jsonParse).toString()

  "get p60 returns" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    lazy val connector: P60Connector = app.injector.instanceOf[P60Connector]
    val ninoIdentifier: Nino = Nino("AA002022B")
    val selectionNino: Selection = Selection(ninoIdentifier)
    val url = s"/rti/individual/payments/nino/${ninoIdentifier.withoutSuffix}/tax-year/${currentTaxYear.previous.yearForUrl}"

    "successfully obtain data for valid NINO" in {
      stubGetWithResponseBody(url, OK, responseBody)
      val result: Seq[Payment] = await(connector.getRecords(selectionNino))
      result.toList.head shouldBe Payment(LocalDate.parse("2015-04-06"), Some(0), Some(0), Some(155.02), Some(100.02),
                                          earningsAbovePT = Some(0),
                                          statutoryMaternityPay = Some(300.02),
                                          statutorySharedParentalPay = Some(0),
                                          statutoryAdoptionPay = Some(0),
                                          studentLoanDeductions = Some(800.02),
                                          postgraduateLoanDeductions = Some(0),
                                          totalTaxYTD = Some(120.99))
    }

    "return empty Seq() AND warnLogs if no NINO is given" in {
      withCaptureOfLoggingFrom[P60Connector] { logs =>
        stubGetWithResponseBody(url, OK, responseBody)
        val payeRef: EmpRef = EmpRef("fakeTaxOfficeNumber", "fakeTaxOfficeReference")
        val selectionPayeRef: Selection = Selection(payeRef)
        val result: Seq[Payment] = await(connector.getRecords(selectionPayeRef))

        result shouldBe Seq.empty

        val warnLogs = logs.filter(_.getLevel == Level.WARN)
        warnLogs.size shouldBe 1
        warnLogs.head.getMessage should include (s"p60Service, No nino identifier for selection: $selectionPayeRef")
      }
    }

    "return UpstreamErrorResponse and empty Seq() when P60 data not found" in {
      withCaptureOfLoggingFrom[P60Connector] { logs =>
        stubGetWithResponseBody(url, NOT_FOUND, responseBody)
        val result: Seq[Payment] = await(connector.getRecords(selectionNino))
        result shouldBe Seq.empty

        val infoLogs = logs.filter(_.getLevel == Level.INFO)
        infoLogs.size shouldBe 1
        infoLogs.head.getMessage should include ("p60Service is not available for user:")
      }
    }
  }
}
