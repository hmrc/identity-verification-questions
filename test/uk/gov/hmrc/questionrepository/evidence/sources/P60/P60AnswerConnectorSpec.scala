/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.P60

import Utils.UnitSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60AnswerConnector
import uk.gov.hmrc.questionrepository.models.identifier.{NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, Correct, CorrelationId, DoubleAnswer, Incorrect, Origin, PaymentToDate, Question, QuestionDataCache, QuestionResult, Selection, Unknown}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class P60AnswerConnectorSpec extends UnitSpec {

  "verifyAnswer" should {
    "return score of 'Correct'" when {
      "answer matches an answer retrieved from repo" in new Setup {
        when(mockQuestionRepo.findAnswers(any, any)).thenReturn(Future.successful(List(correctQDC)))

        connector.verifyAnswer(correlationId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(PaymentToDate, Correct)
      }
    }

    "return score of 'Incorrect'" when {
      "answer does not matche an answer retrieved from repo" in new Setup {
        when(mockQuestionRepo.findAnswers(any, any)).thenReturn(Future.successful(List(inCorrectQDC)))

        connector.verifyAnswer(correlationId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(PaymentToDate, Incorrect)
      }
    }

    "return score of 'Unknown'" when {
      "no answers retrieved from repo" in new Setup {
        when(mockQuestionRepo.findAnswers(any, any)).thenReturn(Future.successful(List()))

        connector.verifyAnswer(correlationId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(PaymentToDate, Unknown)
      }
    }
  }

  trait Setup extends  TestData {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockQuestionRepo = mock[QuestionMongoRepository]
    val connector = new P60AnswerConnector(mockQuestionRepo)
  }

  trait TestData {
    val correlationId = CorrelationId()
    val dateTime = LocalDateTime.now()
    val origin: Origin = Origin("alala")
    val ninoIdentifier: NinoI = NinoI("AA000000D")
    val saUtrIdentifier: SaUtrI = SaUtrI("12345678")
    val answerDetails: AnswerDetails = AnswerDetails(PaymentToDate, DoubleAnswer(100.11))
    val correctQDC = QuestionDataCache(correlationId, Selection(origin, Seq(ninoIdentifier, saUtrIdentifier)), Seq(Question(PaymentToDate, Seq("200.22", "100.11"))), dateTime)
    val inCorrectQDC = QuestionDataCache(correlationId, Selection(origin, Seq(ninoIdentifier, saUtrIdentifier)), Seq(Question(PaymentToDate, Seq("200.22", "300.33"))), dateTime)
  }
}
