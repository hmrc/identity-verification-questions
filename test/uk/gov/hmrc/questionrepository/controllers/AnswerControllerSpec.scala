/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs
import uk.gov.hmrc.questionrepository.models.Identifier.NinoI
import uk.gov.hmrc.questionrepository.models.{AnswerCheck, AnswerDetails, IntegerAnswer, Origin, PaymentToDate, QuestionResult, Unknown}
import uk.gov.hmrc.questionrepository.services.AnswerVerificationService

import scala.concurrent.{ExecutionContext, Future}

class AnswerControllerSpec() extends Utils.UnitSpec {

  "POST /answers" should {
    "return 200 with a valid json body" in new Setup {
      when(answersService.checkAnswers(eqTo[AnswerCheck](answerCheck))(any)).thenReturn(Future.successful(List(QuestionResult(PaymentToDate, Unknown))))
      val result: Future[Result] = controller.answer()(fakeRequest)
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(List(QuestionResult(PaymentToDate, Unknown)))
    }
  }

  trait Setup extends TestData {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
    val answersService: AnswerVerificationService = mock[AnswerVerificationService]
    val fakeRequest: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(answerCheck))
    val controller = new AnswerController(answersService)(Stubs.stubMessagesControllerComponents(), ExecutionContext.global)
  }

  trait TestData {
    val origin: Origin = Origin("seiss")
    val ninoIdentifier: NinoI = NinoI("AA000000A")
    val integerAnswer: IntegerAnswer = IntegerAnswer(5)
    val answerCheck: AnswerCheck = AnswerCheck(origin, Seq(ninoIdentifier), Seq(AnswerDetails(PaymentToDate, integerAnswer)))
  }
}
