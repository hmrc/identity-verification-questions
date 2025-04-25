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

package test.controllers

import ch.qos.logback.classic.Level
import org.scalatestplus.play.BaseOneServerPerSuite
import play.api.libs.json.{JsObject, JsResult, Json}
import play.api.libs.ws.WSResponse
import test.iUtils.{BaseISpec, LogCapturing, WireMockStubs}
import test.iUtils.TestData.RtiTestData
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.P60.{EmployeeNIContributions, PaymentToDate}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Service

import java.time.LocalDateTime

class QuestionControllerISpec extends BaseISpec with LogCapturing with BaseOneServerPerSuite {
  "POST /questions" should {
    "return 200 if provided with valid json with userAgent of IV" in new Setup {
      rtiProxyReturnOk(rtiResponseJson)
      ivReturnOk
      basGatewayStub
      val response: WSResponse = await(resourceRequest(questionRoute).post(validQuestionRequest))
      response.status shouldBe 200
      val questionResponse: JsResult[QuestionResponse] = Json.parse(response.body).validate[QuestionResponse]
      questionResponse.isSuccess shouldBe true
      private val questions: Seq[Question] = questionResponse.get.questions
      questions.nonEmpty shouldBe true
      questions.map(_.questionKey.evidenceOption).contains("P60") shouldBe true
      questions.map(_.questionKey.evidenceOption).contains("P45") shouldBe false
      questions.map(q => q.questionKey) should contain(paymentToDateQuestion.questionKey)
    }

    "return 200 if provided with valid json for P45 with userAgent of nino-IV" in new Setup {
      rtiProxyReturnOk(rtiResponseJson)
      ivReturnOk
      basGatewayStub
      val response: WSResponse = await(resourceRequestP45(questionRoute).post(validQuestionRequest))
      response.status shouldBe 200
      val questionResponse: JsResult[QuestionResponse] = Json.parse(response.body).validate[QuestionResponse]
      questionResponse.isSuccess shouldBe true
      private val questions: Seq[Question] = questionResponse.get.questions
      questions.nonEmpty shouldBe true
      questions.map(_.questionKey.evidenceOption).contains("P60") shouldBe true
      questions.map(_.questionKey.evidenceOption).contains("P45") shouldBe true
      questions.map(q => q.questionKey) should contain(paymentToDateQuestion.questionKey)
    }

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
        logs.filter(_.getLevel == Level.ERROR).count(_.getMessage.contains("p60Service threw Exception, origin: identity-verification; detail: uk.gov.hmrc.http.Upstream5xxResponse")) shouldBe 1
      }
    }

    "return 200 and an empty sequence of question if provided with valid json but does not contain required identifier" in new Setup {
      val response: WSResponse = await(resourceRequest(questionRoute).post(validQuestionRequestUtr))
      response.status shouldBe 200
      val questionResponse: JsResult[QuestionResponse] = Json.parse(response.body).validate[QuestionResponse]
      questionResponse.isSuccess shouldBe true
      questionResponse.get.questions shouldBe Seq.empty[QuestionWithAnswers]
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
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between ${toBTZ(datePast)} and ${toBTZ(dateFuture)}") shouldBe 1
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
      rtiProxyReturnOk(rtiResponseJson)
      ivReturnOk
      basGatewayStub
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions.nonEmpty shouldBe true
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between ${toBTZ(datePast)} and ${toBTZ(dateFuture)}") shouldBe 1
      }
    }
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
      rtiProxyReturnOk(rtiResponseJson)
      ivReturnOk
      basGatewayStub
      withCaptureOfLoggingFrom[AppConfig] { logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions.nonEmpty shouldBe true
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled p60Service outage between ${toBTZ(datePast)} and ${toBTZ(dateFuture)}") shouldBe 1
      }
    }
  }

  "Just wait for circuit breaker to reset" in {
    Thread.sleep(2000)
  }
}

trait Setup extends WireMockStubs with TestData {

  val questionRoute = "/identity-verification-questions/questions"

}

trait TestData extends RtiTestData {

  val validQuestionRequest: JsObject = Json.obj(
    "nino" -> "AA000000A",
  )

  val validQuestionRequestUtr: JsObject = Json.obj(
    "sautr" -> "123456789",
  )

  val validQuestionRequestNinoDob: JsObject = Json.obj(
    "nino" -> "AA000000A",
    "dob" -> "1986-02-28"
  )

  val invalidQuestionRequest: JsObject = Json.obj(
    "foo" -> "bar",
  )

  val paymentToDateQuestion: QuestionWithAnswers = QuestionWithAnswers(PaymentToDate, Seq.empty[String], Map("currentTaxYear" -> "2019/20"))
  val employeeNIContributionsQuestion: QuestionWithAnswers = QuestionWithAnswers(EmployeeNIContributions, Seq.empty[String], Map("currentTaxYear" -> "2019/20"))
  val passportQuestion: QuestionWithAnswers = QuestionWithAnswers(PassportQuestion, Seq.empty[String])
  val scpEmailQuestion: QuestionWithAnswers = QuestionWithAnswers(SCPEmailQuestion, Seq.empty[String], Map.empty[String, String])

  val testQuestions = Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
}
