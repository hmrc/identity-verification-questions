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

package connectors.p45

import ch.qos.logback.classic.Level
import iUtils.{BaseISpec, LogCapturing, TestTaxYearBuilder, WireMockStubs}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.sources.P45.P45Connector

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class P45ConnectorISpec extends BaseISpec with LogCapturing with WireMockStubs with TestTaxYearBuilder {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)

  private val currentJsonParse: String =
    """
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
      |                "pmtDate": "2015-04-06",
      |                "rcvdDate": "2015-04-06",
      |                "taxYear": "14-15"
      |              },
      |              {
      |                "payId": "20426",
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
      |                "pmtDate": "2015-04-05",
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
  private val previousJsonParse: String =
    """
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
      |                "pmtDate": "2014-04-03",
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
  private val currentResponseBody: String = Json.parse(currentJsonParse).toString()
  private val previousResponseBody: String = Json.parse(previousJsonParse).toString()
  private val emptyReponseBody: String = Json.parse("{}").toString()

  "get p45 returns" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val connector: P45Connector = app.injector.instanceOf[P45Connector]
    val ninoIdentifier: Nino = Nino("AA000003B")
    val selectionNino: Selection = Selection(ninoIdentifier)
    val currentResultData = Payment(LocalDate.parse("2015-04-06"), Some(0), Some(130.99), Some(155.02), Some(100.02), None, None, None, None, None, None, Some(LocalDate.parse("2012-06-22")), Some(130.99))
    val previousResultData = Payment(LocalDate.parse("2014-04-03"), Some(0), Some(130.99), Some(155.02), Some(100.02), None, None, None, None, None, None, Some(LocalDate.parse("2012-06-22")), Some(130.99))
    val currentTaxUrl = s"/rti/individual/payments/nino/${ninoIdentifier.withoutSuffix}/tax-year/${currentTaxYear.yearForUrl}"
    val previousTaxUrl = s"/rti/individual/payments/nino/${ninoIdentifier.withoutSuffix}/tax-year/${currentTaxYear.previous.yearForUrl}"

    "successfully obtain data for a valid NINO" when {
      "both tax years exist with data" in {
        stubGetWithResponseBody(currentTaxUrl, OK, currentResponseBody)
        stubGetWithResponseBody(previousTaxUrl, OK, previousResponseBody)
        val result: Seq[Payment] = await(connector.getRecords(selectionNino))

        result shouldBe List(currentResultData, previousResultData)
      }

      "only current tax year exists with data" in {
        stubGetWithResponseBody(currentTaxUrl, OK, currentResponseBody)
        stubGetWithResponseBody(previousTaxUrl, NOT_FOUND, emptyReponseBody)
        val result: Seq[Payment] = await(connector.getRecords(selectionNino))

        result shouldBe List(currentResultData)
      }

      "only previous tax year exists with data" in {
        stubGetWithResponseBody(currentTaxUrl, NOT_FOUND, emptyReponseBody)
        stubGetWithResponseBody(previousTaxUrl, OK, previousResponseBody)
        val result: Seq[Payment] = await(connector.getRecords(selectionNino))

        result shouldBe List(previousResultData)
      }
    }

    "return empty Seq() AND warnLogs if no NINO is given" in {
      withCaptureOfLoggingFrom[P45Connector] { logs =>
        stubGetWithResponseBody(currentTaxUrl, OK, currentResponseBody)
        stubGetWithResponseBody(previousTaxUrl, OK, previousResponseBody)
        val payeRef: EmpRef = EmpRef("fakeTaxOfficeNumber", "fakeTaxOfficeReference")
        val selectionPayeRef: Selection = Selection(payeRef)
        val result: Seq[Payment] = await(connector.getRecords(selectionPayeRef))

        result shouldBe Seq.empty

        val warnLogs = logs.filter(_.getLevel == Level.WARN)
        warnLogs.size shouldBe 1
        warnLogs.head.getMessage should include (s"p45Service, No nino identifier for selection: $selectionPayeRef")
      }
    }

    "return UpstreamErrorResponse and empty Seq() when P45 data not found for both tax years" in {
      withCaptureOfLoggingFrom[P45Connector] { logs =>
        stubGetWithResponseBody(currentTaxUrl, NOT_FOUND, currentResponseBody)
        stubGetWithResponseBody(previousTaxUrl, NOT_FOUND, previousResponseBody)
        val result: Seq[Payment] = await(connector.getRecords(selectionNino))
        result shouldBe Seq.empty

        val infoLogs = logs.filter(_.getLevel == Level.INFO)
        infoLogs.size shouldBe 2
        infoLogs.head.getMessage should include ("p45Service is not available for user:")
      }
    }
  }
}
