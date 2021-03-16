package controllers

import ch.qos.logback.classic.Level
import com.github.tomakehurst.wiremock.client.WireMock.{get, notFound, okJson, serverError, stubFor, urlEqualTo}
import iUtils.{BaseISpec, LogCapturing}
import iUtils.TestData.P60TestData
import play.api.libs.json.{JsObject, JsSuccess, JsValue, Json}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.models.{EmployeeNIContributions, PaymentToDate, Question, QuestionResponse}

import java.time.LocalDateTime

class QuestionControllerISpec extends BaseISpec with LogCapturing {
  "POST /questions" should {
    "return 200 if provided with valid json" in new Setup {
      p60ProxyReturnOk(p60ResponseJson)
      val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
      response.status shouldBe 200
      val questionResponse = Json.parse(response.body).validate[QuestionResponse]
      questionResponse.isSuccess shouldBe true
      questionResponse.get.questions shouldBe Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
//      Json.parse(response.body).validate[Seq[Question]] shouldBe JsSuccess(Seq(paymentToDateQuestion, employeeNIContributionsQuestion))
    }

    "return 200 and an empty sequence of question if provided with valid json but P60 returns not found" in new Setup {
      p60ProxyReturnNotFound
      withCaptureOfLoggingFrom[P60Service] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions shouldBe Seq.empty[Question]
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"p60Service, no records returned for selection, origin: lost-credentials, identifiers: AA000000A") shouldBe 1
      }
    }

    "return 200 and an empty sequence of question if provided with valid json but P60 returns error" in new Setup {
      p60ProxyReturnError
      withCaptureOfLoggingFrom[P60Service] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions shouldBe Seq.empty[Question]
        logs.filter(_.getLevel == Level.ERROR).count(_.getMessage.contains("p60Service, threw exception uk.gov.hmrc.http.Upstream5xxResponse")) shouldBe 1
      }
    }

    "return 200 and an empty sequence of question if provided with valid json but does not contain required identifier" in new Setup {
      val response = await(resourceRequest(questionRoute).post(validQuestionRequestUtr))
      response.status shouldBe 200
      val questionResponse = Json.parse(response.body).validate[QuestionResponse]
      questionResponse.isSuccess shouldBe true
      questionResponse.get.questions shouldBe Seq.empty[Question]
    }

    "return 400 if provided with invalid json" in new Setup {
      val response = await(resourceRequest(questionRoute).post(invalidQuestionRequest))
      response.status shouldBe 400
    }
  }
}


class QuestionControllerOutageISpec extends BaseISpec with LogCapturing {

  val datePast = LocalDateTime.now.minusDays(1).toString
  val dateFuture = LocalDateTime.now.plusDays(1).toString
  override def extraConfig = {
    super.extraConfig ++ Map("microservice.services.p60Service.disabled.start" -> datePast,
      "microservice.services.p60Service.disabled.end" -> dateFuture)
  }

  "POST /questions for disabled service" should {
    "return 200 and an empty sequence if P60 service is within outage window" in new Setup {
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions shouldBe Seq.empty[Question]
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between $datePast and $dateFuture") shouldBe 1
      }
    }
  }
}

class QuestionControllerDisabledOriginISpec extends BaseISpec with LogCapturing {

  val datePast = LocalDateTime.now.minusDays(1).toString
  val dateFuture = LocalDateTime.now.plusDays(1).toString
  override def extraConfig = {
    super.extraConfig ++ Map("microservice.services.p60Service.disabled.origin.0" -> "lost-credentials")
  }

  "POST /questions for disabled origin" should {
    "return 200 and an empty sequence if P60 service is disabled for origin service" in new Setup {
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions shouldBe Seq.empty[Question]
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == "Disabled origins for p60Service are [lost-credentials]") shouldBe 1
      }
    }
  }
}

class QuestionControllerEnabledOriginISpec extends BaseISpec with LogCapturing {

