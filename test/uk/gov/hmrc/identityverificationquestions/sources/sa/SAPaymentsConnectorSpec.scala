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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import Utils.UnitSpec
import mocks.MockHttpClientV2
import play.api.Configuration
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, StringContextOps}
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class SAPaymentsConnectorSpec extends UnitSpec with MockHttpClientV2 {

  "sa payments connector" should {
    "return an existing self assessment payments returns" in new Setup {
      val expectedData: List[SAPayment] = List(SAPayment(BigDecimal(1550), Some(LocalDate.now()), Some("PYT")))
      val expectedResult: List[SAPaymentReturn] = List(SAPaymentReturn(expectedData))

      val expectedUrl = s"$mockedBaseUrl/individuals/self-assessment/payments/utr/$TEST_SAUTR"

      mockHttpClientV2Get(url"$expectedUrl")
      mockHttpClientV2Execute[Seq[SAPayment]](expectedData)

      connector.getReturns(SAUTR).futureValue shouldBe expectedResult
    }

    "return an existing self assessment payment returns using SaUtr" in new Setup {
      val expectedData: List[SAPayment] = List(SAPayment(BigDecimal(1550), Some(LocalDate.now()), Some("PYT")))
      val expectedResult: List[SAPaymentReturn] = List(SAPaymentReturn(expectedData))

      val expectedUrl = s"$mockedBaseUrl/individuals/self-assessment/payments/utr/$TEST_SAUTR"

      mockHttpClientV2Get(url"$expectedUrl")
      mockHttpClientV2Execute[Seq[SAPayment]](expectedData)

      connector.getReturns(SAUTR).futureValue shouldBe expectedResult
    }

    "return an empty list of payments returns if 404 is returned" in new Setup {
      val expectedUrl = s"$mockedBaseUrl/individuals/self-assessment/payments/utr/$TEST_SAUTR"

      mockHttpClientV2Get(url"$expectedUrl")
      mockHttpClientV2ExecuteException[Seq[SAPayment]](new NotFoundException("intentional failure"))

      connector.getReturns(SAUTR).futureValue shouldBe List()
    }

    "pass through an exception that isn't Not Found" in new Setup {
      val expectedUrl = s"$mockedBaseUrl/individuals/self-assessment/payments/utr/$TEST_SAUTR"

      val errorMessage = "intentional failure"

      mockHttpClientV2Get(url"$expectedUrl")
      mockHttpClientV2ExecuteException[Seq[SAPayment]](new RuntimeException(errorMessage))

      val ex: RuntimeException = intercept[RuntimeException] {
        connector.getReturns(SAUTR).futureValue
      }

      ex.getCause.getMessage shouldBe errorMessage
    }

  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockedBaseUrl = "https://sa-adapter:443"
    val metricsService: MetricsService = app.injector.instanceOf[MetricsService]
    lazy val additionalConfig: Map[String, Any] = Map.empty
    private lazy val configData: Map[String, Any] = Map(
      "microservice.services.self-assessment.protocol" -> "https",
      "microservice.services.self-assessment.host" -> "sa-adapter",
      "microservice.services.self-assessment.port" -> "443"
    ) ++ additionalConfig
    val config: Configuration = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)

    val TEST_SAUTR = "123456789"
    val SAUTR: SaUtr = SaUtr(TEST_SAUTR)

    val connector = new SAPaymentsConnector(mockHttpClientV2, servicesConfig, metricsService)
  }
}
