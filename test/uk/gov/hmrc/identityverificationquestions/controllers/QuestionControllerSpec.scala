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

package uk.gov.hmrc.identityverificationquestions.controllers

import Utils.{LogCapturing, UnitSpec}
import ch.qos.logback.classic.Level
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.{CorrelationId, Question, QuestionResponse, Selection}
import uk.gov.hmrc.identityverificationquestions.services.EvidenceRetrievalService
import uk.gov.hmrc.play.bootstrap.tools.Stubs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class QuestionControllerSpec extends UnitSpec with LogCapturing {

  "POST /questions" should {
    "return 200 ok" in new Setup {
      (fakeEvidenceRetrievalService.callAllEvidenceSources(_: Selection)(_:Request[_],_: HeaderCarrier)).expects(*, *, *).returning(Future.successful(questionResponse))
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
    val selection: Selection = Selection(Nino("AA000000D"))
    val questionResponse: QuestionResponse = QuestionResponse(CorrelationId(), Seq.empty[Question])
    val jsonBody: JsValue = Json.toJson(selection)
    val badJson: JsValue = Json.parse("""
                            |{
                            |   "selections":[{"nino":"AA000000D"}]
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

