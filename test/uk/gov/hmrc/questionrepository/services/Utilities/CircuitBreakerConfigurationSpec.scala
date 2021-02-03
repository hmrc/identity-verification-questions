/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.Utilities

import Utils.UnitSpec
import play.api.Configuration
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.services.utilities.CircuitBreakerConfiguration

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
  }

  trait Setup extends TestData {

    def testConfig: Map[String, Any] = Map.empty
    val config: Configuration = Configuration.from(testConfig)
    lazy val servicesConfig = new ServicesConfig(config)
    lazy val mockAppConfig = new AppConfig(config, servicesConfig)

    val circuitBreakerConfiguration = new CircuitBreakerConfiguration {
      override def appConfig: AppConfig = mockAppConfig
      override def serviceName = "test"
    }

  }

  trait TestData {
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

    val defaultCircuitBreakerConfig = CircuitBreakerConfig("test", 20 , 60 * 1000, 300 * 1000)
    val configuredCircuitBreakerConfig = CircuitBreakerConfig("test", 30, 70 * 1000, 310 * 1000)
  }
}
