/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.SCPEmail

import Utils.UnitSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.SCPEmailAnswerConnector
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SCPEmailAnswerServiceSpec extends UnitSpec {

  "verifyAnswer" should {
    "return score of 'Correct'" when {
      "answer matches an answer retrieved from repo" in new Setup {
        when(mockQuestionRepo.findAnswers(any, any)).thenReturn(Future.successful(List(correctQDC)))
        connector.verifyAnswer(corrId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(SCPEmailQuestion, Correct)
      }
    }

    "return score of 'Incorrect'" when {
      "answer does not match an answer retrieved from repo" in new Setup {
        when(mockQuestionRepo.findAnswers(any, any)).thenReturn(Future.successful(List(inCorrectQDC)))
        connector.verifyAnswer(corrId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(SCPEmailQuestion, Incorrect)
      }
    }

    "return score of 'Unknown'" when {
      "no answers retrieved from repo" in new Setup {
        when(mockQuestionRepo.findAnswers(any, any)).thenReturn(Future.successful(List()))
        connector.verifyAnswer(corrId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(SCPEmailQuestion, Unknown)
      }
    }
  }

  trait Setup extends  TestData {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockQuestionRepo = mock[QuestionMongoRepository]
    val connector = new SCPEmailAnswerConnector(mockQuestionRepo)
  }

  trait TestData {
    val answerDetails: AnswerDetails = AnswerDetails(SCPEmailQuestion, StringAnswer("email@email.com"))
    val correctQDC = QuestionDataCache(corrId, Selection(origin, Seq(ninoIdentifier, saUtrIdentifier)), Seq(Question(SCPEmailQuestion, Seq("email@email.com"))), dateTime)
    val inCorrectQDC = QuestionDataCache(corrId, Selection(origin, Seq(ninoIdentifier, saUtrIdentifier)), Seq(Question(SCPEmailQuestion, Seq("bad-email@bad-email.com"))), dateTime)
  }
}
