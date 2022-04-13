/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import Utils.{LogCapturing, UnitSpec}
import ch.qos.logback.classic.Level
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.identifier.NinoI
import uk.gov.hmrc.questionrepository.models.{CorrelationId, Origin, Question, QuestionResponse, Selection}
import uk.gov.hmrc.questionrepository.services.EvidenceRetrievalService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class QuestionControllerSpec extends UnitSpec with LogCapturing {

  "POST /questions" should {
    "return 200 ok" in new Setup {
      (fakeEvidenceRetrievalService.callAllEvidenceSources(_: Selection)(_: HeaderCarrier)).expects(*, *).returning(Future.successful(questionResponse))
      val result: Future[Result] = controller.question()(fakeQuestionRequest)
      status(result) shouldBe OK
    }

    "return 400 BadRequest if the request as bad request body" in new Setup {
      val result: Future[Result] = controller.question()(fakeBadRequest)
      status(result) shouldBe BAD_REQUEST
    }

    "return 403 Forbidden if Unauthorised client called question repository" in new Setup {
      withCaptureOfLoggingFrom[QuestionController] { logs =>
        val result: Future[Result] = controller.question()(fakeRequest)
        status(result) shouldBe FORBIDDEN
        val infoLogs = logs.filter(_.getLevel == Level.WARN)
        infoLogs.size shouldBe 1
        infoLogs.count(_.getMessage == "Unauthorised client called question repository, User-Agent is: Some(Unknown)") shouldBe 1
      }
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

    val fakeQuestionRequest: FakeRequest[JsValue] = FakeRequest().withBody(jsonBody).withHeaders("user-agent" -> "identity-verification")
    val fakeBadRequest: FakeRequest[JsValue] = FakeRequest().withBody(badJson).withHeaders("User-Agent" -> "identity-verification")
    val fakeRequest: FakeRequest[JsValue] = FakeRequest().withBody(jsonBody).withHeaders("User-Agent" -> "Unknown")
    val fakeEvidenceRetrievalService: EvidenceRetrievalService = mock[EvidenceRetrievalService]
    implicit val mccStub: MessagesControllerComponents = Stubs.stubMessagesControllerComponents()
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val controller = new QuestionController(fakeEvidenceRetrievalService, appConfig)
  }
}

