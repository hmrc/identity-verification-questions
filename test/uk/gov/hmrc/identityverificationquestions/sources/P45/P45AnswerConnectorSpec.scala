/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.sources.P45

import Utils.UnitSpec
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.P45.{PaymentToDate, TaxToDate}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class P45AnswerConnectorSpec extends UnitSpec with BeforeAndAfterEach {

  implicit val request: Request[_] = FakeRequest()
  val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(mongoComponent)
  val auditService: AuditService = mock[AuditService]
  val connector = new P45AnswerConnector(mongoRepo, auditService)

  val answerDetailsPaymentToDate: AnswerDetails = AnswerDetails(PaymentToDate, SimpleAnswer("100.11"))
  val answerDetailsTaxToDate: AnswerDetails = AnswerDetails(TaxToDate, SimpleAnswer("50.05"))

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

      "matching TaxToDate result" in {
        val correctQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(TaxToDate, Seq("50.05", "10.10"))), dateTime)
        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, correctQDC, Correct, *, *, *, *)
        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, answerDetailsTaxToDate, None).futureValue shouldBe QuestionResult(TaxToDate, Correct)
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

      "failed matching TaxToDate" in {
        val inCorrectQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(TaxToDate, Seq("200.22", "300.33"))), dateTime)

        (auditService.sendQuestionAnsweredResult(_: AnswerDetails, _: QuestionDataCache, _: Score, _: Option[IvJourney])(_: HeaderCarrier, _: Request[_], _: ExecutionContext))
          .expects(*, inCorrectQDC, Incorrect, *, *, *, *)

        await(mongoRepo.store(inCorrectQDC))
        connector.verifyAnswer(corrId, answerDetailsTaxToDate, None).futureValue shouldBe QuestionResult(TaxToDate, Incorrect)
      }
    }

    "return score of 'Unknown'" when {
      "no answers retrieved from repo" in {
        connector.verifyAnswer(corrId, answerDetailsPaymentToDate, None).futureValue shouldBe QuestionResult(PaymentToDate, Unknown)
      }
    }
  }
}
