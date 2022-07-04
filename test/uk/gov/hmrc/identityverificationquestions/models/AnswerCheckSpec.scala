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
import uk.gov.hmrc.identityverificationquestions.models.P60.PaymentToDate

class AnswerCheckSpec extends UnitSpec {

  "serializing an AnswerCheck object" should {
    "create valid json" in new Setup {
      Json.toJson(answerCheck) shouldBe validJson
    }
  }

  "deserializing valid json" should {
    "create an AnswerCheck object" in new Setup {
      validJson.validate[AnswerCheck] shouldBe JsSuccess(answerCheck)
    }
  }

  trait Setup {
    val correlationId: CorrelationId = CorrelationId()
    val answerCheck: AnswerCheck = AnswerCheck(correlationId, Seq(AnswerDetails(PaymentToDate, SimpleAnswer("the answer"))), None)
    val validJson: JsValue = Json.parse(s"""{"correlationId":"${correlationId.id}","answers":[{"questionKey":"rti-p60-payment-for-year","answer":"the answer"}]}""")
  }
}
