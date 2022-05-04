/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package controllers

import iUtils.BaseISpec
import play.api.libs.json.{JsString, JsSuccess, Json}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.questionrepository.models.P60._
import uk.gov.hmrc.questionrepository.models._

import java.time.{LocalDateTime, ZoneOffset}

class AnswerControllerISpec extends BaseISpec {

  "POST /answers" should {

    val journeyPath = "/question-repository/answers"

    "return 200 with score unknown" when {

      "questions identifier not passed" in new SetUp {
        await(questionRepository.store(questionDataCache(correlationId, Selection(Some(ninoIdentifier), Some(utrIdentifier), None))))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(incorrectNoIdentifier)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultUnknown))
      }
    }

    "return 200 with score correct" when {
      "answer matches correct answer stored against identifier" in new SetUp {
        await(questionRepository.store(questionDataCache(correlationId, Selection(ninoIdentifier))))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultCorrect))
      }
    }

    "return 200 with score incorrect" when {

      "answer does not match correct answer stored against identifier" in new SetUp {
        await(questionRepository.store(questionDataCache(correlationId, Selection(ninoIdentifier))))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(incorrectAnswerCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultIncorrect))
      }

      "correct identifier not passed" in new SetUp {
        await(questionRepository.store(questionDataCache(correlationId, Selection(Some(ninoIdentifier), Some(utrIdentifier), None))))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(incorrectIdentifierCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultIncorrect))
      }
    }

    "return 404" when {
      "no questions found" in new SetUp {
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
        response.status shouldBe NOT_FOUND
      }

      "no questions found for requested identifier" in new SetUp {
        await(questionRepository.store(questionDataCache(correlationId, Selection(utrIdentifier))))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
        response.status shouldBe NOT_FOUND
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
      val answerCheck: AnswerCheck = AnswerCheck(correlationId, Selection(ninoIdentifier), answerDetails)
      val incorrectAnswerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, SimpleAnswer("666.00")))
      val incorrectAnswerCheck: AnswerCheck = AnswerCheck(correlationId, Selection(ninoIdentifier), incorrectAnswerDetails)
      val incorrectNoIdentifier: AnswerCheck = AnswerCheck(correlationId, Selection(utrIdentifier), incorrectAnswerDetails)
      val incorrectIdentifierCheck: AnswerCheck = AnswerCheck(correlationId, Selection(Some(ninoIdentifier2), Some(utrIdentifier), None), incorrectAnswerDetails)
      val questionResultUnknown: QuestionResult = QuestionResult(PaymentToDate, Unknown)
      val questionResultCorrect: QuestionResult = QuestionResult(PaymentToDate, Correct)
      val questionResultIncorrect: QuestionResult = QuestionResult(PaymentToDate, Incorrect)
      val paymentToDateQuestion: Question = Question(PaymentToDate, Seq("3000.00", "1200.00"), Map("currentTaxYear" -> "2019/20"))
      val employeeNIContributionsQuestion: Question = Question(EmployeeNIContributions, Seq("34.00", "34.00"), Map("currentTaxYear" -> "2019/20"))
      val questions = Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
      def questionDataCache(correlationId: CorrelationId, selection: Selection, questionSeq: Seq[Question] = questions) =
        QuestionDataCache(
          correlationId,
          selection,
          questionSeq,
          LocalDateTime.now(ZoneOffset.UTC) plusMinutes (1))
    }

  }
}
