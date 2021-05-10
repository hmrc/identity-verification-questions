/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import java.time.Period

import Utils.UnitSpec
import org.mockito.MockitoSugar.mock
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.Dvla.DvlaService
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.PassportService
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.SCPEmailService
import uk.gov.hmrc.questionrepository.models.identifier.{DobI, NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EvidenceRetrievalServiceSpec extends UnitSpec{

  "calling callAllEvidenceSources" should {
    "return a QuestionResponse with empty sequence of questions if no matching records" in new Setup {
      when(mockP60Service.questions(any)(any)).thenReturn(Future.successful(Seq.empty[Question]))
      when(mockPassportService.questions(any)(any)).thenReturn(Future.successful(Seq.empty[Question]))
      when(mockSCPEmailService.questions(any)(any)).thenReturn(Future.successful(Seq.empty[Question]))
      when(mockDvlaService.questions(any)(any)).thenReturn(Future.successful(Seq.empty[Question]))
      when(mockMongoRepo.store(any)).thenReturn(Future.successful(Unit))
      when(mockAppConfig.questionRecordTTL).thenReturn(Period.parse("P1D"))
      val result: QuestionResponse = service.callAllEvidenceSources(selection).futureValue
      result.questions shouldBe Seq.empty[Question]
    }

    "return a QuestionResponse with sequence of questions if matching records are found" in new Setup {
      when(mockP60Service.questions(any)(any)).thenReturn(Future.successful(Seq(Question(PaymentToDate, List(TestRecord(1).toString)))))
      when(mockPassportService.questions(any)(any)).thenReturn(Future.successful(Seq(Question(PassportQuestion, List(TestRecord(12345).toString)))))
      when(mockSCPEmailService.questions(any)(any)).thenReturn(Future.successful(Seq(Question(SCPEmailQuestion, Seq("email@email.com"), Map.empty[String, String]))))
      when(mockDvlaService.questions(any)(any)).thenReturn(Future.successful(Seq(Question(DVLAQuestion, List(TestRecord(1).toString), Map.empty[String, String]))))
      when(mockMongoRepo.store(any)).thenReturn(Future.successful(Unit))
      when(mockAppConfig.questionRecordTTL).thenReturn(Period.parse("P1D"))
      when(mockMessageTextService.getQuestionMessageEn(eqTo[QuestionKey](PaymentToDate))).thenReturn(Map("PaymentToDate" -> "payment question"))
      when(mockMessageTextService.getQuestionMessageCy(eqTo[QuestionKey](PaymentToDate))).thenReturn(Map("PaymentToDate" -> "cy payment question"))
      when(mockMessageTextService.getQuestionMessageEn(eqTo[QuestionKey](PassportQuestion))).thenReturn(Map("PassportQuestion" -> "passport question"))
      when(mockMessageTextService.getQuestionMessageCy(eqTo[QuestionKey](PassportQuestion))).thenReturn(Map("PassportQuestion" -> "cy passport question"))
      when(mockMessageTextService.getQuestionMessageEn(eqTo[QuestionKey](SCPEmailQuestion))).thenReturn(Map("SCPEmailQuestion" -> "scp Email question"))
      when(mockMessageTextService.getQuestionMessageCy(eqTo[QuestionKey](SCPEmailQuestion))).thenReturn(Map("SCPEmailQuestion" -> "cy scp Email question"))
      when(mockMessageTextService.getQuestionMessageEn(eqTo[QuestionKey](DVLAQuestion))).thenReturn(Map("DVLAQuestion" -> "DVLAQuestion"))
      when(mockMessageTextService.getQuestionMessageCy(eqTo[QuestionKey](DVLAQuestion))).thenReturn(Map("DVLAQuestion" -> "cy DVLAQuestion"))

      val result: QuestionResponse = service.callAllEvidenceSources(selection).futureValue
      result.questions shouldBe
        Seq(Question(PaymentToDate,List.empty[String]), Question(PassportQuestion,List.empty[String]), Question(SCPEmailQuestion, List.empty[String]), Question(DVLAQuestion, List.empty[String]))
      result.questionTextEn shouldBe
        Map("PaymentToDate" -> "payment question", "PassportQuestion" -> "passport question", "SCPEmailQuestion" -> "scp Email question", "DVLAQuestion" -> "DVLAQuestion")
      result.questionTextCy shouldBe
        Some(Map("PaymentToDate" -> "cy payment question", "PassportQuestion" -> "cy passport question", "SCPEmailQuestion" -> "cy scp Email question", "DVLAQuestion" -> "cy DVLAQuestion"))
    }
  }

}

trait Setup {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val mockAppConfig: AppConfig = mock[AppConfig]
  val mockP60Service: P60Service = mock[P60Service]
  val mockPassportService: PassportService = mock[PassportService]
  val mockSCPEmailService: SCPEmailService = mock[SCPEmailService]
  val mockDvlaService: DvlaService = mock[DvlaService]
  val mockMongoRepo: QuestionMongoRepository = mock[QuestionMongoRepository]
  val mockMessageTextService: MessageTextService = mock[MessageTextService]
  val service = new EvidenceRetrievalService(mockMongoRepo, mockMessageTextService, mockAppConfig, mockP60Service, mockPassportService, mockSCPEmailService, mockDvlaService)
  val origin: Origin = Origin("alala")
  val ninoIdentifier: NinoI = NinoI("AA000000D")
  val saUtrIdentifier: SaUtrI = SaUtrI("12345678")
  val dobIdentifier: DobI = DobI("1984-01-01")
  val selection: Selection = Selection(origin, Seq(ninoIdentifier, saUtrIdentifier, dobIdentifier))
  case class TestRecord(value: BigDecimal)
}
