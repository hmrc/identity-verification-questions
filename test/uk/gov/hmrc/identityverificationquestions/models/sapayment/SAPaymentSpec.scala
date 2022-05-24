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

package uk.gov.hmrc.identityverificationquestions.models.sapayment

import Utils.UnitSpec
import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAPayment

class SAPaymentSpec extends UnitSpec {
  "deserializing valid json" should {
    "create an sa payment object" in new Setup {
      validSaPaymentJson.validate[SAPayment].get shouldBe validPayment
      validSaPaymentJson2.validate[Seq[SAPayment]].get shouldBe List(validPayment)
    }
  }

  trait Setup {

    val validPayment: SAPayment = SAPayment(100.01, Some(LocalDate.parse("2014-06-28")), Some("PYT"))

    val validSaPaymentJson: JsValue = Json.parse(
      """
        |{
        |  "amount":
        |    {
        |      "amount": 100.01
        |    },
        |  "createdDate": "2014-06-28",
        |  "transactionCode":"PYT"
        |}
        |""".stripMargin
    )

    val validSaPaymentJson2: JsValue = Json.parse(
      """
        |{
        | "paymentsList":[
        |   {
        |     "createdDate":"2014-06-28",
        |     "transactionCode":"PYT",
        |     "amount":{"amount":100.01,"currency":"GBP"},
        |     "transactionId":{"tieBreaker":9534,"creationDate":"2014-06-28"},
        |     "taxYearEnd":"2021-04-05"
        |   }
        | ]
        |}
        |
        |""".stripMargin
    )

  }
}
