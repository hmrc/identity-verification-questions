/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs
import uk.gov.hmrc.questionrepository.models.Identifier.NinoI
import uk.gov.hmrc.questionrepository.models.{CorrelationId, Origin, Question, QuestionResponse, Selection}
import uk.gov.hmrc.questionrepository.services.EvidenceRetrievalService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class QuestionControllerSpec extends Utils.UnitSpec {

  "POST /questions" should {
    "return 200 ok" in new Setup {
      when(fakeEvidenceRetrievalService.callAllEvidenceSources(any)(any)).thenReturn(Future.successful(questionResponse))
      val result: Future[Result] = controller.question()(fakeQuestionRequest)
      status(result) shouldBe OK
    }

    "return 400 BadRequest" in new Setup {
      val result: Future[Result] = controller.question()(fakeBadRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

  trait Setup {
    val selection: Selection = Selection(Origin("ma"),Seq(NinoI("AA000000D")),Some(3), Some(1))
    val questionResponse: QuestionResponse = QuestionResponse(CorrelationId(), Seq.empty[Question], Map.empty[String, String], None)
    val jsonBody: JsValue = Json.toJson(selection)
    val badJson: JsValue = Json.parse("""
                            |{
                            |   "origin":{"value":"ma"},
                            |   "selections":[{"nino":"AA000000D"}],
                            |   "max":1,
                            |   "min":3
                            |}
                            |""".stripMargin)

    val fakeQuestionRequest: FakeRequest[JsValue] = FakeRequest().withBody(jsonBody)
    val fakeBadRequest: FakeRequest[JsValue] = FakeRequest().withBody(badJson)
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val fakeEvidenceRetrievalService: EvidenceRetrievalService = mock[EvidenceRetrievalService]
    implicit val mccStub: MessagesControllerComponents = Stubs.stubMessagesControllerComponents()
    val controller = new QuestionController(fakeEvidenceRetrievalService)
  }
}

