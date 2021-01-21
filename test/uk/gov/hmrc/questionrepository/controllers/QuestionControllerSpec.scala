/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs
import uk.gov.hmrc.questionrepository.models.{NinoI, Origin, Selection}

import scala.concurrent.Future

class QuestionControllerSpec extends Utils.UnitSpec {

  "POST /questions" should {
    "return 501 Not Implemented" in new Setup {
      val result: Future[Result] = controller.question()(fakeQuestionRequest)
      status(result) shouldBe NOT_IMPLEMENTED
    }

    "return 400 Not Implemented" in new Setup {
      val result: Future[Result] = controller.question()(fakeBadRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "POST /answers" should {
    "return 501 Not Implemented" in new Setup {
      val result: Future[Result] = controller.answer()(fakeRequest)
      status(result) shouldBe NOT_IMPLEMENTED
    }
  }
  trait Setup {
    val selection: Selection = Selection(Origin("ma"),Seq(NinoI("AA000000D")),Some(3), Some(1))
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
    val controller = new QuestionController()(Stubs.stubMessagesControllerComponents())
  }
}

