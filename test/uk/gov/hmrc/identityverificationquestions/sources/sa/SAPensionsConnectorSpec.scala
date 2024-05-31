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
import play.api.Configuration
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, NotFoundException}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.services.utilities.TaxYear
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SAPensionsConnectorSpec extends UnitSpec {
  "sa pensions connector" should {
    "return an existing self assessment pension returns" in new Setup {
      val expectedResult = Seq(SAReturn(TaxYear(testYear), Seq(SARecord(BigDecimal(100.00), BigDecimal(50.00)))))

      val expectedUrl = s"$mockedBaseUrl/individuals/nino/$testNino/self-assessment/income?startYear=$testYear&endYear=$testYear"

      (mockHttpClient.GET[Seq[SAReturn]](_: String, _: Seq[(String, String)], _: Seq[(String, String)])
        (_: HttpReads[Seq[SAReturn]], _: HeaderCarrier, _: ExecutionContext))
        .expects(expectedUrl, *, *, *, *, *)
        .returning(Future.successful(expectedResult))

      connector.getReturns(testNino, testYear, testYear).futureValue shouldBe expectedResult
    }

    "return an empty list of pension returns if 404 is returned" in new Setup {
      val expectedUrl = s"$mockedBaseUrl/individuals/nino/$testNino/self-assessment/income?startYear=$testYear&endYear=$testYear"

      (mockHttpClient.GET[Seq[SAReturn]](_: String, _: Seq[(String, String)], _: Seq[(String, String)])
        (_: HttpReads[Seq[SAReturn]], _: HeaderCarrier, _: ExecutionContext))
        .expects(expectedUrl, *, *, *, *, *)
        .returning(Future.failed(new NotFoundException("intentional failure")))

      connector.getReturns(testNino, testYear, testYear).futureValue shouldBe Seq()
    }

    "pass through an exception that isn't Not Found" in new Setup {
      val expectedUrl = s"$mockedBaseUrl/individuals/nino/$testNino/self-assessment/income?startYear=$testYear&endYear=$testYear"

      val errorMessage = "intentional failure"

      (mockHttpClient.GET[Seq[SAReturn]](_: String, _: Seq[(String, String)], _: Seq[(String, String)])
        (_: HttpReads[Seq[SAReturn]], _: HeaderCarrier, _: ExecutionContext))
        .expects(expectedUrl, *, *, *, *, *)
        .returning(Future.failed(new RuntimeException(errorMessage)))

      val ex: RuntimeException = intercept[RuntimeException] {
        connector.getReturns(testNino, testYear, testYear).futureValue
      }

      ex.getCause.getMessage shouldBe errorMessage
    }

    "if we are before the switch over date then take the start year as 3 calendar years ago and the end year as 2 calendar years ago" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.switch.day" -> 1,
        "sa.switch.month" -> 8
      )
      connector.determinePeriod shouldBe ((2017, 2018))
    }

    "if we are after the switch over date then take the start year as 2 calendar years ago and the end year as last calendar year" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.switch.day" -> 1,
        "sa.switch.month" -> 3
      )
      connector.determinePeriod shouldBe ((2018, 2019))
    }

    "if we are on the switch over date then take the start year as 2 calendar years ago and the end year as last calendar year" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.switch.day" -> 1,
        "sa.switch.month" -> 6
      )
      connector.determinePeriod shouldBe ((2018, 2019))
    }
  }

  trait Setup {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockedBaseUrl  = "https://sa-adapter:443"
    val mockHttpClient: HttpClient = mock[HttpClient]
    val metricsService: MetricsService = app.injector.instanceOf[MetricsService]

    lazy val additionalConfig: Map[String, Any] = Map.empty
    private lazy val configData: Map[String, Any] = Map(
      "microservice.services.self-assessment.protocol" -> "https",
      "microservice.services.self-assessment.host" -> "sa-adapter",
      "microservice.services.self-assessment.port" -> "443"
    ) ++ additionalConfig
    val config: Configuration = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)
    implicit val appConfig : AppConfig = new AppConfig(config, servicesConfig)

    val testNino: Nino = Nino("AA000003D")
    val testYear = 2019

    val fixedDate: Instant = Instant.parse("2020-06-01T00:00:00Z")

    val connector: SAPensionsConnector = new SAPensionsConnector(mockHttpClient, servicesConfig, appConfig, metricsService) {
      override def currentDate: Instant = fixedDate
    }
  }
}
