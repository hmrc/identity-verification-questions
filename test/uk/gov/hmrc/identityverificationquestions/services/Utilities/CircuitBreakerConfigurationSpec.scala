/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.services.Utilities

import Utils.UnitSpec
import play.api.Configuration
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.{ServiceName, p60Service}
import uk.gov.hmrc.identityverificationquestions.services.utilities.CircuitBreakerConfiguration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class CircuitBreakerConfigurationSpec extends UnitSpec {

  "creating a circuitBreakerConfig" should {
    "retrieve default values from appConfig if none are set" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig

      circuitBreakerConfiguration.circuitBreakerConfig shouldBe defaultCircuitBreakerConfig
    }

    "retrieve configured values from config" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig ++ numberOfCallsToTrigger ++ unavailablePeriodDurationInSec ++ unstablePeriodDurationInSec

      circuitBreakerConfiguration.circuitBreakerConfig shouldBe configuredCircuitBreakerConfig
    }

    "retrieve service specific configured values from config" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig ++
        numberOfCallsToTrigger ++ unavailablePeriodDurationInSec ++ unstablePeriodDurationInSec ++
        serviceNumberOfCallsToTrigger ++ serviceUnavailablePeriodDurationInSec ++ serviceUnstablePeriodDurationInSec

      circuitBreakerConfiguration.circuitBreakerConfig shouldBe servceiSpecificCircuitBreakerConfig
    }
  }

  trait Setup extends TestData {

    def testConfig: Map[String, Any] = Map.empty
    val config: Configuration = Configuration.from(testConfig)
    lazy val servicesConfig = new ServicesConfig(config)
    lazy val mockAppConfig = new AppConfig(config, servicesConfig)

    val circuitBreakerConfiguration = new CircuitBreakerConfiguration {
      override def appConfig: AppConfig = mockAppConfig
      override def serviceName: ServiceName = testServiceName
    }

  }

  trait TestData {
    val testServiceName: ServiceName = p60Service
    val metrics: Map[String, Any] = Map(
      "microservice.metrics.graphite.host" -> "graphite",
      "microservice.metrics.graphite.port" -> "2003",
      "microservice.metrics.graphite.prefix" -> "play.${appName}.",
      "microservice.metrics.graphite.enabled" -> "false"
    )

    val auditing: Map[String, Any] = Map(
      "auditing.enabled" -> "true",
      "auditing.traceRequests" -> "true",
      "auditing.consumer.baseUri.host" -> "localhost",
      "auditing.consumer.baseUri.port" -> "8100"
    )

    val auth: Map[String, Any] = Map(
      "microservice.services.auth.host" -> "localhost",
      "microservice.services.auth.port" -> "1111"
    )

    val baseConfig: Map[String, Any] = metrics ++ auditing ++ auth

    val numberOfCallsToTrigger: Map[String, Any] = Map("circuit.breaker.numberOfCallsToTrigger" -> 30)
    val unavailablePeriodDurationInSec: Map[String, Any] = Map("circuit.breaker.unavailablePeriodDurationInSec" -> 70)
    val unstablePeriodDurationInSec: Map[String, Any] = Map("circuit.breaker.unstablePeriodDurationInSec" -> 310)

    val serviceNumberOfCallsToTrigger: Map[String, Any] = Map(s"microservice.services.${testServiceName.toString}.circuitBreaker.numberOfCallsToTrigger" -> 40)
    val serviceUnavailablePeriodDurationInSec: Map[String, Any] = Map(s"microservice.services.${testServiceName.toString}.circuitBreaker.unavailableDurationInSec" -> 80)
    val serviceUnstablePeriodDurationInSec: Map[String, Any] = Map(s"microservice.services.${testServiceName.toString}.circuitBreaker.unstableDurationInSec" -> 320)


    val defaultCircuitBreakerConfig = CircuitBreakerConfig("p60Service", 20 , 60 * 1000, 300 * 1000)
    val configuredCircuitBreakerConfig = CircuitBreakerConfig("p60Service", 30, 70 * 1000, 310 * 1000)
    val servceiSpecificCircuitBreakerConfig = CircuitBreakerConfig("p60Service", 40, 80 * 1000, 320 * 1000)
  }
}
