/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60AnswerService
import uk.gov.hmrc.questionrepository.models.Identifier._
import uk.gov.hmrc.questionrepository.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswerVerificationServiceSpec extends UnitSpec {

  "calling checkAnswers" should {
    "return a Future of QuestionResult with a result of Unknown" when {
      "the requested answer service returns Unknown" in new SetUp {
        when(mockP60AnswerService.supportedQuestions).thenReturn(Seq(PaymentToDate))
        when(mockP60AnswerService.checkAnswers(any)(any)).thenReturn(Future.successful(Seq(QuestionResult(PaymentToDate, Unknown))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
      }
    }

    "return a Future of QuestionResult with a result of Correct" when {
      "the requested answer service returns Correct" in new SetUp {
        when(mockP60AnswerService.supportedQuestions).thenReturn(Seq(PaymentToDate))
        when(mockP60AnswerService.checkAnswers(any)(any)).thenReturn(Future.successful(Seq(QuestionResult(PaymentToDate, Correct))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Correct))
      }
    }

    "return a Future of QuestionResult with a result of Incorrect" when {
      "the requested answer service returns Correct" in new SetUp {
        when(mockP60AnswerService.supportedQuestions).thenReturn(Seq(PaymentToDate))
        when(mockP60AnswerService.checkAnswers(any)(any)).thenReturn(Future.successful(Seq(QuestionResult(PaymentToDate, Incorrect))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Incorrect))
      }
    }

    "Throw a RuntimeException" when {
      "no supported answer service found for questionKey" in new SetUp {
        when(mockP60AnswerService.supportedQuestions).thenReturn(Seq.empty[QuestionKey])

        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheck)
        }
      }

      /** can't be easily tested until we have multiple evidence sources **/
//      "multiple supporting answer services found for questionKey" in new SetUp {
//        when(mockP60AnswerService.supportedQuestions).thenReturn(Seq.empty[QuestionKey])
//
//        an[RuntimeException] shouldBe thrownBy {
//          service.checkAnswers(answerCheck)
//        }
//      }
    }
  }


  trait SetUp{
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
    val mockP60AnswerService = mock[P60AnswerService]
    val service = new AnswerVerificationService(mockP60AnswerService)
    val origin: Origin = Origin("valid_string")
    val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"))
    val answerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, StringAnswer("an answer")))
    val answerCheck: AnswerCheck = AnswerCheck(origin, identifiers,answerDetails)
  }
}
