/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.UnitSpec
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.evidences.sources.sa.SAService
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import java.time.{LocalDate, Period}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EvidenceRetrievalServiceSpec extends UnitSpec {

  "calling callAllEvidenceSources" should {
    "return a QuestionResponse with empty sequence of questions if no matching records" in new Setup {
      (mockP60Service.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*, *,*,*).returning(Future.successful(Seq.empty[Question]))
      (mockSAService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*, *,*,*).returning(Future.successful(Seq.empty[Question]))
      // ver-1281: not in use for now
//      (mockPassportService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq.empty[Question]))
//      (mockSCPEmailService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq.empty[Question]))
//      (mockDvlaService.questions(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq.empty[Question]))
      (mockAppConfig.questionRecordTTL _).expects().returning(Period.parse("P1D"))
      val result: QuestionResponse = service.callAllEvidenceSources(selection).futureValue
      result.questions shouldBe Seq.empty[Question]
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

    val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(reactiveMongoComponent)
    val service = new EvidenceRetrievalService(mongoRepo, mockAppConfig, mockP60Service, mockSAService)
    val ninoIdentifier: Nino = Nino("AA000000D")
    val saUtrIdentifier: SaUtr = SaUtr("12345678")
    val dobIdentifier: LocalDate = LocalDate.parse("1984-01-01")
    val selection: Selection = Selection(Some(ninoIdentifier), Some(saUtrIdentifier), Some(dobIdentifier))

    case class TestRecord(value: BigDecimal)
  }
}
