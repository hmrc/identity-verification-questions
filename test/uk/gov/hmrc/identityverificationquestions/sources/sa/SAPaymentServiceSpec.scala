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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import Utils.UnitSpec
import org.joda.time.{LocalDate, LocalDateTime}
import org.scalatest.LoneElement
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.{Configuration, Logging}
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.SelfAssessment.SelfAssessedPaymentQuestion
import uk.gov.hmrc.identityverificationquestions.models.{QuestionWithAnswers, Selection}
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SAPaymentServiceSpec extends UnitSpec with Eventually with LogCapturing with LoneElement with Logging {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(500, Millis)))

  val testRequest: Selection = Selection(Nino("AA000003D"))

  "Obtain Questions" should {
    "obtain the correct questions" in new Setup {
      (mockConnector.getReturns(_: SaUtr)(_: HeaderCarrier, _: ExecutionContext)).expects(sautr, *, *).returning(Future.successful(testRecords))

      private val actual = service.questions(testJourney, corrId).futureValue

      actual.size shouldBe 1

      val actualQuestion: QuestionWithAnswers = actual.head

      actualQuestion.questionKey shouldBe SelfAssessedPaymentQuestion
      actualQuestion.answers.size shouldBe 2
      actualQuestion.answers.head shouldBe """{"amount":100,"paymentDate":"2020-06-01","transactionCode":"PYT"}"""
      actualQuestion.answers(1) shouldBe """{"amount":15.51,"paymentDate":"2017-06-01","transactionCode":"PYT"}"""
      actualQuestion.info shouldBe Map.empty

    }

    "limit questions to the last few years based on the payment window" in new Setup {
      override lazy val saPaymentWindowsYears: Int = 2

      (mockConnector.getReturns(_: SaUtr)(_: HeaderCarrier, _: ExecutionContext)).expects(sautr, *, *).returning(Future.successful(testRecords))

      private val actual = service.questions(testJourney, corrId).futureValue

      actual.size shouldBe 1

      val actualQuestion: QuestionWithAnswers = actual.head

      actualQuestion.questionKey shouldBe SelfAssessedPaymentQuestion
      actualQuestion.answers.size shouldBe 1
      actualQuestion.answers.head shouldBe """{"amount":100,"paymentDate":"2020-06-01","transactionCode":"PYT"}"""
      actualQuestion.info shouldBe Map.empty
    }

    "limit questions to those with a transaction code of PYT" in new Setup {
      val updatedRecords: Seq[SAPaymentReturn] = testRecords.map { record =>
        val updatedPayments = record.payments.map {
          case payment if payment.amount == 100 => payment.copy(transactionCode = Some("BCC"))
          case payment => payment
        }
        record.copy(payments = updatedPayments)
      }

      (mockConnector.getReturns(_: SaUtr)(_: HeaderCarrier, _: ExecutionContext)).expects(sautr, *, *).returning(Future.successful(updatedRecords))

      private val actual = service.questions(testJourney, corrId).futureValue

      actual.size shouldBe 1

      val actualQuestion: QuestionWithAnswers = actual.head

      actualQuestion.questionKey shouldBe SelfAssessedPaymentQuestion
      actualQuestion.answers.size shouldBe 1
      actualQuestion.answers.head shouldBe """{"amount":15.51,"paymentDate":"2017-06-01","transactionCode":"PYT"}"""
      actualQuestion.info shouldBe Map.empty
    }

    "limit questions to those with a transaction code of PYT and TFO" in new Setup {
      val updatedRecords: Seq[SAPaymentReturn] = testRecords.map { record =>
        val updatedPayments = record.payments.map {
          case payment if payment.amount == 100 => payment.copy(transactionCode = Some("TFO"))
          case payment => payment
        }
        record.copy(payments = updatedPayments)
      }

      (mockConnector.getReturns(_: SaUtr)(_: HeaderCarrier, _: ExecutionContext)).expects(sautr, *, *).returning(Future.successful(updatedRecords))

      private val actual = service.questions(testJourney, corrId).futureValue

      actual.size shouldBe 1

      val actualQuestion: QuestionWithAnswers = actual.head

      actualQuestion.questionKey shouldBe SelfAssessedPaymentQuestion
      actualQuestion.answers.size shouldBe 2
      actualQuestion.answers.head shouldBe """{"amount":100,"paymentDate":"2020-06-01","transactionCode":"TFO"}"""
      actualQuestion.answers(1) shouldBe """{"amount":15.51,"paymentDate":"2017-06-01","transactionCode":"PYT"}"""
      actualQuestion.info shouldBe Map.empty
    }

    "do not obtain any questions if the values are all zero" in new Setup {
      val zeroRecords: Seq[SAPaymentReturn] = testRecords.map(record => {
        val payments = record.payments.map(saPayment => saPayment.copy(amount = BigDecimal(0)))
        record.copy(payments = payments)
      })

      (mockConnector.getReturns(_: SaUtr)(_: HeaderCarrier, _: ExecutionContext)).expects(sautr, *, *).returning(Future.successful(zeroRecords))

      val actual: Seq[QuestionWithAnswers] = service.questions(testJourney, corrId).futureValue

      actual.size shouldBe 0
    }

    "do not obtain any questions if the connector returns NOT FOUND" in new Setup {
      (mockConnector.getReturns(_: SaUtr)(_: HeaderCarrier, _: ExecutionContext)).expects(sautr, *, *)
        .returning(Future.failed(new NotFoundException("not found")))

      service.questions(testJourney, corrId).futureValue shouldBe Seq()
    }

    "is disabled now" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "microservice.services.SelfAssessmentPaymentService.disabled.start" -> (LocalDateTime.now().minusDays(10).toString("yyyy-MM-dd") + "T00:00:00.000"),
        "microservice.services.SelfAssessmentPaymentService.disabled.end" -> (LocalDateTime.now().plusDays(10).toString("yyyy-MM-dd") + "T00:00:00.000")
      )

      service.questions(testJourney, corrId).futureValue shouldBe Seq()
    }

    "is disabled for disallowed origin" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "microservice.services.SelfAssessmentPaymentService.disabled.origin.0" -> "tcs"
      )

      service.questions(testJourney, corrId).futureValue shouldBe Seq()
    }
  }

  private trait Setup {
    lazy val additionalConfig: Map[String, Any] = Map.empty
    lazy val saPaymentWindowsYears = 4
    private lazy val configData: Map[String, Any] = Map(
      "hods.circuit.breaker.numberOfCallsToTrigger" -> 3,
      "hods.circuit.breaker.unavailablePeriodDurationInSec" -> 15,
      "hods.circuit.breaker.unstablePeriodDurationInSec" -> 30,
      "microservice.services.SelfAssessmentPaymentService.minimumMeoQuestions" -> 1,
      "version" -> "2",
      "sa.payment.window" -> saPaymentWindowsYears,
      "sa.payment.tolerance.future.days" -> 3,
      "sa.payment.tolerance.past.days" -> 3
    ) ++ additionalConfig
    val config: Configuration = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)
    implicit val appConfig: AppConfig = new AppConfig(config, servicesConfig)

    implicit val request: Request[_] = FakeRequest()
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val fixedDate: LocalDate = LocalDate.parse("2020-06-01")

    protected val mockConnector: SAPaymentsConnector = mock[SAPaymentsConnector]
    protected val mockEventDispatcher: EventDispatcher = mock[EventDispatcher]
    protected val mockAuditService: AuditService = mock[AuditService]

    val sautr: SaUtr = SaUtr("123456789")

    val testJourney: Selection = Selection(SaUtr("123456789"))

    val testRecords: Seq[SAPaymentReturn] = Seq(SAPaymentReturn(Vector(
      SAPayment(BigDecimal(100), Some(fixedDate), Some("PYT")),
      SAPayment(BigDecimal(15.51), Some(fixedDate.minusYears(3)), Some("PYT"))
    )))

    val service: SAPaymentService = new SAPaymentService(mockConnector, mockEventDispatcher, mockAuditService, appConfig) {
      override def currentDate: LocalDate = fixedDate
    }

  }

}
