/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.{LogCapturing, UnitSpec}
import ch.qos.logback.classic.Level
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.questionrepository.config.{AppConfig, Outage}
import uk.gov.hmrc.questionrepository.connectors.utilities.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{Identifier, NinoI, Origin, Question, SaUtrI, Selection}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class QuestionServiceSpec extends UnitSpec with LogCapturing {

  "check isAvailable" should {
    "return true" when {
      "no outage is defined, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the past, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(Some(pastOutage), List.empty, List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the future, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(Some(futureOutage), List.empty, List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "no outage is defined, disabledOrigins is defined but does not contain origin, enabledOrigins is empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List("abcd", "xyz"), List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "no outage is defined, disabledOrigins is defined but does not contain origin," + "" +
        "enabledOrigins IS defined AND contains the origin and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List("abcd", "xyz"), List("alala"), List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "no outage is defined, disabledOrigins is defined DOES contain origin BUT," + "" +
        "enabledOrigins IS defined AND contains the origin and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List("alala", "xyz"), List("alala"), List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

    }

    "return false" when {
      "outage defined and covers the period now" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(Some(currentOutage), List.empty, List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe false
      }

      "no outage is defined, disabledOrigins is defined DOES contain origin, enabledOrigins is empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List("alala", "xyz"), List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe false
      }

      "no outage is defined, disabledOrigins is empty, enabledOrigins is defined but does NOT contain origin, required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List("abc", "xyz"), List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe false
      }

      "no outage is defined, disabledOrigins is empty, enabledOrigins is empty BUT NOT all required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List("abc", "xyz"), List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier)) shouldBe false
      }
    }

    "getQuestions" should {
      "return empty list if service is available" when {
        "connector throws error" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(origin, Seq(ninoIdentifier, saUtrIdentifier))).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe "Unexpected response from test"
          }
        }

        "connector returns not found" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))
          override def connectorResult: Future[Seq[TestRecord]] = notFoundResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(origin, Seq(ninoIdentifier, saUtrIdentifier))).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 0
          }
        }
      }

      "return empty list if service unavailable" when {
        "outage set and covers current time" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(Some(currentOutage), List.empty, List.empty, List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(origin, Seq(ninoIdentifier, saUtrIdentifier))).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 0
          }
        }

        "no outage set but origin in disabled origin list time" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List("alala"), List.empty, List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(origin, Seq(ninoIdentifier, saUtrIdentifier))).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 0
          }
        }

        "no outage set but origin NOT in enabled origin list time" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List("another"), List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(origin, Seq(ninoIdentifier, saUtrIdentifier))).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 0
          }
        }

        "no outage set not all required Identifiers are present" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(origin, Seq(saUtrIdentifier))).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 0
          }
        }
      }

      "return list of questions if service available" when {
        "connector successful" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[String]("test"))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult
          service.questions(Selection(origin, Seq(ninoIdentifier, saUtrIdentifier))).futureValue shouldBe List(Question("key",List(TestRecord(1).toString)))
        }
      }
    }
  }


  trait Setup extends TestData {
    self =>

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val mockAppConfig = mock[AppConfig]

    def connectorResult: Future[Seq[TestRecord]] = illegalAccessResult

    def connector = new QuestionConnector[TestRecord] {
      def getRecords(identifiers: Seq[Identifier])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TestRecord]] = connectorResult
    }

    import uk.gov.hmrc.questionrepository.services.utilities.CheckAvailability

    abstract class TestService extends QuestionService with CheckAvailability

    lazy val service = new TestService {
      override val serviceName = "test"
      override type Record = TestRecord

      override def connector: QuestionConnector[TestRecord] = self.connector

      override implicit val appConfig: AppConfig = mockAppConfig

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("test", 2, 1000, 1000)
    }
  }

  trait TestData {
    val origin = Origin("alala")
    val ninoIdentifier = NinoI("AA000000D")
    val saUtrIdentifier = SaUtrI("12345678")
    val futureOutage = Outage(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
    val pastOutage = Outage(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1))
    val currentOutage = Outage(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))

    case class TestRecord(value: BigDecimal)

    def illegalAccessResult: Future[Seq[TestRecord]] = Future.failed(new IllegalAccessException("Connector should not have been called"))
    def testRecordResult: Future[Seq[TestRecord]] = Future.successful(Seq(TestRecord(1)))
    def notFoundResult: Future[Seq[TestRecord]] = Future.failed(new NotFoundException("no no nooooo, no records found"))
    def badRequestResult: Future[Seq[TestRecord]] = Future.failed(new BadRequestException("bad bad bad request"))
  }
}
