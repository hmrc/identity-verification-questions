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

package uk.gov.hmrc.identityverificationquestions.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.identityverificationquestions.models.P60.{EmployeeNIContributions, PaymentToDate}

class QuestionResponseSpec extends UnitSpec {

  "when creating a QuestionResponse it " should {
    "allow valid inputs" in new Setup{
      questionResponse.correlationId.toString shouldBe correlationId.toString
      questionResponse.questions shouldBe questions
    }

    "serialize a valid QuestionResponse object without optional fields" in new Setup {
      Json.toJson(questionResponse).toString shouldBe s"""{"correlationId":"$correlationId","questions":[{"questionKey":"rti-p60-payment-for-year","info":{"currentTaxYear":"2019/20"}},{"questionKey":"rti-p60-employee-ni-contributions","info":{"currentTaxYear":"2019/20"}},{"questionKey":"passport","info":{}}]}""".stripMargin
    }

    "deserialize valid json into a QuestionResponse" in new Setup {
      val validQuestionResponseStr = s"""{"correlationId":"$correlationId","questions":[{"questionKey":"rti-p60-payment-for-year","info":{"currentTaxYear":"2019/20"}},
                                        |{"questionKey":"rti-p60-employee-ni-contributions","info":{"currentTaxYear":"2019/20"}},
                                        |{"questionKey":"passport","answers":[],"info":{}}]}""".stripMargin
      val json: JsValue = Json.parse(validQuestionResponseStr)
      json.validate[QuestionResponse] shouldBe JsSuccess(QuestionResponse(correlationId, questions))
    }

    "error when attempting to deserialize invalid json" in {
      val invalidQuestionResponseStr = s"""{"correlationId":"oh no","questions":[{"questionKey":"PaymentToDate","info":{"currentTaxYear":"2019/20"}},
                                          |{"questionKey":"EmployeeNIContributions","info":{"currentTaxYear":"2019/20"}}]}""".stripMargin
      val json = Json.parse(invalidQuestionResponseStr)
      an[IllegalArgumentException] shouldBe thrownBy {
        json.validate[QuestionResponse]
      }
    }

  }

  trait Setup {
    val correlationId = CorrelationId()
    val paymentToDateQuestion: Question = Question(PaymentToDate, Map("currentTaxYear" -> "2019/20"))
    val employeeNIContributionsQuestion: Question = Question(EmployeeNIContributions, Map("currentTaxYear" -> "2019/20"))
    val passportQuestion: Question = Question(PassportQuestion, Map.empty[String, String])
    val questions = Seq(paymentToDateQuestion, employeeNIContributionsQuestion, passportQuestion)
    val questionTextEn = Map("PaymentToDate" -> "some text")
    val questionResponse: QuestionResponse = QuestionResponse(
      correlationId,
      Seq(paymentToDateQuestion, employeeNIContributionsQuestion, passportQuestion)
    )
  }
}
