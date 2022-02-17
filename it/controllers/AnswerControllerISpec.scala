/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package controllers

import iUtils.BaseISpec
import play.api.libs.json.{JsString, JsSuccess, Json}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.questionrepository.models.P60._
import uk.gov.hmrc.questionrepository.models.identifier._
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import java.time.LocalDateTime

class AnswerControllerISpec extends BaseISpec {

  "POST /answers" should {
    val journeyPath = "/question-repository/answers"
    "return 200 with score unknown" when {
      "questions identifier not passed" in new SetUp {
        questionRepository.store(questionDataCache(correlationId, origin, Seq(ninoIdentifier, utrIdentifier)))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(incorrectNoIdentifier)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultUnknown))
      }
    }

    "return 200 with score correct" when {
      "answer matches correct answer stored against origin and identifier" in new SetUp {
        questionRepository.store(questionDataCache(correlationId, origin, Seq(ninoIdentifier)))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultCorrect))
      }
    }

    "return 200 with score incorrect" when {
      "answer does not match correct answer stored against origin and identifier" in new SetUp {
        questionRepository.store(questionDataCache(correlationId, origin, Seq(ninoIdentifier)))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(incorrectAnswerCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultIncorrect))
      }

      "correct identifier not passed" in new SetUp {
        questionRepository.store(questionDataCache(correlationId, origin, Seq(ninoIdentifier, utrIdentifier)))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(incorrectIdentifierCheck)))
        response.status shouldBe OK
        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResultIncorrect))
      }
    }
      //ver-1281: SCPEmaildisable for now
//    "return 200 with score correct for SCPEmail questions" when {
//      "answer matches correct answer stored against origin and identifier" in new SCPEmailTestData {
//        questionRepository.store(questionDataCache(correlationId, origin, Seq(ninoIdentifier), scpQuestions))
//        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(scpAnswerCheck)))
//        response.status shouldBe OK
//        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(scpQuestionResultCorrect))
//      }
//    }

//    "return 200 with score incorrect for SCPEmail questions" when {
//      "answer does not match correct answer stored against origin and identifier" in new SCPEmailTestData {
//        questionRepository.store(questionDataCache(correlationId, origin, Seq(ninoIdentifier), scpQuestions))
//        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(scpIncorrectAnswerCheck)))
//        response.status shouldBe OK
//        response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(scpQuestionResultIncorrect))
//      }
//    }

    "return 404" when {
      "no questions found" in new SetUp {
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
        response.status shouldBe NOT_FOUND
      }

      "no questions found for requested origin" in new SetUp {
        questionRepository.store(questionDataCache(correlationId, Origin("wrong_origin"), Seq(ninoIdentifier)))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
        response.status shouldBe NOT_FOUND
      }

      "no questions found for requested identifier" in new SetUp {
        questionRepository.store(questionDataCache(correlationId, origin, Seq(utrIdentifier)))
        val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
        response.status shouldBe NOT_FOUND
      }
    }

    "return 400 for a bad request" in {
      val response: WSResponse = await(resourceRequest(journeyPath).post(JsString("")))
      response.status shouldBe 400
    }

    trait SetUp {
      val correlationId = CorrelationId()
      val origin: Origin = Origin("valid_string")
      val ninoIdentifier: Identifier = NinoI("AA000000D")
      val ninoIdentifier2: Identifier = NinoI("AA000002D")
      val utrIdentifier: Identifier = SaUtrI("123456789")
      val answerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, StringAnswer("3000.00")))
      val answerCheck: AnswerCheck = AnswerCheck(correlationId, origin, Seq(ninoIdentifier), answerDetails)
      val incorrectAnswerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, StringAnswer("666.00")))
      val incorrectAnswerCheck: AnswerCheck = AnswerCheck(correlationId, origin, Seq(ninoIdentifier), incorrectAnswerDetails)
      val incorrectNoIdentifier: AnswerCheck = AnswerCheck(correlationId, origin, Seq(utrIdentifier), incorrectAnswerDetails)
      val incorrectIdentifierCheck: AnswerCheck = AnswerCheck(correlationId, origin, Seq(ninoIdentifier2, utrIdentifier), incorrectAnswerDetails)
      val questionResultUnknown: QuestionResult = QuestionResult(PaymentToDate, Unknown)
      val questionResultCorrect: QuestionResult = QuestionResult(PaymentToDate, Correct)
      val questionResultIncorrect: QuestionResult = QuestionResult(PaymentToDate, Incorrect)
      val paymentToDateQuestion: Question = Question(PaymentToDate, Seq("3000.00", "1200.00"), Map("currentTaxYear" -> "2019/20"))
      val employeeNIContributionsQuestion: Question = Question(EmployeeNIContributions, Seq("34.00", "34.00"), Map("currentTaxYear" -> "2019/20"))
      val questions = Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
      def questionDataCache(correlationId: CorrelationId, origin: Origin, identifiers: Seq[Identifier], questionSeq: Seq[Question] = questions) = QuestionDataCache(correlationId,
                                                                                                                            Selection(origin, identifiers),
                                                                                                                            questionSeq,
                                                                                                                            LocalDateTime.now() plusMinutes(1))


      val questionRepository = app.injector.instanceOf[QuestionMongoRepository]
    }

    trait SCPEmailTestData extends SetUp {
      val scpAnswerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(SCPEmailQuestion, StringAnswer("email@email.com")))
      val scpAnswerCheck: AnswerCheck = AnswerCheck(correlationId, origin, Seq(ninoIdentifier), scpAnswerDetails)
      val scpQuestions = Seq(Question(SCPEmailQuestion, Seq("email@email.com"), Map.empty[String, String]))
      val scpQuestionResultCorrect: QuestionResult = QuestionResult(SCPEmailQuestion, Correct)
      val scpIncorrectAnswerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(SCPEmailQuestion, StringAnswer("bad-email@bad-email.com")))
      val scpIncorrectAnswerCheck: AnswerCheck = AnswerCheck(correlationId, origin, Seq(ninoIdentifier), scpIncorrectAnswerDetails)
      val scpQuestionResultIncorrect: QuestionResult = QuestionResult(SCPEmailQuestion, Incorrect)
    }

  }
}
