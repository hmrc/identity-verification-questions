/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.P60.PaymentToDate
import uk.gov.hmrc.identityverificationquestions.models.{AnswerCheck, AnswerDetails, CorrelationId, QuestionDataCache, QuestionResult, QuestionWithAnswers, Selection, SimpleAnswer, Unknown}
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository
import uk.gov.hmrc.identityverificationquestions.services.AnswerVerificationService
import uk.gov.hmrc.play.bootstrap.tools.Stubs

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswerControllerSpec() extends UnitSpec with LogCapturing {

  "POST /answers" should {
    "return 200 with a valid json body" when {
      "origin, correlationId and identifiers match entry in mongo repo" in new Setup {
        await(questionMongoRepository.collection.insertOne(questionDataCache).toFuture())
        (answersService.checkAnswers(_: AnswerCheck)(_: Request[_], _: HeaderCarrier)).expects(answerCheck, *, *).returning(Future.successful(List(QuestionResult(PaymentToDate, Unknown))))
        val result: Future[Result] = controller.answer()(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(List(QuestionResult(PaymentToDate, Unknown)))
        await(questionMongoRepository.collection.drop().toFuture())
      }
    }

    "return 403" when {
      "Unauthorised client called question repository" in new Setup {
        withCaptureOfLoggingFrom[AnswerController] { logs =>
          val result: Future[Result] = controller.answer()(fakeRequestWithUnknownAgent)
          status(result) shouldBe FORBIDDEN
          val infoLogs = logs.filter(_.getLevel == Level.WARN)
          infoLogs.size shouldBe 1
          infoLogs.count(_.getMessage == "Unauthorised client called question repository, User-Agent is: Some(Unknown)") shouldBe 1
        }
      }
    }
  }

  trait Setup extends TestData {
    val answersService: AnswerVerificationService = mock[AnswerVerificationService]
    val questionMongoRepository = new QuestionMongoRepository(reactiveMongoComponent)
    val fakeRequest: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(answerCheck)).withHeaders("User-Agent" -> "identity-verification")
    val fakeRequestWithUnknownAgent: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(answerCheck)).withHeaders("User-Agent" -> "Unknown")
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val controller = new AnswerController(answersService, questionMongoRepository, appConfig)(Stubs.stubMessagesControllerComponents(), global)
  }

  trait TestData {
    val correlationId: CorrelationId = CorrelationId()
    val ninoIdentifier: Nino = Nino("AA000000A")
    val answerCheck: AnswerCheck = AnswerCheck(correlationId, Selection(ninoIdentifier), Seq(AnswerDetails(PaymentToDate, SimpleAnswer("5"))), None)
    val questionDataCache: QuestionDataCache = QuestionDataCache(correlationId, Selection(ninoIdentifier), Seq.empty[QuestionWithAnswers], LocalDateTime.now)
  }
}