  val datePast = LocalDateTime.now.minusDays(1).toString
  val dateFuture = LocalDateTime.now.plusDays(1).toString
  override def extraConfig = {
    super.extraConfig ++ Map("microservice.services.p60Service.enabled.origin.0" -> "identity-verification")
  }

  "POST /questions for enabled origin" should {
    "return 200 and an empty sequence if origin service in not in P60 service enabled origins" in new Setup {
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions shouldBe Seq.empty[Question]
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == "Enabled origins for p60Service are [identity-verification]") shouldBe 1
      }
    }
  }
}

class QuestionControllerBeforeOutageISpec extends BaseISpec with LogCapturing {

  val datePast = LocalDateTime.now.plusDays(1).toString
  val dateFuture = LocalDateTime.now.plusDays(2).toString
  override def extraConfig = {
    super.extraConfig ++ Map("microservice.services.p60Service.disabled.start" -> datePast,
      "microservice.services.p60Service.disabled.end" -> dateFuture)
  }

  "POST /questions for service with scheduled outage" should {
    "return 200 and sequence of questions if outage window is in future" in new Setup {
      p60ProxyReturnOk(p60ResponseJson)
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions shouldBe Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between $datePast and $dateFuture") shouldBe 1
      }
    }
  }
}

class QuestionControllerAfterOutageISpec extends BaseISpec with LogCapturing {

  val datePast = LocalDateTime.now.minusDays(2).toString
  val dateFuture = LocalDateTime.now.minusDays(1).toString
  override def extraConfig = {
    super.extraConfig ++ Map("microservice.services.p60Service.disabled.start" -> datePast,
      "microservice.services.p60Service.disabled.end" -> dateFuture)
  }

  "POST /questions for service with past outage" should {
    "return 200 and sequence of questions if outage window is in past" in new Setup {
      p60ProxyReturnOk(p60ResponseJson)
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions shouldBe Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between $datePast and $dateFuture") shouldBe 1
      }
    }
  }
}

trait Setup extends TestData {

  val questionRoute = "/question-repository/questions"

  def p60ProxyReturnOk(payments: JsValue) =
    stubFor(
      get(
        urlEqualTo("/rti/individual/payments/nino/AA000000/tax-year/19-20"))
        .willReturn(
          okJson(
            Json.toJson(payments).toString()
          )
        )
    )

  def p60ProxyReturnNotFound =
    stubFor(
      get(
        urlEqualTo("/rti/individual/payments/nino/AA000000/tax-year/19-20"))
        .willReturn(
          notFound()
        )
    )

  def p60ProxyReturnError =
    stubFor(
      get(
        urlEqualTo("/rti/individual/payments/nino/AA000000/tax-year/19-20"))
        .willReturn(
          serverError()
        )
    )
}

trait TestData extends P60TestData {

  val validQuestionRequest: JsObject = Json.obj(
    "origin" -> "lost-credentials",
    "identifiers" -> Json.arr(Json.obj("nino" -> "AA000000A")),
    "max" -> 3,
    "min" -> 1
  )

  val validQuestionRequestUtr: JsObject = Json.obj(
    "origin" -> "lost-credentials",
    "identifiers" -> Json.arr(Json.obj("utr" -> "123456789")),
    "max" -> 3,
    "min" -> 1
  )


  val invalidQuestionRequest: JsObject = Json.obj(
    "origin" -> "lost-credentials",
    "identifiers" -> Json.arr(Json.obj("nino" -> "AA000000A")),
    "max" -> 5,
    "min" -> 8
  )

  val paymentToDateQuestion: Question = Question(PaymentToDate, Seq("3000.00", "1266.00"), Map("currentTaxYear" -> "2019/20"))
  val employeeNIContributionsQuestion: Question = Question(EmployeeNIContributions, Seq("34.00", "34.00"), Map("currentTaxYear" -> "2019/20"))

}
