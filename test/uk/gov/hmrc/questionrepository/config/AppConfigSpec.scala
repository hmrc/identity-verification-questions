/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.config

import ch.qos.logback.classic.Level
import Utils.{LogCapturing, UnitSpec}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

class AppConfigSpec extends UnitSpec with LogCapturing {

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

    "circuit breaker config" should {
      "return 20 for circuitBreakerNumberOfCallsToTrigger if no config value set" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig
        appConfig.circuitBreakerNumberOfCallsToTrigger shouldBe 20
      }

      "return 30 for circuitBreakerNumberOfCallsToTrigger if no config value set to 30" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig ++ numberOfCallsToTrigger
        appConfig.circuitBreakerNumberOfCallsToTrigger shouldBe 30
      }

      "return 60 for circuitBreakerUnavailableDurationInSec if no config value set" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig
        appConfig.circuitBreakerUnavailableDurationInSec shouldBe 60
      }

      "return 70 for circuitBreakerUnavailableDurationInSec if no config value set to 70" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig ++ unavailablePeriodDurationInSec
        appConfig.circuitBreakerUnavailableDurationInSec shouldBe 70
      }

      "return 300 for circuitBreakerUnstableDurationInSec if no config value set" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig
        appConfig.circuitBreakerUnstableDurationInSec shouldBe 300
      }

      "return 310 for circuitBreakerUnstableDurationInSec if no config value set to 310" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig ++ unstablePeriodDurationInSec
        appConfig.circuitBreakerUnstableDurationInSec shouldBe 310
      }
    }

    "getting ServiceStatus" should {
      "return ServiceStatus with outage for a service with full valid config" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "2020-08-08T23:00:00.000") ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(Some(testOutage), testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage between 2020-08-08T21:00 and 2020-08-08T23:00").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has no outage set" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage not specified").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has endDate earlier than startDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T23:00:00.000", "2020-08-08T21:00:00.000") ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage startDate: 2020-08-08T23:00 must be earlier than endDate: 2020-08-08T21:00").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has invalid startDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("Not A Date", "2020-08-08T21:00:00.000") ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage Invalid date in `microservice.services.test.disabled.start` : `Not A Date`").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has invalid endDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "Not A Date") ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage Invalid date in `microservice.services.test.disabled.end` : `Not A Date`").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has no startDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = endDate ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage test.disabled.start missing").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has no endDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = startDate ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage test.disabled.end missing").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with empty List for disabled origins for service with config missing setting" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "2020-08-08T23:00:00.000") ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(Some(testOutage), List.empty, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage between 2020-08-08T21:00 and 2020-08-08T23:00").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test not specified").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with empty List for enabled origins for service with config missing setting" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "2020-08-08T23:00:00.000") ++ disabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(Some(testOutage), testDisabled, List.empty, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage between 2020-08-08T21:00 and 2020-08-08T23:00").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test not specified").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test are [nino, utr]").size shouldBe 1
        }
      }

      "return ServiceStatus with empty List for required identifiers for service with config missing setting" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "2020-08-08T23:00:00.000") ++ enabledOrigins ++ disabledOrigins

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus("test") shouldBe appConfig.ServiceState(Some(testOutage), testDisabled, testEnabled, List.empty)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.filter(_.getMessage == "Scheduled test outage between 2020-08-08T21:00 and 2020-08-08T23:00").size shouldBe 1
          infoLogs.filter(_.getMessage == "Disabled origins for test are [seiss, ddt]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Enabled origins for test are [dwp-iv]").size shouldBe 1
          infoLogs.filter(_.getMessage == "Required identifiers for test not specified").size shouldBe 1
        }
      }
    }
  }

  trait Setup extends TestData {
    def testConfig: Map[String, Any] = Map.empty
    val config: Configuration = Configuration.from(testConfig)
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

    val auth: Map[String, Any] = Map(
      "microservice.services.auth.host" -> "localhost",
      "microservice.services.auth.port" -> "1111"
    )

    val baseConfig: Map[String, Any] = metrics ++ auditing ++ auth

    val numberOfCallsToTrigger: Map[String, Any] = Map("circuit.breaker.numberOfCallsToTrigger" -> 30)
    val unavailablePeriodDurationInSec: Map[String, Any] = Map("circuit.breaker.unavailablePeriodDurationInSec" -> 70)
    val unstablePeriodDurationInSec: Map[String, Any] = Map("circuit.breaker.unstablePeriodDurationInSec" -> 310)

    def outageConfig(startDate: String, endDate: String): Map[String, Any] = Map(
      "microservice.services.test.disabled.start" -> startDate,
      "microservice.services.test.disabled.end" -> endDate
    )

    val startDate: Map[String, Any] = Map("microservice.services.test.disabled.start" -> "2020-08-08T21:00:00.000")
    val endDate: Map[String, Any] = Map("microservice.services.test.disabled.end" -> "2020-08-08T23:00:00.000")

    val disabledOrigins: Map[String, Any] = Map(
      "microservice.services.test.disabled.origin.0" -> "seiss",
      "microservice.services.test.disabled.origin.1" -> "ddt"
    )

    val enabledOrigins: Map[String, Any] = Map(
      "microservice.services.test.enabled.origin.0" -> "dwp-iv"
    )

    val requiredIdentifiers: Map[String, Any] = Map(
      "microservice.services.test.identifier.required.0" -> "nino",
      "microservice.services.test.identifier.required.1" -> "utr"
    )

    val testStartTime = LocalDateTime.parse("2020-08-08T21:00:00.000", ISO_LOCAL_DATE_TIME)
    val testEndTime = LocalDateTime.parse("2020-08-08T23:00:00.000", ISO_LOCAL_DATE_TIME)
    val testOutage = Outage(testStartTime, testEndTime)

    val testDisabled = List("seiss", "ddt")
    val testEnabled = List("dwp-iv")
    val testIdentifiers = List ("nino", "utr")

  }
}
