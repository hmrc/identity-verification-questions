/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.config

import Utils.UnitSpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigSpec extends UnitSpec {

  "AppConfig" should {

    "return the url for authBaseUrl" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig

      appConfig.authBaseUrl shouldBe "http://localhost:1111"
    }

    "return boolean for auditingEnabled" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig
      appConfig.auditingEnabled shouldBe true
    }

    "return value for graphiteHost" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig
      appConfig.graphiteHost shouldBe "graphite"
    }
  }

  trait Setup extends TestData{
    def testConfig: Map[String, Any] = Map.empty
    val config = Configuration.from(testConfig)
    lazy val servicesConfig = new ServicesConfig(config)
    lazy val appConfig = new AppConfig(config, servicesConfig)
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

    val auth: Map[String, Any] = Map (
      "microservice.services.auth.host" -> "localhost",
      "microservice.services.auth.port" -> "1111"
    )

    val baseConfig: Map[String, Any] = metrics ++ auditing ++ auth
  }
}
