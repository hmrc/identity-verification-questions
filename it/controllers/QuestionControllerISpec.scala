/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package controllers

import ch.qos.logback.classic.Level
import iUtils.{BaseISpec, LogCapturing, WireMockStubs}
import iUtils.TestData.P60TestData
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.play.BaseOneServerPerSuite
import play.api.libs.json.{JsObject, JsResult, Json}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.models.{EmployeeNIContributions, Origin, PassportQuestion, PaymentToDate, Question, QuestionResponse, SCPEmailQuestion, Selection}

import java.time.LocalDateTime
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.questionrepository.models.identifier.NinoI
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

class QuestionControllerISpec extends BaseISpec with LogCapturing with BaseOneServerPerSuite {
  "POST /questions" should {
    "return 200 if provided with valid json" in new Setup {
      val questionRepository = app.injector.instanceOf[QuestionMongoRepository]
      p60ProxyReturnOk(p60ResponseJson)
      ivReturnOk
      basGatewayStub
      val response: WSResponse = await(resourceRequest(questionRoute).post(validQuestionRequest))
      response.status shouldBe 200
      val questionResponse: JsResult[QuestionResponse] = Json.parse(response.body).validate[QuestionResponse]
      questionResponse.isSuccess shouldBe true
      questionResponse.get.questions.nonEmpty shouldBe true
      questionResponse.get.questions.map(q => q.questionKey) should contain(paymentToDateQuestion.questionKey)

      //ver-1281: SCPEmail disabled for now
      val mongoResult = questionRepository.findAnswers(questionResponse.get.correlationId, Selection(Origin("lost-credentials"), Seq(NinoI("AA000000A")))).futureValue
      //mongoResult.flatMap(qdc => qdc.questions.flatMap(q => q.answers)).count(_ == "email@email.com") shouldBe 1
    }

//    "return 200 and a sequence of non p60 question if provided with valid json but P60 returns not found" in new Setup {
//      p60ProxyReturnNotFound
//      ivReturnOk
//      basGatewayStub
//      withCaptureOfLoggingFrom[P60Service] { logs =>
//        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
//        response.status shouldBe 200
//        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
//        questionResponse.isSuccess shouldBe true
//        questionResponse.get.questions should not contain paymentToDateQuestion
//        questionResponse.get.questions should not contain employeeNIContributionsQuestion
//        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"p60Service, no records returned for selection, origin: lost-credentials, identifiers: AA000000A") shouldBe 1
//      }
//    }

    "return 200 and an empty sequence of question if provided with valid json but P60 returns error" in new Setup {
      p60ProxyReturnError
      ivReturnOk
      basGatewayStub
      withCaptureOfLoggingFrom[P60Service] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions should not contain paymentToDateQuestion
        questionResponse.get.questions should not contain employeeNIContributionsQuestion
        logs.filter(_.getLevel == Level.ERROR).count(_.getMessage.contains("p60Service, threw exception uk.gov.hmrc.http.Upstream5xxResponse")) shouldBe 1
      }
    }

    "return 200 and an empty sequence of question if provided with valid json but does not contain required identifier" in new Setup {
      val response: WSResponse = await(resourceRequest(questionRoute).post(validQuestionRequestUtr))
      response.status shouldBe 200
      val questionResponse: JsResult[QuestionResponse] = Json.parse(response.body).validate[QuestionResponse]
      questionResponse.isSuccess shouldBe true
      questionResponse.get.questions shouldBe Seq.empty[Question]
    }

    "return 400 if provided with invalid json" in new Setup {
      val response: WSResponse = await(resourceRequest(questionRoute).post(invalidQuestionRequest))
      response.status shouldBe 400
    }
  }
}


class QuestionControllerOutageISpec extends BaseISpec with LogCapturing {

  val datePast: String = LocalDateTime.now.minusDays(1).toString
  val dateFuture: String = LocalDateTime.now.plusDays(1).toString
  override def extraConfig: Map[String, Any] = {
    super.extraConfig ++ Map("microservice.services.p60Service.disabled.start" -> datePast,
      "microservice.services.p60Service.disabled.end" -> dateFuture)
  }

  "POST /questions for disabled service" should {
    "return 200 and a sequence of non P60 responses if P60 service is within outage window" in new Setup {
      ivReturnOk
      basGatewayStub
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions should not contain paymentToDateQuestion
        questionResponse.get.questions should not contain employeeNIContributionsQuestion
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between $datePast and $dateFuture") shouldBe 1
      }
    }
  }
}

class QuestionControllerDisabledOriginISpec extends BaseISpec with LogCapturing {

  val datePast: String = LocalDateTime.now.minusDays(1).toString
  val dateFuture: String = LocalDateTime.now.plusDays(1).toString
  override def extraConfig: Map[String, Any] = {
    super.extraConfig ++ Map("microservice.services.p60Service.disabled.origin.0" -> "lost-credentials")
  }

