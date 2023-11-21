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

package uk.gov.hmrc.identityverificationquestions.sources.P60

import Utils.UnitSpec
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.P60.{EarningsAbovePT, PaymentToDate, PostgraduateLoanDeductions, StatutoryMaternityPay, StudentLoanDeductions}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class P60AnswerConnectorSpec extends UnitSpec with BeforeAndAfterEach {

  implicit val request: Request[_] = FakeRequest()
  val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(mongoComponent)
  val auditService: AuditService = mock[AuditService]
  val connector = new P60AnswerConnector(mongoRepo, auditService)

  val answerDetailsPaymentToDate: AnswerDetails = AnswerDetails(PaymentToDate, SimpleAnswer("100.11"))
  val answerDetailsEarningsAbovePT: AnswerDetails = AnswerDetails(EarningsAbovePT, SimpleAnswer("100.11"))
  val answerDetailsStatutoryMaternityPay: AnswerDetails = AnswerDetails(StatutoryMaternityPay, SimpleAnswer("300.02"))
  val answerDetailsStudentLoans: AnswerDetails = AnswerDetails(StudentLoanDeductions, SimpleAnswer("800"))
  val answerDetailsPostgraduateLoans: AnswerDetails = AnswerDetails(PostgraduateLoanDeductions, SimpleAnswer("300"))

  override def afterEach(): Unit = {
    super.afterEach()
    await(mongoRepo.collection.drop().toFuture())
  }

  "verifyAnswer" should {
    "return score of 'Correct'" when {
      "matching PaymentToDate result" in {
        val correctQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(PaymentToDate, Seq("200.22", "100.11"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsPaymentToDate, None).futureValue shouldBe QuestionResult(PaymentToDate, Correct)
      }
      "matching EarningsAbovePT result when input is £1 more than data stored" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(EarningsAbovePT, Seq("200.22", "100.11"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsEarningsAbovePT.copy(answer = SimpleAnswer("101.11")), None).futureValue shouldBe QuestionResult(EarningsAbovePT, Correct)
      }
      "matching EarningsAbovePT result when input is £1 less than data stored" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(EarningsAbovePT, Seq("200.22", "100.11"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsEarningsAbovePT.copy(answer = SimpleAnswer("99.11")), None).futureValue shouldBe QuestionResult(EarningsAbovePT, Correct)
      }
      "matching StatutoryMaternityPay result" in {
        val correctQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(StatutoryMaternityPay, Seq("200.22", "300.02"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsStatutoryMaternityPay, None).futureValue shouldBe QuestionResult(StatutoryMaternityPay, Correct)
      }
      "matching StudentLoanDeductions result with no pence" in {
        val correctQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(StudentLoanDeductions, Seq("200.22", "800.00"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsStudentLoans, None).futureValue shouldBe QuestionResult(StudentLoanDeductions, Correct)
      }
      "matching PostgraduateLoanDeductions result with no pence" in {
        val correctQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(PostgraduateLoanDeductions, Seq("200.22", "300.00"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsPostgraduateLoans, None).futureValue shouldBe QuestionResult(PostgraduateLoanDeductions, Correct)
      }
    }

    "return score of 'Incorrect'" when {
      "failed matching PaymentToDate" in {
        val inCorrectQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(PaymentToDate, Seq("200.22", "300.33"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, inCorrectQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(inCorrectQDC))
        connector.verifyAnswer(corrId, answerDetailsPaymentToDate, None).futureValue shouldBe QuestionResult(PaymentToDate, Incorrect)
      }
      "failed matching as EarningsAbovePT input is over tolerance" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(EarningsAbovePT, Seq("200.22", "100.11"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsEarningsAbovePT.copy(answer = SimpleAnswer("102.11")), None).futureValue shouldBe QuestionResult(EarningsAbovePT, Incorrect)
      }
      "failed matching as EarningsAbovePT input is under tolerance" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(EarningsAbovePT, Seq("200.22", "100.11"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsEarningsAbovePT.copy(answer = SimpleAnswer("98.11")), None).futureValue shouldBe QuestionResult(EarningsAbovePT, Incorrect)
      }
      "failed matching as StatutoryMaternityPay input has incorrect pence value" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(StatutoryMaternityPay, Seq("200.22", "300.02"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsStatutoryMaternityPay.copy(answer = SimpleAnswer("300.99")), None).futureValue shouldBe QuestionResult(StatutoryMaternityPay, Incorrect)
      }
      "failed matching as StudentLoanDeductions input includes pence" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(StudentLoanDeductions, Seq("200.22", "800.00"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsStudentLoans.copy(answer = SimpleAnswer("800.02")), None).futureValue shouldBe QuestionResult(StudentLoanDeductions, Incorrect)
      }
      "failed matching as PostgraduateLoanDeductions input includes pence" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(PostgraduateLoanDeductions, Seq("200.22", "300.00"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsPostgraduateLoans.copy(answer = SimpleAnswer("300.02")), None).futureValue shouldBe QuestionResult(PostgraduateLoanDeductions, Incorrect)
      }
    }

    "return score of 'Unknown'" when {
      "no answers retrieved from repo" in {
        connector.verifyAnswer(corrId, answerDetailsPaymentToDate, None).futureValue shouldBe QuestionResult(PaymentToDate, Unknown)
      }
    }
  }
}
