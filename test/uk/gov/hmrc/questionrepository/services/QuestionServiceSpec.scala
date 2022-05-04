/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.{LogCapturing, UnitSpec}
import ch.qos.logback.classic.Level
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.circuitbreaker.{CircuitBreakerConfig, UnhealthyServiceException}
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.questionrepository.config.{AppConfig, Outage}
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.P60.PaymentToDate
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.monitoring.{EventDispatcher, MonitoringEvent, ServiceUnavailableEvent}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class QuestionServiceSpec extends UnitSpec with LogCapturing {

  "check isAvailable" should {
    "return true" when {
      "no outage is defined, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

        service.isAvailable(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the past, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(pastOutage), List("nino", "utr")))

        service.isAvailable(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the future, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(futureOutage), List("nino", "utr")))

        service.isAvailable(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

    }

    "return false" when {
      "outage defined and covers the period now" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(currentOutage), List("nino", "utr")))

        service.isAvailable(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe false
      }

    }

    "getQuestions" should {
      "return empty list if service is available" when {
        "connector throws error" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))
          override def connectorResult: Future[Seq[TestRecord]] = badRequestResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(ninoIdentifier, saUtrIdentifier)).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe "p60Service, threw exception uk.gov.hmrc.http.Upstream4xxResponse: bad bad bad request, selection: AA000000D,12345678"
          }
        }

        "connector returns not found" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))
          override def connectorResult: Future[Seq[TestRecord]] = notFoundResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(ninoIdentifier, saUtrIdentifier)).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.INFO)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe "p60Service, no records returned for selection, origin: origin, identifiers: AA000000D,12345678"
          }
        }

        "connector returns an unhealthy service exception" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

          (service3.eventDispatcher.dispatchEvent(_: MonitoringEvent)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
            .expects(ServiceUnavailableEvent("p60Service"),*,*,*)

          (service3.auditService.sendCircuitBreakerEvent(_: Selection, _: String)(_: HeaderCarrier, _: ExecutionContext))
            .expects(Selection(ninoIdentifier,saUtrIdentifier),"p60Service",*,*)

          service3.questions(Selection(ninoIdentifier, saUtrIdentifier)).futureValue shouldBe Seq()
        }
      }

      "return empty list if service unavailable" when {
        "outage set and covers current time" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(currentOutage), List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(ninoIdentifier, saUtrIdentifier)).futureValue shouldBe Seq()
            logs.size shouldBe 0
          }
        }

        "no outage set not all required Identifiers are present" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(saUtrIdentifier)).futureValue shouldBe Seq()
            logs.size shouldBe 0
          }
        }
      }

      "return list of questions if service available" when {
        "connector successful" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult
          service.questions(Selection(ninoIdentifier, saUtrIdentifier)).futureValue shouldBe List(Question(PaymentToDate,List(TestRecord(1).toString)))
        }
      }
    }

    "calling multiple Question Services" should {
      "return Seq of Questions" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(*).returning(mockAppConfig.ServiceState(None, List("nino", "utr"))).noMoreThanTwice()
        val services = Seq(service, service2)
        val selection: Selection = Selection(ninoIdentifier, saUtrIdentifier)

        Future.sequence(services.map(_.questions(selection))).map(_.flatten).futureValue shouldBe Seq()
      }
    }
  }


  trait Setup extends TestData {
    self =>

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("user-agent" -> "origin")

    def connectorResult: Future[Seq[TestRecord]] = illegalAccessResult

    def connector: QuestionConnector[TestRecord] = new QuestionConnector[TestRecord] {
      def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TestRecord]] = connectorResult
    }

    def connector2: QuestionConnector[TestRecord] = new QuestionConnector[TestRecord] {
      def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TestRecord]] = unhealthyServiceExceptionResult
    }

    import uk.gov.hmrc.questionrepository.services.utilities.CheckAvailability

    abstract class TestService extends QuestionService with CheckAvailability

    lazy val service: TestService {
      type Record = TestRecord
    } = new TestService {
      override val serviceName = p60Service
      override type Record = TestRecord

      override def connector: QuestionConnector[TestRecord] = self.connector

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("p60Service", 2, 1000, 1000)

      override def evidenceTransformer(records: Seq[TestRecord]): Seq[Question] = records.map(r => Question(PaymentToDate, Seq(r.toString))).toList

      override implicit val appConfig: AppConfig = mockAppConfig
      override implicit val eventDispatcher: EventDispatcher = mock[EventDispatcher]
      override implicit val auditService: AuditService = mock[AuditService]
    }

    lazy val service2: TestService {
      type Record = TestRecord
    } = new TestService {
      override val serviceName = p60Service
      override type Record = TestRecord

      override def connector: QuestionConnector[TestRecord] = self.connector

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("p60Service", 2, 1000, 1000)

      override def evidenceTransformer(records: Seq[TestRecord]): Seq[Question] = records.map(r => Question(PaymentToDate, Seq(r.toString))).toList

      override implicit val appConfig: AppConfig = mockAppConfig
      override implicit val eventDispatcher: EventDispatcher = mock[EventDispatcher]
      override implicit val auditService: AuditService = mock[AuditService]
    }

    lazy val service3: TestService {
      type Record = TestRecord
    } = new TestService {
      override val serviceName = p60Service
      override type Record = TestRecord

      override def connector: QuestionConnector[TestRecord] = connector2

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("p60Service", 2, 1000, 1000)

      override def evidenceTransformer(records: Seq[TestRecord]): Seq[Question] = records.map(r => Question(PaymentToDate, Seq(r.toString))).toList

      override implicit val appConfig: AppConfig = mockAppConfig
      override implicit val eventDispatcher: EventDispatcher = mock[EventDispatcher]
      override implicit val auditService: AuditService = mock[AuditService]
    }
  }

  trait TestData {

    val ninoIdentifier: Nino = Nino("AA000000D")
    val saUtrIdentifier: SaUtr = SaUtr("12345678")
    val futureOutage: Outage = Outage(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
    val pastOutage: Outage = Outage(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1))
    val currentOutage: Outage = Outage(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))

    case class TestRecord(value: BigDecimal)

    def illegalAccessResult: Future[Seq[TestRecord]] = Future.failed(new IllegalAccessException("Connector should not have been called"))
    def testRecordResult: Future[Seq[TestRecord]] = Future.successful(Seq(TestRecord(1)))
    def notFoundResult: Future[Seq[TestRecord]] = Future.failed(UpstreamErrorResponse("no no nooooo, no records found", NOT_FOUND))
    def badRequestResult: Future[Seq[TestRecord]] = Future.failed(UpstreamErrorResponse("bad bad bad request", BAD_REQUEST))
    def unhealthyServiceExceptionResult: Future[Seq[TestRecord]] = Future.failed(new UnhealthyServiceException("issue with service"))
  }
}