  "POST /questions for disabled origin" should {
    "return 200 and an empty sequence if P60 service is disabled for origin service" in new Setup {
      ivReturnOk
      basGatewayStub
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions should not contain paymentToDateQuestion
        questionResponse.get.questions should not contain employeeNIContributionsQuestion
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == "Disabled origins for p60Service are [lost-credentials]") shouldBe 1
      }
    }
  }
}

class QuestionControllerEnabledOriginISpec extends BaseISpec with LogCapturing {

  val datePast: String = LocalDateTime.now.minusDays(1).toString
  val dateFuture: String = LocalDateTime.now.plusDays(1).toString
  override def extraConfig: Map[String, Any] = {
    super.extraConfig ++ Map("microservice.services.p60Service.enabled.origin.0" -> "identity-verification")
  }

  "POST /questions for enabled origin" should {
    "return 200 and an empty sequence if origin service in not in P60 service enabled origins" in new Setup {
      ivReturnOk
      basGatewayStub
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions should not contain paymentToDateQuestion
        questionResponse.get.questions should not contain employeeNIContributionsQuestion
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == "Enabled origins for p60Service are [identity-verification]") shouldBe 1
      }
    }
  }
}

class QuestionControllerBeforeOutageISpec extends BaseISpec with LogCapturing {

  val datePast: String = LocalDateTime.now.plusDays(1).toString
  val dateFuture: String = LocalDateTime.now.plusDays(2).toString
  override def extraConfig: Map[String, Any] = {
    super.extraConfig ++ Map("microservice.services.p60Service.disabled.start" -> datePast,
      "microservice.services.p60Service.disabled.end" -> dateFuture)
  }

  "POST /questions for service with scheduled outage" should {
    "return 200 and sequence of questions if outage window is in future" in new Setup {
      p60ProxyReturnOk(p60ResponseJson)
      ivReturnOk
      basGatewayStub
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions.nonEmpty shouldBe true
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between $datePast and $dateFuture") shouldBe 1
      }
    }

    //ver-1281: Passport disabled for now
//    "return 200 and sequence of questions inc Passport" when {
//      "outage window is in future and identifiers includes DOB" in new Setup {
//        p60ProxyReturnOk(p60ResponseJson)
//        ivReturnOk
//        basGatewayStub
//        val response = await(resourceRequest(questionRoute).post(validQuestionRequestNinoDob))
//        response.status shouldBe 200
//        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
//        questionResponse.isSuccess shouldBe true
//        questionResponse.get.questions should contain(passportQuestion)
//      }
//    }
  }
}

class QuestionControllerAfterOutageISpec extends BaseISpec with LogCapturing {

  val datePast: String = LocalDateTime.now.minusDays(2).toString
  val dateFuture: String = LocalDateTime.now.minusDays(1).toString
  override def extraConfig: Map[String, Any] = {
    super.extraConfig ++ Map("microservice.services.p60Service.disabled.start" -> datePast,
      "microservice.services.p60Service.disabled.end" -> dateFuture)
  }

  "POST /questions for service with past outage" should {
    "return 200 and sequence of questions if outage window is in past" in new Setup {
      p60ProxyReturnOk(p60ResponseJson)
      ivReturnOk
      basGatewayStub
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions.nonEmpty shouldBe true
        questionResponse.get.questionTextEn.nonEmpty shouldBe true
        questionResponse.get.questionTextCy.isDefined shouldBe true
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between $datePast and $dateFuture") shouldBe 1
      }
    }
  }

  "Just wait for circuit breaker to reset" in {
    Thread.sleep(2000)
  }
}

trait Setup extends WireMockStubs with TestData {

  val questionRoute = "/question-repository/questions"


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

  val validQuestionRequestNinoDob: JsObject = Json.obj(
    "origin" -> "lost-credentials",
    "identifiers" -> Json.arr(Json.obj("nino" -> "AA000000A"),Json.obj("dob" -> "1986-02-28")),
    "max" -> 3,
    "min" -> 1
  )


  val invalidQuestionRequest: JsObject = Json.obj(
    "origin" -> "lost-credentials",
    "identifiers" -> Json.arr(Json.obj("nino" -> "AA000000A")),
    "max" -> 5,
    "min" -> 8
  )

  val paymentToDateQuestion: Question = Question(PaymentToDate, Seq.empty[String], Map("currentTaxYear" -> "2019/20"))
  val employeeNIContributionsQuestion: Question = Question(EmployeeNIContributions, Seq.empty[String], Map("currentTaxYear" -> "2019/20"))
  val passportQuestion: Question = Question(PassportQuestion, Seq.empty[String])
  val scpEmailQuestion: Question = Question(SCPEmailQuestion, Seq.empty[String], Map.empty[String, String])

  val testQuestions = Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
}
