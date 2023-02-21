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

class VatReturnSubmissionSpec extends UnitSpec {
  "deserializing VatReturnSubmission json" should {
    "create an VatReturnSubmission object" in new Setup {
      validVatReturnSubmissionJson.validate[VatReturnSubmission] shouldBe JsSuccess(validVatReturnSubmission)
    }
  }

  trait Setup {
    val validVatReturnSubmissionJson: JsValue = Json.parse(
      """{
        |"periodKey":"22YA",
        |"vatDueSales": 1000,
        |"vatDueAcquisitions": 1000,
        |"vatDueTotal": 1000,
        |"vatReclaimedCurrPeriod": 1000,
        |"vatDueNet": 1000,
        |"totalValueSalesExVAT": 1000,
        |"totalValuePurchasesExVAT": 500.50,
        |"totalValueGoodsSuppliedExVAT": 1000,
        |"totalAllAcquisitionsExVAT": 1000
        |}""".stripMargin
    )

    val validVatReturnSubmission: VatReturnSubmission = VatReturnSubmission(
      "22YA",
      BigDecimal("1000"),
      BigDecimal("1000"),
      BigDecimal("1000"),
      BigDecimal("1000"),
      BigDecimal("1000"),
      BigDecimal("1000"),
      BigDecimal("500.50"),
      BigDecimal("1000"),
      BigDecimal("1000")
    )
  }
}
