/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.UnitSpec
import org.mockito.MockitoSugar.mock
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.models.Identifier.{NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.{Origin, PaymentToDate, Question, QuestionResponse, Selection}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import java.time.Period
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EvidenceRetrievalServiceSpec extends UnitSpec{

  "calling callAllEvidenceSources" should {
    "return a QuestionResponse with empty sequence of questions if no matching records" in new Setup {
      when(mockP60Service.questions(any)(any)).thenReturn(Future.successful(Seq.empty[Question]))
      when(mockMongoRepo.store(any)).thenReturn(Future.successful(Unit))
      when(mockAppConfig.questionRecordTTL).thenReturn(Period.parse("P1D"))
      val result: QuestionResponse = service.callAllEvidenceSources(selection).futureValue
      result.questions shouldBe Seq.empty[Question]
    }

    "return a QuestionResponse with sequence of questions if matching records are found" in new Setup {
      when(mockP60Service.questions(any)(any)).thenReturn(Future.successful(Seq(Question(PaymentToDate,List(TestRecord(1).toString)))))
      when(mockMongoRepo.store(any)).thenReturn(Future.successful(Unit))
      when(mockAppConfig.questionRecordTTL).thenReturn(Period.parse("P1D"))
      val result: QuestionResponse = service.callAllEvidenceSources(selection).futureValue
      result.questions shouldBe Seq(Question(PaymentToDate,List(TestRecord(1).toString)))
    }
  }

}

trait Setup {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val mockAppConfig: AppConfig = mock[AppConfig]
  val mockP60Service: P60Service = mock[P60Service]
  val mockMongoRepo: QuestionMongoRepository = mock[QuestionMongoRepository]
  val service = new EvidenceRetrievalService(mockMongoRepo, mockP60Service, mockAppConfig)
  val origin: Origin = Origin("alala")
  val ninoIdentifier: NinoI = NinoI("AA000000D")
  val saUtrIdentifier: SaUtrI = SaUtrI("12345678")
  val selection: Selection = Selection(origin,Seq(ninoIdentifier,saUtrIdentifier))
  case class TestRecord(value: BigDecimal)
}