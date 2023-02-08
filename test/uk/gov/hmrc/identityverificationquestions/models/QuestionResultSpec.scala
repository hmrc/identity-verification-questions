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

class QuestionResultSpec extends UnitSpec {

  "when creating a QuestionResult object it" should {
    "allow creation with valid QuestionId and Correct Score" in new Setup {
      questionResultCorrect.questionKey.toString shouldBe "PaymentToDate"
      questionResultCorrect.score.toString shouldBe "correct"
    }

    "allow creation with valid QuestionId and Incorrect Score" in new Setup {
      questionResultIncorrect.questionKey.toString shouldBe "PaymentToDate"
      questionResultIncorrect.score.toString shouldBe "incorrect"
    }

    "allow creation with valid QuestionId and Unknown Score" in new Setup {
      questionResultUnknown.questionKey.toString shouldBe "PaymentToDate"
      questionResultUnknown.score.toString shouldBe "unknown"
    }

    "serialize to json with correct score" in new Setup {
      Json.toJson(questionResultCorrect) shouldBe jsonCorrect
    }

    "serialize to json with incorrect score" in new Setup {
      Json.toJson(questionResultIncorrect) shouldBe jsonIncorrect
    }

    "serialize to json with unknown score" in new Setup {
      Json.toJson(questionResultUnknown) shouldBe jsonUnknown
    }

    "deserialize from json with correct score" in new Setup {
      jsonCorrect.validate[QuestionResult] shouldBe JsSuccess(questionResultCorrect)
    }

    "deserialize from json with incorrect score" in new Setup {
      jsonIncorrect.validate[QuestionResult] shouldBe JsSuccess(questionResultIncorrect)
    }

    "deserialize from json with unknown score" in new Setup {
      jsonUnknown.validate[QuestionResult] shouldBe JsSuccess(questionResultUnknown)
    }
  }
  trait Setup {
    val questionResultCorrect: QuestionResult = QuestionResult(PaymentToDate, Correct)
    val questionResultIncorrect: QuestionResult = QuestionResult(PaymentToDate, Incorrect)
    val questionResultUnknown: QuestionResult = QuestionResult(PaymentToDate, Unknown)
    val jsonCorrect: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","score":"correct"}""")
    val jsonIncorrect: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","score":"incorrect"}""")
    val jsonUnknown: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","score":"unknown"}""")
  }
}
