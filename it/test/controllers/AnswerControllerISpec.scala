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

package test.controllers

import play.api.libs.json.{JsString, JsSuccess, Json}
import play.api.libs.ws.WSResponse
import test.iUtils.BaseISpec
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.identityverificationquestions.models.P60.{EmployeeNIContributions, PaymentToDate}
import uk.gov.hmrc.identityverificationquestions.models._

import java.time.{Duration, Instant, LocalDateTime, ZoneOffset}

class AnswerControllerISpec extends BaseISpec {

  "POST /answers" should {

    val journeyPath = "/identity-verification-questions/answers"

    "return 200 with score correct" when {
      "answer matches correct answer stored against identifier" in new SetUp {
        await(identityverificationquestions.store(questionDataCache(correlationId, Selection(ninoIdentifier))))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultCorrect))
      }
    }

    "return 200 with score incorrect" when {

      "answer does not match correct answer stored against identifier" in new SetUp {
        await(identityverificationquestions.store(questionDataCache(correlationId, Selection(ninoIdentifier))))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(incorrectAnswerCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultIncorrect))
      }

      "correct identifier not passed" in new SetUp {
        await(identityverificationquestions.store(questionDataCache(correlationId, Selection(Some(ninoIdentifier), Some(utrIdentifier), None, None))))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(incorrectIdentifierCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultIncorrect))
      }
    }

    "return 400 for a bad request" in {
      val response: WSResponse = await(resourceRequest(journeyPath).post(JsString("")))
      response.status shouldBe 400
    }

    trait SetUp {
      val correlationId: CorrelationId = CorrelationId()
      val ninoIdentifier: Nino = Nino("AA000000D")
      val ninoIdentifier2: Nino = Nino("AA000002D")
      val utrIdentifier: SaUtr = SaUtr("123456789")
      val answerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, SimpleAnswer("3000.00")))
      val answerCheck: AnswerCheck = AnswerCheck(correlationId, answerDetails, None)
      val incorrectAnswerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, SimpleAnswer("666.00")))
      val incorrectAnswerCheck: AnswerCheck = AnswerCheck(correlationId, incorrectAnswerDetails, None)
      val incorrectIdentifierCheck: AnswerCheck = AnswerCheck(correlationId, incorrectAnswerDetails, None)
      val questionResultCorrect: QuestionResult = QuestionResult(PaymentToDate, Correct)
      val questionResultIncorrect: QuestionResult = QuestionResult(PaymentToDate, Incorrect)
      val paymentToDateQuestion: QuestionWithAnswers = QuestionWithAnswers(PaymentToDate, Seq("3000.00", "1200.00"), Map("currentTaxYear" -> "2019/20"))
      val employeeNIContributionsQuestion: QuestionWithAnswers = QuestionWithAnswers(EmployeeNIContributions, Seq("34.00", "34.00"), Map("currentTaxYear" -> "2019/20"))
      val questions = Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
      def questionDataCache(correlationId: CorrelationId, selection: Selection, questionSeq: Seq[QuestionWithAnswers] = questions) =
        QuestionDataCache(
          correlationId,
          selection,
          questionSeq,
          Instant.now().plus(Duration.ofMinutes(1)))
    }

  }
}
