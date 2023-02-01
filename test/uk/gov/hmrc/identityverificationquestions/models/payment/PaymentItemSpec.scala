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

package uk.gov.hmrc.identityverificationquestions.models.payment

import Utils.UnitSpec
import play.api.libs.json.{JsValue, Json}

class PaymentItemSpec extends UnitSpec {
  "deserializing valid json" should {
    "create an NiLettersAndValues object" in new Setup {
      validNiLettersAndValuesJson.validate[NiLettersAndValues]
    }
  }

  trait Setup {
    val validNiLettersAndValuesJson: JsValue = Json.parse("""{"niFigure":[{"type":"type1","amount":100.00}]}""")
    val validPaymentItem = PaymentItem("type1", 100.00)
    val validNiLetterAndValues = NiLettersAndValues(Seq(validPaymentItem))
  }

}
