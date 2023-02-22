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

package uk.gov.hmrc.identityverificationquestions.services

import Utils.UnitSpec
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.P60.PaymentToDate
import uk.gov.hmrc.identityverificationquestions.models.TaxCredits.BankAccount
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60AnswerService
import uk.gov.hmrc.identityverificationquestions.sources.empRef.EmpRefAnswerService
import uk.gov.hmrc.identityverificationquestions.sources.ntc.NtcAnswerService
import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipAnswerService
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAAnswerService
import uk.gov.hmrc.identityverificationquestions.sources.vat.VatReturnsAnswerService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswerVerificationServiceSpec extends UnitSpec {

  "calling checkAnswers for P60" should {
    "return a Future of QuestionResult with a result of Unknown" when {
      "the requested answer service returns Unknown" in new mockOtherServiceApartFromP60 {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck, _: AnswerDetails)(_: Request[_], _: HeaderCarrier)).expects(*, *, *, *)
          .returning(Future.successful(Seq(QuestionResult(PaymentToDate, Unknown))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheckForP60))
        result shouldBe Seq(QuestionResult(PaymentToDate, Unknown))
      }
    }

    "return a Future of QuestionResult with a result of Correct" when {
      "the requested answer service returns Correct" in new mockOtherServiceApartFromP60 {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck, _: AnswerDetails)(_: Request[_], _: HeaderCarrier)).expects(*, *, *, *)
          .returning(Future.successful(Seq(QuestionResult(PaymentToDate, Correct))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheckForP60))
        result shouldBe Seq(QuestionResult(PaymentToDate, Correct))
      }
    }

    "return a Future of QuestionResult with a result of Incorrect" when {
      "the requested answer service returns Correct" in new mockOtherServiceApartFromP60 {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        (mockP60AnswerService.checkAnswers(_: AnswerCheck, _: AnswerDetails)(_: Request[_], _: HeaderCarrier)).expects(*, *, *, *)
          .returning(Future.successful(Seq(QuestionResult(PaymentToDate, Incorrect))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheckForP60))
        result shouldBe Seq(QuestionResult(PaymentToDate, Incorrect))
      }
    }

    "Throw a RuntimeException" when {
      "no supported answer service found for questionKey" in new mockOtherServiceApartFromP60 {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheckForP60)
        }
      }

      /** can't be easily tested until we have multiple evidence sources **/
      "multiple supporting answer services found for questionKey" in new mockOtherServiceApartFromP60 {
        (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(PaymentToDate))
        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheckForP60)
        }
      }
    }
  }

  "calling checkAnswers for Ntc" should {
    "return a Future of QuestionResult with a result of Unknown" when {
      "the requested answer service returns Unknown" in new mockOtherServiceApartFromNtc {
        (mockNtcAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(BankAccount))
        (mockNtcAnswerService.checkAnswers(_: AnswerCheck, _: AnswerDetails)(_: Request[_], _: HeaderCarrier)).expects(*, *, *, *)
          .returning(Future.successful(Seq(QuestionResult(BankAccount, Unknown))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheckForNtc))
        result shouldBe Seq(QuestionResult(BankAccount, Unknown))
      }
    }

    "return a Future of QuestionResult with a result of Correct" when {
      "the requested answer service returns Correct" in new mockOtherServiceApartFromNtc {
        (mockNtcAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(BankAccount))
        (mockNtcAnswerService.checkAnswers(_: AnswerCheck, _: AnswerDetails)(_: Request[_], _: HeaderCarrier)).expects(*, *, *, *)
          .returning(Future.successful(Seq(QuestionResult(BankAccount, Correct))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheckForNtc))
        result shouldBe Seq(QuestionResult(BankAccount, Correct))
      }
    }

    "return a Future of QuestionResult with a result of Incorrect" when {
      "the requested answer service returns Correct" in new mockOtherServiceApartFromNtc {
        (mockNtcAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(BankAccount))
        (mockNtcAnswerService.checkAnswers(_: AnswerCheck, _: AnswerDetails)(_: Request[_], _: HeaderCarrier)).expects(*, *, *, *)
          .returning(Future.successful(Seq(QuestionResult(BankAccount, Incorrect))))
        val result: Seq[QuestionResult] = await(service.checkAnswers(answerCheckForNtc))
        result shouldBe Seq(QuestionResult(BankAccount, Incorrect))
      }
    }

    "Throw a RuntimeException" when {
      "no supported answer service found for questionKey" in new mockOtherServiceApartFromNtc {
        (mockNtcAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq.empty[QuestionKey])
        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheckForNtc)
        }
      }

      /** can't be easily tested until we have multiple evidence sources **/
      "multiple supporting answer services found for questionKey" in new mockOtherServiceApartFromNtc {
        (mockNtcAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq(BankAccount))
        an[RuntimeException] shouldBe thrownBy {
          service.checkAnswers(answerCheckForNtc)
        }
      }
    }
  }

  trait SetUp {
    implicit val request: Request[_] = FakeRequest()
    val mockP60AnswerService: P60AnswerService = mock[P60AnswerService]
    val mockSAAnswerService: SAAnswerService = mock[SAAnswerService]
    val mockPayslipAnswerService: PayslipAnswerService = mock[PayslipAnswerService]
    val mockEmpRefAnswerService: EmpRefAnswerService = mock[EmpRefAnswerService]
    val mockNtcAnswerService: NtcAnswerService = mock[NtcAnswerService]
    val mockVatReturnsAnswerService: VatReturnsAnswerService = mock[VatReturnsAnswerService]
    val service = new AnswerVerificationService(mockP60AnswerService, mockSAAnswerService, mockPayslipAnswerService, mockNtcAnswerService, mockEmpRefAnswerService, mockVatReturnsAnswerService)

    val answerDetailsForP60: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, SimpleAnswer("an answer")))
    val answerCheckForP60: AnswerCheck = AnswerCheck(corrId, answerDetailsForP60, None)

    val answerDetailsForNtc: Seq[AnswerDetails] = Seq(AnswerDetails(BankAccount, SimpleAnswer("an answer")))
    val answerCheckForNtc: AnswerCheck = AnswerCheck(corrId, answerDetailsForNtc, None)
  }

  trait mockOtherServiceApartFromP60 extends SetUp {
    (mockSAAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
    (mockPayslipAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
    (mockEmpRefAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
    (mockVatReturnsAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
    (mockNtcAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
  }

  trait mockOtherServiceApartFromNtc extends SetUp {
    (mockSAAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
    (mockPayslipAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
    (mockEmpRefAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
    (mockVatReturnsAnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
    (mockP60AnswerService.supportedQuestions _: () => Seq[QuestionKey]).expects().returning(Seq())
  }
}
