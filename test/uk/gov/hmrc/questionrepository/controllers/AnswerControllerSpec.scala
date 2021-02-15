/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs
import uk.gov.hmrc.questionrepository.models.Identifier.NinoI
import uk.gov.hmrc.questionrepository.models.{AnswerCheck, AnswerDetails, IntegerAnswer, Origin, QuestionId, QuestionResult, Unknown}
import uk.gov.hmrc.questionrepository.services.AnswersService

import scala.concurrent.{ExecutionContext, Future}

class AnswerControllerSpec() extends Utils.UnitSpec {

  "POST /answers" should {
    "return 200 with a valid json body" in new Setup {
      when(answersService.checkAnswers(eqTo[AnswerCheck](answerCheck))).thenReturn(Future.successful(List(QuestionResult(questionId,Unknown))))
      val result: Future[Result] = controller.answer()(fakeRequest)
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(List(QuestionResult(questionId,Unknown)))
    }
  }

  trait Setup extends TestData {
    val answersService: AnswersService = mock[AnswersService]
    val fakeRequest: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(answerCheck))
    val controller = new AnswerController(answersService)(Stubs.stubMessagesControllerComponents(), ExecutionContext.global)
  }

  trait TestData {
    val origin: Origin = Origin("seiss")
    val ninoIdentifier: NinoI = NinoI("AA000000A")
    val integerAnswer: IntegerAnswer = IntegerAnswer(5)
    val questionId: QuestionId = QuestionId("12345")
    val answerCheck: AnswerCheck = AnswerCheck(origin, Seq(ninoIdentifier), Seq(AnswerDetails(questionId,integerAnswer)))
  }
}
