/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.{LogCapturing, UnitSpec}
import ch.qos.logback.classic.Level
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.questionrepository.config.{AppConfig, Outage}
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.Identifier.{Identifier, NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.{AnswerCheck, AnswerDetails, Correct, DoubleAnswer, EmployeeNIContributions, Origin, PaymentToDate, QuestionKey, QuestionResult, Score, ServiceName, Unknown, p60Service}

import java.time.LocalDateTime
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AnswerServiceSpec extends UnitSpec with LogCapturing {

  "check isAvailable" should {
    "return true" when {
      "no outage is defined, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the past, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(Some(pastOutage), List.empty, List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the future, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(Some(futureOutage), List.empty, List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "no outage is defined, disabledOrigins is defined but does not contain origin, enabledOrigins is empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List("abcd", "xyz"), List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "no outage is defined, disabledOrigins is defined but does not contain origin," + "" +
        "enabledOrigins IS defined AND contains the origin and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List("abcd", "xyz"), List("alala"), List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "no outage is defined, disabledOrigins is defined DOES contain origin BUT," + "" +
        "enabledOrigins IS defined AND contains the origin and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List("alala", "xyz"), List("alala"), List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }
    }

    "return false" when {
      "outage defined and covers the period now" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(Some(currentOutage), List.empty, List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe false
      }

      "no outage is defined, disabledOrigins is defined DOES contain origin, enabledOrigins is empty and required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List("alala", "xyz"), List.empty, List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe false
      }

      "no outage is defined, disabledOrigins is empty, enabledOrigins is defined but does NOT contain origin, required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List("abc", "xyz"), List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier, saUtrIdentifier)) shouldBe false
      }

      "no outage is defined, disabledOrigins is empty, enabledOrigins is empty BUT NOT all required identifiers are present" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List("abc", "xyz"), List("nino", "utr")))

        service.isAvailable(origin, Seq(ninoIdentifier)) shouldBe false
      }
    }

    "checkAnswers" should {
      "return list of supported questions with score of 'unknown'" when {
        "connector throws error" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

          override def connectorResult: Future[TestRecord] = badRequestResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(origin, Seq(ninoIdentifier, saUtrIdentifier), Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer))).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe "p60Service, threw exception uk.gov.hmrc.http.Upstream4xxResponse: bad bad bad request, origin: alala, identifiers: AA000000D,12345678"
          }
        }

        "connector returns not found" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

          override def connectorResult: Future[TestRecord] = notFoundResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(origin, Seq(ninoIdentifier, saUtrIdentifier), Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer))).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
            val errorLogs = logs.filter(_.getLevel == Level.INFO)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe "p60Service, no answers returned for selection, origin: alala, identifiers: AA000000D,12345678"
          }
        }
      }

      "return list of supported questions with score of 'unknown' if service is unavailable" when {
        "outage set and covers current time" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(Some(currentOutage), List.empty, List.empty, List("nino", "utr")))

          override def connectorResult: Future[TestRecord] = testRecordResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(origin, Seq(ninoIdentifier, saUtrIdentifier), Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer))).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
            logs.size shouldBe 0
          }
        }

        "no outage set but origin in disabled origin list time" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List("alala"), List.empty, List("nino", "utr")))

          override def connectorResult: Future[TestRecord] = testRecordResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(origin, Seq(ninoIdentifier, saUtrIdentifier), Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer))).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
            logs.size shouldBe 0
          }
        }

        "no outage set but origin NOT in enabled origin list time" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List("another"), List("nino", "utr")))

          override def connectorResult: Future[TestRecord] = testRecordResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(origin, Seq(ninoIdentifier, saUtrIdentifier), Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer))).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
            logs.size shouldBe 0
          }
        }

        "no outage set not all required Identifiers are present" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

          override def connectorResult: Future[TestRecord] = testRecordResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(origin, Seq(saUtrIdentifier), Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer))).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
            logs.size shouldBe 0
          }
        }
      }

      "return list of questions if service available" when {
        "connector successful" in new Setup {
          when(mockAppConfig.serviceStatus(eqTo[ServiceName](p60Service))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino", "utr")))

          override def connectorResult: Future[TestRecord] = testRecordResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(origin, Seq(ninoIdentifier, saUtrIdentifier), Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer))).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Correct))
            logs.size shouldBe 0
          }
        }
      }
    }
  }

  trait Setup extends TestData {
    self =>

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val mockAppConfig: AppConfig = mock[AppConfig]

    def connectorResult: Future[TestRecord] = illegalAccessResult

    def connector: AnswerConnector[TestRecord] = new AnswerConnector[TestRecord] {
      def verifyAnswer(origin: Origin, identifiers: Seq[Identifier], answer: AnswerDetails): Future[TestRecord] = connectorResult
    }

    import uk.gov.hmrc.questionrepository.services.utilities.CheckAvailability

    abstract class TestService extends AnswerService with CheckAvailability

    lazy val service: TestService {
      type Record = TestRecord
    } = new TestService {
      override type Record = TestRecord
      override val serviceName = p60Service

      override def connector: AnswerConnector[TestRecord] = self.connector

      override def supportedQuestions: Seq[QuestionKey] = Seq(PaymentToDate)

      override def answerTransformer(records: Seq[TestRecord], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] =
        records.map(r => QuestionResult(r.questionKey, r.answer))

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("p60Service", 2, 1000, 1000)
//
//      override protected def breakOnException(t: Throwable): Boolean = ???

    }
  }

  trait TestData {
    val origin: Origin = Origin("alala")
    val ninoIdentifier: NinoI = NinoI("AA000000D")
    val saUtrIdentifier: SaUtrI = SaUtrI("12345678")
    val futureOutage: Outage = Outage(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
    val pastOutage: Outage = Outage(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1))
    val currentOutage: Outage = Outage(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))

    val paymentToDateAnswer = AnswerDetails(PaymentToDate, DoubleAnswer(100.11))
    val EmployeeNIContributionsAnswer = AnswerDetails(EmployeeNIContributions, DoubleAnswer(200.22))

    case class TestRecord(questionKey: QuestionKey, answer: Score)

    def illegalAccessResult: Future[TestRecord] = Future.failed(new IllegalAccessException("Connector should not have been called"))
    def testRecordResult: Future[TestRecord] = Future.successful(TestRecord(PaymentToDate, Correct))
    def notFoundResult: Future[TestRecord] = Future.failed(UpstreamErrorResponse("no no nooooo, no records found", NOT_FOUND))
    def badRequestResult: Future[TestRecord] = Future.failed(UpstreamErrorResponse("bad bad bad request", BAD_REQUEST))
  }
}
