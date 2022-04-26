/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.Dvla.DvlaAnswerService
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60AnswerService
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.PassportAnswerService
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.SCPEmailAnswerService
import uk.gov.hmrc.questionrepository.models.P60.PaymentToDate
import uk.gov.hmrc.questionrepository.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswerVerificationServiceSpec extends UnitSpec {

  "calling checkAnswers" should {
    "return a Future of QuestionResult with a result of Unknown" when {
      "the requested answer service returns Unknown" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq(QuestionResult(PaymentToDate, Unknown))))
        // ver-1281: not in use for now
//        (mockPassportAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockSCPEmailAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockDvlaAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
      }
    }

    "return a Future of QuestionResult with a result of Correct" when {
      "the requested answer service returns Correct" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq(QuestionResult(PaymentToDate, Correct))))
        // ver-1281: not in use for now
//        (mockPassportAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockSCPEmailAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockDvlaAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Correct))
      }
    }

    "return a Future of QuestionResult with a result of Incorrect" when {
      "the requested answer service returns Correct" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(Seq(QuestionResult(PaymentToDate, Incorrect))))
        // ver-1281: not in use for now
//        (mockPassportAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockSCPEmailAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockDvlaAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Incorrect))
      }
    }

    "Throw a RuntimeException" when {
      "no supported answer service found for questionKey" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        // ver-1281: not in use for now
//        (mockPassportAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockSCPEmailAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockDvlaAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheck)
        }
      }

      /** can't be easily tested until we have multiple evidence sources **/
      "multiple supporting answer services found for questionKey" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        // ver-1281: not in use for now
//        (mockPassportAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
//        (mockSCPEmailAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
//        (mockDvlaAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheck)
        }
      }
    }
  }


  trait SetUp{
    val mockP60AnswerService = mock[P60AnswerService]
    val mockPassportAnswerService = mock[PassportAnswerService]
    val mockSCPEmailAnswerService = mock[SCPEmailAnswerService]
    val mockDvlaAnswerService = mock[DvlaAnswerService]
    val service = new AnswerVerificationService(mockP60AnswerService, mockPassportAnswerService, mockSCPEmailAnswerService, mockDvlaAnswerService)
    val answerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, StringAnswer("an answer")))
    val answerCheck: AnswerCheck = AnswerCheck(corrId, Selection(ninoIdentifier), answerDetails)
  }
}
