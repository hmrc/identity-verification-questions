/*
 * Copyright 2022 HM Revenue & Customs
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

import Utils.UnitSpec
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.{QuestionResponse, QuestionWithAnswers, Selection}
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Service
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAService
import java.time.{LocalDate, Period}

import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EvidenceRetrievalServiceSpec extends UnitSpec {

  "calling callAllEvidenceSources" should {
    "return a QuestionResponse with empty sequence of questions if no matching records" in new Setup {
      (mockP60Service.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*, *,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockSAService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*, *,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      // ver-1281: not in use for now
//      (mockPassportService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq.empty[Question]))
//      (mockSCPEmailService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq.empty[Question]))
//      (mockDvlaService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq.empty[Question]))
      (mockAppConfig.questionRecordTTL _).expects().returning(Period.parse("P1D"))
      val result: QuestionResponse = service.callAllEvidenceSources(selection).futureValue
      result.questions shouldBe Seq.empty[QuestionWithAnswers]
    }

    // ver-1281: not in use for now
//    "return a QuestionResponse with sequence of questions if matching records are found" in new Setup {
//      (mockP60Service.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq(Question(PaymentToDate, List(TestRecord(1).toString)))))
//      (mockPassportService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq(Question(PassportQuestion, List(TestRecord(12345).toString)))))
//      (mockSCPEmailService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq(Question(SCPEmailQuestion, Seq("email@email.com"), Map.empty[String, String]))))
//      (mockDvlaService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq(Question(DVLAQuestion, List(TestRecord(1).toString), Map.empty[String, String]))))
//      (mockAppConfig.questionRecordTTL _).expects().returning(Period.parse("P1D"))
//      (mockMessageTextService.getQuestionMessageEn(_: QuestionKey)).expects(PaymentToDate).returning(Map("PaymentToDate" -> "payment question"))
//      (mockMessageTextService.getQuestionMessageCy(_: QuestionKey)).expects(PaymentToDate).returning(Map("PaymentToDate" -> "cy payment question"))
//      (mockMessageTextService.getQuestionMessageEn(_: QuestionKey)).expects(PassportQuestion).returning(Map("PassportQuestion" -> "passport question"))
//      (mockMessageTextService.getQuestionMessageCy(_: QuestionKey)).expects(PassportQuestion).returning(Map("PassportQuestion" -> "cy passport question"))
//      (mockMessageTextService.getQuestionMessageEn(_: QuestionKey)).expects(SCPEmailQuestion).returning(Map("SCPEmailQuestion" -> "scp Email question"))
//      (mockMessageTextService.getQuestionMessageCy(_: QuestionKey)).expects(SCPEmailQuestion).returning(Map("SCPEmailQuestion" -> "cy scp Email question"))
//      (mockMessageTextService.getQuestionMessageEn(_: QuestionKey)).expects(DVLAQuestion).returning(Map("DVLAQuestion" -> "DVLAQuestion"))
//      (mockMessageTextService.getQuestionMessageCy(_: QuestionKey)).expects(DVLAQuestion).returning(Map("DVLAQuestion" -> "cy DVLAQuestion"))
//
//      val result: QuestionResponse = service.callAllEvidenceSources(selection).futureValue
//      result.questions shouldBe
//        Seq(Question(PaymentToDate, List.empty[String]), Question(PassportQuestion, List.empty[String]), Question(SCPEmailQuestion, List.empty[String]), Question(DVLAQuestion, List.empty[String]))
//      result.questionTextEn shouldBe
//        Map("PaymentToDate" -> "payment question", "PassportQuestion" -> "passport question", "SCPEmailQuestion" -> "scp Email question", "DVLAQuestion" -> "DVLAQuestion")
//      result.questionTextCy shouldBe
//        Some(Map("PaymentToDate" -> "cy payment question", "PassportQuestion" -> "cy passport question", "SCPEmailQuestion" -> "cy scp Email question", "DVLAQuestion" -> "cy DVLAQuestion"))
//    }
  }


  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val mockP60Service: P60Service = mock[P60Service]
    val mockSAService: SAService = mock[SAService]
    val mockPayslipService: PayslipService = mock[PayslipService]

    val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(reactiveMongoComponent)
    val service = new EvidenceRetrievalService(mongoRepo, mockAppConfig, mockP60Service, mockSAService, mockPayslipService)
    val ninoIdentifier: Nino = Nino("AA000000D")
    val saUtrIdentifier: SaUtr = SaUtr("12345678")
    val dobIdentifier: LocalDate = LocalDate.parse("1984-01-01")
    val selection: Selection = Selection(Some(ninoIdentifier), Some(saUtrIdentifier), Some(dobIdentifier))

    case class TestRecord(value: BigDecimal)
  }
}
