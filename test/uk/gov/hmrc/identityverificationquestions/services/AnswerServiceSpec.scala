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
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.config.{AppConfig, Outage}
import uk.gov.hmrc.identityverificationquestions.connectors.AnswerConnector
import uk.gov.hmrc.identityverificationquestions.models.P60.{EmployeeNIContributions, PaymentToDate}
import uk.gov.hmrc.identityverificationquestions.models.{AnswerCheck, AnswerDetails, Correct, CorrelationId, IvJourney, QuestionKey, QuestionResult, Score, Selection, ServiceName, SimpleAnswer, Unknown, p60Service}
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.services.utilities.CheckAvailability

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswerServiceSpec extends UnitSpec with LogCapturing {

  "check isAvailable" should {
    "return true" when {
      "no outage is defined, and required identifiers are present" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(None, List("nino", "utr")))

        service.isAvailableForRequestedSelection(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the past, and required identifiers are present" in new Setup {
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(p60Service).returning(mockAppConfig.ServiceState(Some(pastOutage), List("nino", "utr")))

        service.isAvailableForRequestedSelection(Selection(ninoIdentifier, saUtrIdentifier)) shouldBe true
      }

      "outage defined but is in the future, and required identifiers are present" in new Setup {
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

    "checkAnswers" should {
      "return list of supported questions with score of 'unknown'" when {
        "connector throws error" in new Setup {
          override def connectorResult: Future[TestRecord] = badRequestResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(correlationId, Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer), None),paymentToDateAnswer).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
            val errorLogs = logs.filter(_.getLevel == Level.ERROR)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe s"p60Service, threw exception uk.gov.hmrc.http.Upstream4xxResponse: bad bad bad request, correlationId: ${correlationId.id}"
          }
        }

        "connector returns not found" in new Setup {
          override def connectorResult: Future[TestRecord] = notFoundResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(correlationId, Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer), None), paymentToDateAnswer).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
            val errorLogs = logs.filter(_.getLevel == Level.WARN)
            errorLogs.size shouldBe 1
            errorLogs.head.getMessage shouldBe s"p60Service, no answers returned for selection, correlationId: ${correlationId.id}"
          }
        }
      }

      "return list of questions if service available" when {
        "connector successful" in new Setup {
          override def connectorResult: Future[TestRecord] = testRecordResult

          withCaptureOfLoggingFrom[AnswerServiceSpec] { logs =>
            service.checkAnswers(AnswerCheck(correlationId, Seq(paymentToDateAnswer, EmployeeNIContributionsAnswer), None), paymentToDateAnswer).futureValue shouldBe Seq(QuestionResult(PaymentToDate, Correct))
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
    implicit val request: Request[_] = FakeRequest()

    def connectorResult: Future[TestRecord] = illegalAccessResult

    def connector: AnswerConnector[TestRecord] = new AnswerConnector[TestRecord] {
      override def verifyAnswer(correlationId: CorrelationId, answer: AnswerDetails, ivJourney: Option[IvJourney])(implicit hc: HeaderCarrier, request: Request[_]): Future[TestRecord] = connectorResult
    }

    abstract class TestService extends AnswerService with CheckAvailability

    lazy val service: TestService {
      type Record = TestRecord
    } = new TestService {
      override type Record = TestRecord
      override val serviceName: ServiceName = p60Service

      override def connector: AnswerConnector[TestRecord] = self.connector

      override def supportedQuestions: Seq[QuestionKey] = Seq(PaymentToDate)

      override def answerTransformer(records: Seq[TestRecord], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] =
        records.map(r => QuestionResult(r.questionKey, r.answer))

      override protected def circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig("p60Service", 2, 1000, 1000)

      override def auditService: AuditService = mock[AuditService]

      override implicit val appConfig: AppConfig = mockAppConfig
    }
  }

  trait TestData {
    val correlationId: CorrelationId = CorrelationId()
    val ninoIdentifier: Nino = Nino("AA000000D")
    val saUtrIdentifier: SaUtr = SaUtr("12345678")
    val futureOutage: Outage = Outage(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
    val pastOutage: Outage = Outage(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1))
    val currentOutage: Outage = Outage(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))

    val paymentToDateAnswer: AnswerDetails = AnswerDetails(PaymentToDate, SimpleAnswer("100.11"))
    val EmployeeNIContributionsAnswer: AnswerDetails = AnswerDetails(EmployeeNIContributions, SimpleAnswer("200.22"))

    case class TestRecord(questionKey: QuestionKey, answer: Score)

    def illegalAccessResult: Future[TestRecord] = Future.failed(new IllegalAccessException("Connector should not have been called"))
    def testRecordResult: Future[TestRecord] = Future.successful(TestRecord(PaymentToDate, Correct))
    def notFoundResult: Future[TestRecord] = Future.failed(UpstreamErrorResponse("no no nooooo, no records found", NOT_FOUND))
    def badRequestResult: Future[TestRecord] = Future.failed(UpstreamErrorResponse("bad bad bad request", BAD_REQUEST))
  }
}
