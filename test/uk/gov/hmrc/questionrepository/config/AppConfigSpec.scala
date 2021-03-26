/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.config

import Utils.testData.AppConfigTestData
import ch.qos.logback.classic.Level
import Utils.{LogCapturing, UnitSpec}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.models.p60Service
import uk.gov.hmrc.questionrepository.models.passport.PassportAuthData

import java.time.{LocalDateTime, Period}
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
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(Some(testOutage), testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage between 2020-08-08T21:00 and 2020-08-08T23:00") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has no outage set" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage not specified") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has endDate earlier than startDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T23:00:00.000", "2020-08-08T21:00:00.000") ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage startDate: 2020-08-08T23:00 must be earlier than endDate: 2020-08-08T21:00") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has invalid startDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("Not A Date", "2020-08-08T21:00:00.000") ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage Invalid date in `microservice.services.p60Service.disabled.start` : `Not A Date`") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has invalid endDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "Not A Date") ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage Invalid date in `microservice.services.p60Service.disabled.end` : `Not A Date`") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has no startDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = endDate ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage p60Service.disabled.start missing") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with no outage for service with config that has no endDate" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = startDate ++ disabledOrigins ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(None, testDisabled, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage p60Service.disabled.end missing") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with empty List for disabled origins for service with config missing setting" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "2020-08-08T23:00:00.000") ++ enabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(Some(testOutage), List.empty, testEnabled, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage between 2020-08-08T21:00 and 2020-08-08T23:00") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service not specified") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with empty List for enabled origins for service with config missing setting" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "2020-08-08T23:00:00.000") ++ disabledOrigins ++ requiredIdentifiers

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(Some(testOutage), testDisabled, List.empty, testIdentifiers)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage between 2020-08-08T21:00 and 2020-08-08T23:00") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service not specified") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service are [nino, utr]") shouldBe 1
        }
      }

      "return ServiceStatus with empty List for required identifiers for service with config missing setting" in new Setup {
        lazy val testServiceStatus: Map[String, Any] = outageConfig("2020-08-08T21:00:00.000", "2020-08-08T23:00:00.000") ++ enabledOrigins ++ disabledOrigins

        override def testConfig: Map[String, Any] = baseConfig ++ testServiceStatus

        withCaptureOfLoggingFrom[AppConfig] { logs =>
          appConfig.serviceStatus(p60Service) shouldBe appConfig.ServiceState(Some(testOutage), testDisabled, testEnabled, List.empty)

          val infoLogs = logs.filter(_.getLevel == Level.INFO)
          infoLogs.size shouldBe 4
          infoLogs.count(_.getMessage == "Scheduled p60Service outage between 2020-08-08T21:00 and 2020-08-08T23:00") shouldBe 1
          infoLogs.count(_.getMessage == "Disabled origins for p60Service are [seiss, ddt]") shouldBe 1
          infoLogs.count(_.getMessage == "Enabled origins for p60Service are [dwp-iv]") shouldBe 1
          infoLogs.count(_.getMessage == "Required identifiers for p60Service not specified") shouldBe 1
        }
      }
    }

    "getting hodConfiguration" should {
      "return HodConf for serviceName" when {
        "all hod config values are present for service" in new Setup {
          override def testConfig: Map[String, Any] = baseConfig ++ hodAuthorizationToken ++ hodEnvironmentHeader

          appConfig.hodConfiguration(p60Service) shouldBe Right(HodConf("authToken", "envHeader"))
        }
      }

      "return MissingAuthorizationToken for serviceName" when {
        "hod config AuthorizationToken value is missing for service" in new Setup {
          override def testConfig: Map[String, Any] = baseConfig ++ hodEnvironmentHeader

          appConfig.hodConfiguration(p60Service) shouldBe Left(MissingAuthorizationToken)
        }
      }

      "return MissingEnvironmentHeader for serviceName" when {
        "hod config EnvironmentHeader value is missing for service" in new Setup {
          override def testConfig: Map[String, Any] = baseConfig ++ hodAuthorizationToken

          appConfig.hodConfiguration(p60Service) shouldBe Left(MissingEnvironmentHeader)
        }
      }

      "return MissingAllConfig for serviceName" when {
        "all hod config is missing for service" in new Setup {
          override def testConfig: Map[String, Any] = baseConfig

          appConfig.hodConfiguration(p60Service) shouldBe Left(MissingAllConfig)
        }
      }
    }

    "return a baseUrl for specified service" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig ++ serviceBaseUrl

      appConfig.serviceBaseUrl(p60Service) shouldBe "http://localhost:8080"
    }

    "throw an Exception if baseUrl not in config for service" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig
      an[RuntimeException] shouldBe thrownBy {
        appConfig.serviceBaseUrl(p60Service)
      }
    }

    "return the bufferInMonths for requested service" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig ++ bufferInMonthsForService

      appConfig.bufferInMonthsForService(p60Service) shouldBe 2
    }

    "throw an Exception if bufferInMonths not in config for service" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig
      an[RuntimeException] shouldBe thrownBy {
        appConfig.bufferInMonthsForService(p60Service)
      }
    }

    "get 'questionRecordTT'" should {
      "return the value in application.conf if present" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig ++ questionRepoTtlPeriod
        appConfig.questionRecordTTL shouldBe Period.parse("P2D")
      }

      "return the default value of 'P1D' if not set in application.conf" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig
        appConfig.questionRecordTTL shouldBe Period.parse("P1D")
      }
    }

    "getting passportAuthData" should {
      "return a populated PassportAuthData object" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig ++ passportAuthDataData
        appConfig.passportAuthData shouldBe PassportAuthData("THMRC", "THMRC001", "THMRC_WS", "passport-pwd")
      }

      "throw an Exception if any/all values are missing from conf" in new Setup {
        override def testConfig: Map[String, Any] = baseConfig
        an[RuntimeException] shouldBe thrownBy {
          appConfig.passportAuthData
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

  trait TestData extends AppConfigTestData {

    val numberOfCallsToTrigger: Map[String, Any] = Map("circuit.breaker.numberOfCallsToTrigger" -> 30)
    val unavailablePeriodDurationInSec: Map[String, Any] = Map("circuit.breaker.unavailablePeriodDurationInSec" -> 70)
    val unstablePeriodDurationInSec: Map[String, Any] = Map("circuit.breaker.unstablePeriodDurationInSec" -> 310)

    def outageConfig(startDate: String, endDate: String): Map[String, Any] = Map(
      "microservice.services.p60Service.disabled.start" -> startDate,
      "microservice.services.p60Service.disabled.end" -> endDate
    )

    val startDate: Map[String, Any] = Map("microservice.services.p60Service.disabled.start" -> "2020-08-08T21:00:00.000")
    val endDate: Map[String, Any] = Map("microservice.services.p60Service.disabled.end" -> "2020-08-08T23:00:00.000")

    val disabledOrigins: Map[String, Any] = Map(
      "microservice.services.p60Service.disabled.origin.0" -> "seiss",
      "microservice.services.p60Service.disabled.origin.1" -> "ddt"
    )

    val enabledOrigins: Map[String, Any] = Map(
      "microservice.services.p60Service.enabled.origin.0" -> "dwp-iv"
    )

    val requiredIdentifiers: Map[String, Any] = Map(
      "microservice.services.p60Service.identifier.required.0" -> "nino",
      "microservice.services.p60Service.identifier.required.1" -> "utr"
    )

    val testStartTime: LocalDateTime = LocalDateTime.parse("2020-08-08T21:00:00.000", ISO_LOCAL_DATE_TIME)
    val testEndTime: LocalDateTime = LocalDateTime.parse("2020-08-08T23:00:00.000", ISO_LOCAL_DATE_TIME)
    val testOutage: Outage = Outage(testStartTime, testEndTime)

    val testDisabled = List("seiss", "ddt")
    val testEnabled = List("dwp-iv")
    val testIdentifiers = List ("nino", "utr")

    val hodAuthorizationToken: Map[String, Any] = Map("microservice.services.p60Service.hod.authorizationToken" -> "authToken")
    val hodEnvironmentHeader: Map[String, Any] = Map("microservice.services.p60Service.hod.environmentHeader" -> "envHeader")

    val serviceBaseUrl: Map[String, Any] = Map(
      "microservice.services.p60Service.protocol" -> "http",
      "microservice.services.p60Service.host" -> "localhost",
      "microservice.services.p60Service.port" -> 8080
    )

    val bufferInMonthsForService: Map[String, Any] = Map("microservice.services.p60Service.bufferInMonths" -> 2)

    val questionRepoTtlPeriod: Map[String, Any] = Map("question.record.duration" -> "P2D")

    val passportAuthDataData: Map[String, Any] = Map(
      "microservice.services.passportService.authenticationData.organisationId" -> "THMRC",
      "microservice.services.passportService.authenticationData.organisationApplicationId" -> "THMRC001",
      "microservice.services.passportService.authenticationData.organisationUserName" -> "THMRC_WS",
      "microservice.services.passportService.authenticationData.organisationUserPassword" -> "passport-pwd"
    )
  }
}
