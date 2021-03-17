/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.connectors.utilities

import Utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.questionrepository.config.{AppConfig, HodConf, MissingAllConfig, MissingAuthorizationToken, MissingEnvironmentHeader}
import uk.gov.hmrc.questionrepository.models.{ServiceName, p60Service}

class HodConnectorConfigSpec extends UnitSpec {

  "headersForDES" should {
    "return headerCarrier with DES headers" when {
      "valid hodConfig returned from AppConfig for service" in new Setup {
        when(mockAppConfig.hodConfiguration(eqTo[ServiceName](p60Service))).thenReturn(Right(HodConf("authToken", "envHeader")))

        testHodConfig.publicHeadersForDES shouldBe hcForDES
      }
    }

    "throw error" when {
      "MissingAuthorizationToken returned" in new Setup {
        when(mockAppConfig.hodConfiguration(eqTo[ServiceName](p60Service))).thenReturn(Left(MissingAuthorizationToken))
        an[RuntimeException] shouldBe thrownBy {
          testHodConfig.publicHeadersForDES
        }
      }

      "MissingEnvironmentHeader returned" in new Setup {
        when(mockAppConfig.hodConfiguration(eqTo[ServiceName](p60Service))).thenReturn(Left(MissingEnvironmentHeader))
        an[RuntimeException] shouldBe thrownBy {
          testHodConfig.publicHeadersForDES
        }
      }

      "MissingAllConfig returned" in new Setup {
        when(mockAppConfig.hodConfiguration(eqTo[ServiceName](p60Service))).thenReturn(Left(MissingAllConfig))
        an[RuntimeException] shouldBe thrownBy {
          testHodConfig.publicHeadersForDES
        }
      }
    }
  }

  trait Setup extends TestData{

    implicit val mockAppConfig: AppConfig = mock[AppConfig]

    val testHodConfig = new HodConnectorConfig {
      override implicit val appConfig: AppConfig = mockAppConfig

      override def serviceName: ServiceName = p60Service

      def publicHeadersForDES: HeaderCarrier = headersForDES
    }
  }

  trait TestData {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val hcForDES: HeaderCarrier = hc.copy(authorization = Some(Authorization(s"Bearer authToken")), extraHeaders = Seq("Environment" -> "envHeader"))
  }
}
