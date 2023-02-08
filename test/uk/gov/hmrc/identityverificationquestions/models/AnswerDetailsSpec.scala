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

package uk.gov.hmrc.identityverificationquestions.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.identityverificationquestions.models.P60.PaymentToDate

class AnswerDetailsSpec extends UnitSpec {

  "when creating an AnswerDetails object it" should {

    "allow creation with valid QuestionId and StringAnswer objects" in new Setup {
      answerDetailsStr.questionKey shouldBe PaymentToDate
      answerDetailsStr.answer.toString shouldBe "an answer"
    }

    "serialize to json with StringAnswer" in new Setup {
      val json: JsValue = Json.toJson(answerDetailsStr)
      json shouldBe jsonStringAnswer
    }

    "deserialize from json with StringAnswer" in new Setup {
      jsonStringAnswer.validate[AnswerDetails] shouldBe JsSuccess(answerDetailsStr)
    }

  }

  trait Setup {
    val answerDetailsStr: AnswerDetails = AnswerDetails(PaymentToDate, SimpleAnswer("an answer"))
    val jsonStringAnswer: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","answer":"an answer"}""")
    val jsonIntegerAnswer: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","answer":500}""")
    val jsonDoubleAnswer: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","answer":500.12}""")
    val jsonBooleanAnswer: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","answer":true}""")
  }
}
