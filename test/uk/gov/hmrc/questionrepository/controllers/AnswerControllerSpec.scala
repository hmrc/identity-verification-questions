/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import Utils.{LogCapturing, UnitSpec}
import ch.qos.logback.classic.Level
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.P60.PaymentToDate
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import uk.gov.hmrc.questionrepository.services.AnswerVerificationService

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class AnswerControllerSpec() extends UnitSpec with LogCapturing {

  "POST /answers" should {
    "return 200 with a valid json body" when {
      "origin, correlationId and identifiers match entry in mongo repo" in new Setup {
        await(questionMongoRepository.collection.insertOne(questionDataCache).toFuture())
        (answersService.checkAnswers(_: AnswerCheck)(_: HeaderCarrier)).expects(answerCheck, *).returning(Future.successful(List(QuestionResult(PaymentToDate, Unknown))))
        val result: Future[Result] = controller.answer()(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(List(QuestionResult(PaymentToDate, Unknown)))
        await(questionMongoRepository.collection.drop().toFuture())
      }
    }

    "return 404" when {
      "origin, correlationId and identifiers do not match entry in mongo repo" in new Setup {
        val result: Future[Result] = controller.answer()(fakeRequest)
        status(result) shouldBe NOT_FOUND
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
    val answerCheck: AnswerCheck = AnswerCheck(correlationId, Selection(ninoIdentifier), Seq(AnswerDetails(PaymentToDate, SimpleAnswer("5"))))
    val questionDataCache: QuestionDataCache = QuestionDataCache(correlationId, Selection(ninoIdentifier), Seq.empty[Question], LocalDateTime.now)
  }
}
