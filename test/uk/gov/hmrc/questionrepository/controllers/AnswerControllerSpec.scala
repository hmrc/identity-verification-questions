/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs

import scala.concurrent.Future

class AnswerControllerSpec extends Utils.UnitSpec {

  "POST /answers" should {
    "return 501 Not Implemented" in new Setup {
      val result: Future[Result] = controller.answer()(fakeRequest)
      status(result) shouldBe NOT_IMPLEMENTED
    }
  }

  trait Setup {
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val controller = new QuestionController()(Stubs.stubMessagesControllerComponents())
  }
}
