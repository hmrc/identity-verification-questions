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
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.P60.PaymentToDate
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60AnswerService
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAAnswerService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswerVerificationServiceSpec extends UnitSpec {

  "calling checkAnswers" should {
    "return a Future of QuestionResult with a result of Unknown" when {
      "the requested answer service returns Unknown" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck)(_: Request[_], _: HeaderCarrier)).expects(*, *, *).returning(Future.successful(Seq(QuestionResult(PaymentToDate, Unknown))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
      }
    }

    "return a Future of QuestionResult with a result of Correct" when {
      "the requested answer service returns Correct" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck)(_: Request[_], _: HeaderCarrier)).expects(*, *, *).returning(Future.successful(Seq(QuestionResult(PaymentToDate, Correct))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Correct))
      }
    }

    "return a Future of QuestionResult with a result of Incorrect" when {
      "the requested answer service returns Correct" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck)(_: Request[_], _: HeaderCarrier)).expects(*, *, *).returning(Future.successful(Seq(QuestionResult(PaymentToDate, Incorrect))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheck))
        result shouldBe Seq(QuestionResult(PaymentToDate, Incorrect))
      }
    }

    "Throw a RuntimeException" when {
      "no supported answer service found for questionKey" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheck)
        }
      }

      /** can't be easily tested until we have multiple evidence sources **/
      "multiple supporting answer services found for questionKey" in new SetUp {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheck)
        }
      }
    }
  }


  trait SetUp{
    implicit val request: Request[_] = FakeRequest()
    val mockP60AnswerService: P60AnswerService = mock[P60AnswerService]
    val mockSAAnswerService: SAAnswerService = mock[SAAnswerService]
    val service = new AnswerVerificationService(mockP60AnswerService, mockSAAnswerService)
    val answerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, SimpleAnswer("an answer")))
    val answerCheck: AnswerCheck = AnswerCheck(corrId, Selection(ninoIdentifier), answerDetails)
    (mockSAAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
  }
}
