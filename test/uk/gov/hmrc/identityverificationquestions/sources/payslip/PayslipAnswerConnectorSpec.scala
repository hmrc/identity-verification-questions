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

package uk.gov.hmrc.identityverificationquestions.sources.payslip

import Utils.UnitSpec
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Payslip.{IncomeTax, NationalInsurance}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class PayslipAnswerConnectorSpec extends UnitSpec with BeforeAndAfterEach {

  implicit val request: Request[_] = FakeRequest()
  val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(mongoComponent)
  val auditService: AuditService = mock[AuditService]
  val connector = new PayslipAnswerConnector(mongoRepo, auditService)

  val answerDetailsIncomeTax: AnswerDetails = AnswerDetails(IncomeTax, SimpleAnswer("100.11"))
  val answerDetailsNI: AnswerDetails = AnswerDetails(NationalInsurance, SimpleAnswer("100.11"))

  override def afterEach(): Unit = {
    super.afterEach()
    await(mongoRepo.collection.drop().toFuture())
  }

  "verifyAnswer" should {
    "return score of 'Correct'" when {
      "answer matches an answer retrieved from repo IncomeTax" in {
        val correctQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier), Seq(QuestionWithAnswers(IncomeTax, Seq("200.22", "100.11"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(answerDetailsIncomeTax, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsIncomeTax, None).futureValue shouldBe QuestionResult(IncomeTax, Correct)
      }
      "answer matches an answer retrieved from repo NationalInsurance" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(NationalInsurance, Seq("200.22", "100.11"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsNI, None).futureValue shouldBe QuestionResult(NationalInsurance, Correct)
      }
    }

    "return score of 'Incorrect'" when {
      "answer does not match an answer retrieved from repo" in {
        val inCorrectQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(IncomeTax, Seq("200.22", "300.33"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, inCorrectQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(inCorrectQDC))
        connector.verifyAnswer(corrId, answerDetailsIncomeTax, None).futureValue shouldBe QuestionResult(IncomeTax, Incorrect)
      }
      "answer matches an answer retrieved from repo NationalInsurance over tolerance" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(NationalInsurance, Seq("200.22", "100.11"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsNI.copy(answer = SimpleAnswer("102.11")), None).futureValue shouldBe QuestionResult(NationalInsurance, Incorrect)
      }
    }

    "return score of 'Unknown'" when {
      "no answers retrieved from repo" in {
        connector.verifyAnswer(corrId, answerDetailsIncomeTax, None).futureValue shouldBe QuestionResult(IncomeTax, Unknown)
      }
    }
  }
}
