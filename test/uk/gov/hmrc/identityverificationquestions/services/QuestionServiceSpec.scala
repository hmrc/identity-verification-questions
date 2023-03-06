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

package uk.gov.hmrc.identityverificationquestions.services

import Utils.{LogCapturing, UnitSpec}
import ch.qos.logback.classic.Level
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.circuitbreaker.{CircuitBreakerConfig, UnhealthyServiceException}
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.config.{AppConfig, Outage}
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.P60.PaymentToDate
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.{EventDispatcher, MonitoringEvent, ServiceUnavailableEvent}
import uk.gov.hmrc.identityverificationquestions.services.utilities.CheckAvailability

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class QuestionServiceSpec extends UnitSpec with LogCapturing {

  "check isAvailable" should {
    "return true" when {
      "no outage is defined, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

        service.isAvailableForRequestedSelection(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the past, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(pastOutage), List("nino", "utr")))

        service.isAvailableForRequestedSelection(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the future, disabledOrigins & enabledOrigins are empty and required identifiers are present" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(futureOutage), List("nino", "utr")))

        service.isAvailableForRequestedSelection(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

    }

    "return false" when {
      "outage defined and covers the period now" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(currentOutage), List("nino", "utr")))

        service.isAvailableForRequestedSelection(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe false
      }

    }

    "getQuestions" should {
      "return empty list if service is available" when {
        "connector throws error" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))
          override def connectorResult: Future[Seq[TestRecord]] = badRequestResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(ninoIdentifier, saUtrIdentifier), corrId).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe "p60Service, threw exception uk.gov.hmrc.http.Upstream4xxResponse: bad bad bad request, origin: origin, selection: XXXX0000D,XXXX5678"
          }
        }

        "connector returns internalServerErrorResult" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))
          override def connectorResult: Future[Seq[TestRecord]] = internalServerErrorResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(ninoIdentifier, saUtrIdentifier), corrId).futureValue shouldBe Seq()
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe "p60Service, threw exception uk.gov.hmrc.http.Upstream5xxResponse: internal server error, origin: origin, selection: XXXX0000D,XXXX5678"
          }
        }

        "connector returns an unhealthy service exception" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

          (service3.eventDispatcher.dispatchEvent(_: MonitoringEvent)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
            .expects(ServiceUnavailableEvent("p60Service"),*,*,*)

          (service3.auditService.sendCircuitBreakerEvent(_: Selection, _: String)(_: HeaderCarrier, _: ExecutionContext))
            .expects(Selection(ninoIdentifier,saUtrIdentifier),"p60Service",*,*)

          service3.questions(Selection(ninoIdentifier, saUtrIdentifier), corrId).futureValue shouldBe Seq()
        }
      }

      "return empty list if service unavailable" when {
        "outage set and covers current time" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(currentOutage), List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(ninoIdentifier, saUtrIdentifier), corrId).futureValue shouldBe Seq()
            logs.size shouldBe 0
          }
        }

        "no outage set not all required Identifiers are present" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult

          withCaptureOfLoggingFrom[QuestionServiceSpec] { logs =>
            service.questions(Selection(saUtrIdentifier), corrId).futureValue shouldBe Seq()
            logs.size shouldBe 0
          }
        }
      }

      "return list of questions if service available" when {
        "connector successful" in new Setup {
          (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

          override def connectorResult: Future[Seq[TestRecord]] = testRecordResult
          service.questions(Selection(ninoIdentifier, saUtrIdentifier), corrId).futureValue shouldBe List(QuestionWithAnswers(PaymentToDate,List(TestRecord(1).toString)))
        }
      }
    }

    "calling multiple Question Services" should {
      "return Seq of Questions" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(*).returning(mockAppConfig.ServiceState(None, List("nino", "utr"))).noMoreThanTwice()
        val services = Seq(service, service2)
        val selection: Selection = Selection(ninoIdentifier, saUtrIdentifier)

        Future.sequence(services.map(_.questions(selection, corrId))).map(_.flatten).futureValue shouldBe Seq()
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

    abstract class TestService extends QuestionService with CheckAvailability

    lazy val service: TestService {
      type Record = TestRecord
    } = new TestService {
      override val serviceName: ServiceName = p60Service
      override type Record = TestRecord

      override def connector: QuestionConnector[TestRecord] = self.connector

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("p60Service", 2, 1000, 1000)

      override def evidenceTransformer(records: Seq[TestRecord], corrId: CorrelationId): Seq[QuestionWithAnswers] = records.map(r => QuestionWithAnswers(PaymentToDate, Seq(r.toString))).toList

      override implicit val appConfig: AppConfig = mockAppConfig
      override implicit val eventDispatcher: EventDispatcher = mock[EventDispatcher]
      override implicit val auditService: AuditService = mock[AuditService]
    }

    lazy val service2: TestService {
      type Record = TestRecord
    } = new TestService {
      override val serviceName: ServiceName = p60Service
      override type Record = TestRecord

      override def connector: QuestionConnector[TestRecord] = self.connector

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("p60Service", 2, 1000, 1000)

      override def evidenceTransformer(records: Seq[TestRecord], corrId: CorrelationId): Seq[QuestionWithAnswers] = records.map(r => QuestionWithAnswers(PaymentToDate, Seq(r.toString))).toList

      override implicit val appConfig: AppConfig = mockAppConfig
      override implicit val eventDispatcher: EventDispatcher = mock[EventDispatcher]
      override implicit val auditService: AuditService = mock[AuditService]
    }

    lazy val service3: TestService {
      type Record = TestRecord
    } = new TestService {
      override val serviceName: ServiceName = p60Service
      override type Record = TestRecord

      override def connector: QuestionConnector[TestRecord] = connector2

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("p60Service", 2, 1000, 1000)

      override def evidenceTransformer(records: Seq[TestRecord], corrId: CorrelationId): Seq[QuestionWithAnswers] = records.map(r => QuestionWithAnswers(PaymentToDate, Seq(r.toString))).toList

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
    def internalServerErrorResult: Future[Seq[TestRecord]] = Future.failed(UpstreamErrorResponse("internal server error", INTERNAL_SERVER_ERROR))
    def badRequestResult: Future[Seq[TestRecord]] = Future.failed(UpstreamErrorResponse("bad bad bad request", BAD_REQUEST))
    def unhealthyServiceExceptionResult: Future[Seq[TestRecord]] = Future.failed(new UnhealthyServiceException("issue with service"))
  }
}
