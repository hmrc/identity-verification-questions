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
import uk.gov.hmrc.questionrepository.models.identifier.NinoI
import uk.gov.hmrc.questionrepository.models.{AnswerCheck, AnswerDetails, CorrelationId, IntegerAnswer, Origin, PaymentToDate, Question, QuestionDataCache, QuestionResult, Selection, Unknown}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import uk.gov.hmrc.questionrepository.services.AnswerVerificationService

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class AnswerControllerSpec() extends Utils.UnitSpec {

  "POST /answers" should {
    "return 200 with a valid json body" when {
      "origin, correlationId and identifiers match entry in mongo repo" in new Setup {
        when(answersService.checkAnswers(eqTo[AnswerCheck](answerCheck))(any)).thenReturn(Future.successful(List(QuestionResult(PaymentToDate, Unknown))))
        when(mockQuestionMongReop.findAnswers(any, any)).thenReturn(Future.successful(List(questionDataCache)))
        val result: Future[Result] = controller.answer()(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(List(QuestionResult(PaymentToDate, Unknown)))
      }
    }

    "return 404" when {
      "origin, correlationId and identifiers do not match entry in mongo repo" in new Setup {
        when(mockQuestionMongReop.findAnswers(any, any)).thenReturn(Future.successful(List.empty[QuestionDataCache]))
        val result: Future[Result] = controller.answer()(fakeRequest)
        status(result) shouldBe NOT_FOUND
      }
    }
  }

  trait Setup extends TestData {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
    val answersService: AnswerVerificationService = mock[AnswerVerificationService]
    val mockQuestionMongReop = mock[QuestionMongoRepository]
    val fakeRequest: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(answerCheck))
    val controller = new AnswerController(answersService, mockQuestionMongReop)(Stubs.stubMessagesControllerComponents(), ExecutionContext.global)
  }

  trait TestData {
    val correlationId: CorrelationId = CorrelationId()
    val origin: Origin = Origin("seiss")
    val ninoIdentifier: NinoI = NinoI("AA000000A")
    val integerAnswer: IntegerAnswer = IntegerAnswer(5)
    val answerCheck: AnswerCheck = AnswerCheck(correlationId, origin, Seq(ninoIdentifier), Seq(AnswerDetails(PaymentToDate, integerAnswer)))
    val questionDataCache: QuestionDataCache = QuestionDataCache(correlationId,Selection(origin, Seq(ninoIdentifier)), Seq.empty[Question], LocalDateTime.now)
  }
}
