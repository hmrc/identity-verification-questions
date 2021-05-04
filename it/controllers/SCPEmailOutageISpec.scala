/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers

import ch.qos.logback.classic.Level
import com.github.tomakehurst.wiremock.client.WireMock.{get, okJson, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import iUtils.{BaseISpec, LogCapturing}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.{PassportQuestion, Question, QuestionResponse}

import java.time.LocalDateTime

class SCPEmailOutageISpec extends BaseISpec with LogCapturing {

  val datePast: String = LocalDateTime.now.minusDays(1).toString
  val dateFuture: String = LocalDateTime.now.plusDays(1).toString
  override def extraConfig: Map[String, Any] = {
    super.extraConfig ++ Map("microservice.services.scpEmailService.disabled.start" -> datePast,
      "microservice.services.scpEmailService.disabled.end" -> dateFuture)
  }

  "POST /questions for disabled service" should {
    "return 200 and a sequence of non scpEmail responses if scpEmail service is within outage window" in new Setup {
      p60ProxyReturnOk(validQuestionRequest)
      withCaptureOfLoggingFrom[AppConfig]{logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions should not contain passportQuestion
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled scpEmailService outage between $datePast and $dateFuture") shouldBe 1
      }
    }
  }

  trait Setup{
    val passportQuestion: Question = Question(PassportQuestion, Seq())
    val questionRoute = "/question-repository/questions"

    def p60ProxyReturnOk(payments: JsValue): StubMapping =
      stubFor(
        get(
          urlMatching("/rti/individual/payments/nino/AA000000/tax-year/([0-9]{2}+(-[0-9]{2}))"))
          .willReturn(
            okJson(
              Json.toJson(payments).toString()
            )
          )
      )

    val validQuestionRequest: JsObject = Json.obj(
      "origin" -> "lost-credentials",
      "identifiers" -> Json.arr(Json.obj("nino" -> "AA000000A")),
      "max" -> 3,
      "min" -> 1
    )
  }
}
