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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import Utils.UnitSpec
import org.scalatest.LoneElement
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.{Configuration, Logging}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.SelfAssessment.{SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion}
import uk.gov.hmrc.identityverificationquestions.models.{AnswerDetails, Correct, CorrelationId, Incorrect, QuestionDataCache, QuestionWithAnswers, Selection, SimpleAnswer}
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.ExecutionContext.Implicits.global

class SAAnswerConnectorSpec extends UnitSpec with Eventually with LogCapturing with LoneElement with Logging {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(500, Millis)))

  "Validate SA Payment Answer" should {
    "return Correct if the correct data is passed in" in new Setup {

      val answer = """{"amount":15.51,"paymentDate":"2017-06-01"}"""
      val result = service.checkResult(questionDataCacheForSAPayments, AnswerDetails(testSAPaymentQuestion.questionKey, SimpleAnswer(answer)))
      result shouldBe Correct
    }

    "return Match if the correct amount is used and the payment date supplied is within the tolerance in the past" in new Setup {
      val answer = """{"amount":100,"paymentDate":"2020-02-18"}"""
      val result = service.checkResult(questionDataCacheForSAPayments, AnswerDetails(testSAPaymentQuestion.questionKey, SimpleAnswer(answer)))
      result shouldBe Correct
    }

    "return Match if the correct amount is used and the payment date supplied is within the tolerance in the future" in new Setup {
      val answer = """{"amount":100,"paymentDate":"2020-02-21"}"""
      val result = service.checkResult(questionDataCacheForSAPayments, AnswerDetails(testSAPaymentQuestion.questionKey, SimpleAnswer(answer)))
      result shouldBe Correct
    }

    "return NoMatch if the payment amount is incorrect" in new Setup {
      val answer = """{"amount":11,"paymentDate":"2017-06-01"}"""
      val result = service.checkResult(questionDataCacheForSAPayments, AnswerDetails(testSAPaymentQuestion.questionKey, SimpleAnswer(answer)))
      result shouldBe Incorrect
    }

    "return NoMatch if the payment date is incorrect" in new Setup {
      val answer = """{"amount":15.51,"paymentDate":"2017-03-01"}"""
      val result = service.checkResult(questionDataCacheForSAPayments, AnswerDetails(testSAPaymentQuestion.questionKey, SimpleAnswer(answer)))
      result shouldBe Incorrect
    }

    "return NoMatch if the payment date is just before the tolerance period" in new Setup {
      val answer = """{"amount":100,"paymentDate":"2020-02-16"}"""
      val result = service.checkResult(questionDataCacheForSAPayments, AnswerDetails(testSAPaymentQuestion.questionKey, SimpleAnswer(answer)))
      result shouldBe Incorrect
    }

    "return NoMatch if the payment date is just after the tolerance period" in new Setup {
      val answer = """{"amount":15.51,"paymentDate":"2017-06-05"}"""
      val result = service.checkResult(questionDataCacheForSAPayments, AnswerDetails(testSAPaymentQuestion.questionKey, SimpleAnswer(answer)))
      result shouldBe Incorrect
    }

    "return NoMatch if the payment date is missing" in new Setup {
      val answer = """{"amount":15.51}"""
      val result = service.checkResult(questionDataCacheForSAPayments, AnswerDetails(testSAPaymentQuestion.questionKey, SimpleAnswer(answer)))
      result shouldBe Incorrect
    }
  }

  "Validate SA Pension Answer" should {
    "return Match if the answer exactly matches a valid answer" in new Setup {
      val validAnswers = Seq("111", "1234")

      val result = service.checkResult(questionDataCacheForSAPensions(validAnswers), userAnswer("1234"))
      result shouldBe Correct
    }

    "return Match, ignoring pence, if the answer exactly matches a the pounds in a valid answer" in new Setup {
      val validAnswers = Seq("111", "1234.98")
      val result = service.checkResult(questionDataCacheForSAPensions(validAnswers), userAnswer("1234"))
      result shouldBe Correct
    }

    "return NoMatch, ignoring pence, if the answer doesn't exactly match a valid answer and offset is 0" in new Setup {
      val validAnswers = Seq("1233.98", "1235")
      val result = service.checkResult(questionDataCacheForSAPensions(validAnswers), userAnswer("1234"))
      result shouldBe Incorrect
    }

    "return Match, ignoring pence, if the answer matches a valid answer within offset" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.answerOffset" -> 2
      )
      val validAnswers = Seq("1232.98", "1236")
      val result = service.checkResult(questionDataCacheForSAPensions(validAnswers), userAnswer("1234"))
      result shouldBe Correct
    }

    "return NoMatch, ignoring pence, if the answer doesn't match a valid answer even allowing for offset" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.answerOffset" -> 2
      )
      val validAnswers = Seq("1231.98", "1237")
      val result = service.checkResult(questionDataCacheForSAPensions(validAnswers), userAnswer("1234"))
      result shouldBe Incorrect
    }
  }
  private trait Setup {
    lazy val additionalConfig: Map[String, Any] = Map.empty
    lazy val saPaymentWindowsYears = 4
    private lazy val configData: Map[String, Any] = Map(
      "hods.circuit.breaker.numberOfCallsToTrigger" -> 3,
      "hods.circuit.breaker.unavailablePeriodDurationInSec" -> 15,
      "hods.circuit.breaker.unstablePeriodDurationInSec" -> 30,
      "microservice.services.SelfAssessmentPaymentService.minimumMeoQuestions" -> 1,
      "version" -> "2",
      "sa.payment.window" -> saPaymentWindowsYears,
      "sa.payment.tolerance.future.days" -> 3,
      "sa.payment.tolerance.past.days" -> 3
    ) ++ additionalConfig
    val config = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)
    implicit val appConfig: AppConfig = new AppConfig(config, servicesConfig)

    implicit val request: Request[_] = FakeRequest()
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val testSAPaymentQuestion = QuestionWithAnswers(SelfAssessedPaymentQuestion, Seq(
      """{"amount":100,"paymentDate":"2020-02-20"}""",
      """{"amount":15.51,"paymentDate":"2017-06-01"}"""
    ))

    val selection = Selection(Nino("AA000003D"))
    val questionDataCacheForSAPayments: Seq[QuestionDataCache] = Seq(QuestionDataCache(CorrelationId(), selection, Seq(testSAPaymentQuestion), dateTime))
    def questionDataCacheForSAPensions(validAnswers: Seq[String]): Seq[QuestionDataCache] =
      Seq(QuestionDataCache(CorrelationId(), selection, Seq(QuestionWithAnswers(SelfAssessedIncomeFromPensionsQuestion, validAnswers)), dateTime))
    val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(reactiveMongoComponent)
    val service = new SAAnswerConnector(appConfig, mongoRepo)
  }

  def userAnswer(userAnswerVal: String): AnswerDetails = AnswerDetails(SelfAssessedIncomeFromPensionsQuestion, SimpleAnswer(userAnswerVal))
}
