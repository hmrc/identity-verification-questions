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
import play.api.Configuration
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.SelfAssessment.{SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion}
import uk.gov.hmrc.identityverificationquestions.models.{CorrelationId, QuestionWithAnswers, Selection}
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SAServiceSpec extends UnitSpec {

  val testRequest: Selection = Selection(Nino("AA000003D"))
  "Self Assessment Service" should {
    "obtain payment question if only it is returned" in new Setup {
      (mockSaPaymentService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *, *)
        .returning(Future.successful(Seq(paymentQuestion)))

      (mockSaPensionService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *, *)
        .returning(Future.successful(Seq()))

      val actual: Seq[QuestionWithAnswers] = service.questions(testRequest, corrId).futureValue

      actual shouldBe List(paymentQuestion)
    }

    "obtain payment question if both questions are returned" in new Setup {
      (mockSaPaymentService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *, *)
        .returning(Future.successful(Seq(paymentQuestion)))

      (mockSaPensionService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *, *)
        .returning(Future.successful(Seq(pensionQuestion)))

      val actual: Seq[QuestionWithAnswers] = service.questions(testRequest, corrId).futureValue

      actual shouldBe List(paymentQuestion)
    }

    "obtain pension question if only it is returned" in new Setup {
      (mockSaPaymentService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *, *)
        .returning(Future.successful(Seq()))

      (mockSaPensionService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *, *)
        .returning(Future.successful(Seq(pensionQuestion)))

      val actual: Seq[QuestionWithAnswers] = service.questions(testRequest, corrId).futureValue

      actual shouldBe List(pensionQuestion)
    }
  }

  //private trait Setup extends JourneyData {
  private trait Setup {
    lazy val additionalConfig: Map[String, Any] = Map.empty
    private lazy val configData: Map[String, Any] = Map(
      "hods.circuit.breaker.numberOfCallsToTrigger" -> 3,
      "hods.circuit.breaker.unavailablePeriodDurationInSec" -> 15,
      "hods.circuit.breaker.unstablePeriodDurationInSec" -> 30,
      "microservice.services.SelfAssessmentService.minimumMeoQuestions" -> 1,
      "version" -> "2"
    ) ++ additionalConfig
    val config: Configuration = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)
    implicit val appConfig: AppConfig = new AppConfig(config, servicesConfig)

    implicit val request : Request[_] = FakeRequest()
    implicit val hc: HeaderCarrier = HeaderCarrier()

    protected val mockSaPensionService: SAPensionService = mock[SAPensionService]
    protected val mockSaPaymentService: SAPaymentService = mock[SAPaymentService]
    protected val mockEventDispatcher: EventDispatcher = mock[EventDispatcher]
    protected val mockAuditService: AuditService = mock[AuditService]


    val paymentQuestion: QuestionWithAnswers = QuestionWithAnswers(SelfAssessedPaymentQuestion,  Seq("123.11"))
    val pensionQuestion: QuestionWithAnswers = QuestionWithAnswers(SelfAssessedIncomeFromPensionsQuestion, Seq("456.22"))

    val service = new SAService(appConfig, mockSaPensionService, mockSaPaymentService, mockEventDispatcher, mockAuditService)
  }
}
