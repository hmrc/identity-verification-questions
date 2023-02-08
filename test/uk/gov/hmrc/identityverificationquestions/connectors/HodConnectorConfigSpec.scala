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

package uk.gov.hmrc.identityverificationquestions.connectors

import Utils.UnitSpec
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.identityverificationquestions.config.{AppConfig, HodConf, MissingAllConfig, MissingAuthorizationToken, MissingEnvironmentHeader}
import uk.gov.hmrc.identityverificationquestions.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.identityverificationquestions.models.{ServiceName, p60Service}

class HodConnectorConfigSpec extends UnitSpec {

  "headersForDES" should {
    "return headerCarrier with DES headers" when {
      "valid hodConfig returned from AppConfig for service" in new Setup {
        (mockAppConfig.hodConfiguration(_: ServiceName)).expects(p60Service).returning(Right(HodConf("authToken", "envHeader")))

        testHodConfig.publicHeadersForDES shouldBe hcForDES
      }
    }

    "throw error" when {
      "MissingAuthorizationToken returned" in new Setup {
        (mockAppConfig.hodConfiguration(_: ServiceName)).expects(p60Service).returning(Left(MissingAuthorizationToken))

        an[RuntimeException] shouldBe thrownBy {
          testHodConfig.publicHeadersForDES
        }
      }

      "MissingEnvironmentHeader returned" in new Setup {
        (mockAppConfig.hodConfiguration(_: ServiceName)).expects(p60Service).returning(Left(MissingEnvironmentHeader))
        an[RuntimeException] shouldBe thrownBy {
          testHodConfig.publicHeadersForDES
        }
      }

      "MissingAllConfig returned" in new Setup {
        (mockAppConfig.hodConfiguration(_: ServiceName)).expects(p60Service).returning(Left(MissingAllConfig))
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
