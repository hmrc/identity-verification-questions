/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.sa

import Utils.UnitSpec
import play.api.Configuration
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, NotFoundException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.evidences.sources.sa.{SAPensionsConnector, SARecord, SAReturn}
import uk.gov.hmrc.questionrepository.services.utilities.TaxYear
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

      val ex = intercept[RuntimeException] {
        connector.getReturns(testNino, testYear, testYear).futureValue
      }

      ex.getCause.getMessage shouldBe errorMessage
    }
  }

  trait Setup {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockedBaseUrl  = "https://sa-adapter:443"
    val mockHttpClient = mock[HttpClient]
    lazy val additionalConfig: Map[String, Any] = Map.empty
    private lazy val configData: Map[String, Any] = Map(
      "microservice.services.self-assessment.protocol" -> "https",
      "microservice.services.self-assessment.host" -> "sa-adapter",
      "microservice.services.self-assessment.port" -> "443"
    ) ++ additionalConfig
    val config = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)

    val testNino = Nino("AA000003D")
    val testYear = 2019

    val connector = new SAPensionsConnector(mockHttpClient, servicesConfig)
  }
}
