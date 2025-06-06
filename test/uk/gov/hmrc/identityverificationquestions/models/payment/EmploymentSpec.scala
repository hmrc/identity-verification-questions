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
import Utils.testData.P60TestData
import play.api.libs.json.JsSuccess

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

class EmploymentSpec extends UnitSpec {

  "deserializing valid json" should {
    "create an Seq[Employment] object" in new Setup {
      p60ResponseJson.validate[Seq[Employment]] shouldBe JsSuccess(p60Response)
    }

    "create a Seq[Employment] with no Payments if payments not in json" in new Setup {
      p60ResponseWithEmployerWithoutPaymentsJson.validate[Seq[Employment]] shouldBe JsSuccess(p60ResponseWithEmployerWithoutPayments)
    }

    "create a Seq[Employment] with no Employments if employments not in json" in new Setup {
      p60ResponseWithoutEmploymentsJson.validate[Seq[Employment]] shouldBe JsSuccess(p60ResponseWithoutEmployments)
    }

    "create a Seq[Employment] without optional fields if those fields are not in json" in new Setup {
      p60ResponseWithoutOptionalFieldsJson.validate[Seq[Employment]] shouldBe JsSuccess(p60ResponseWithoutOptionalFields)
    }

    "calling 'paymentsByDateDescending' on an Employment results in sequence of payments in date order newest to oldest" in new Setup {
      val p60 = p60ResponseWithoutOptionalFieldsJson.validate[Seq[Employment]]
      p60 shouldBe JsSuccess(p60ResponseWithoutOptionalFields)
      p60.get.head.payments shouldBe Seq(paymentSix, paymentSeven)
      p60.get.head.paymentsByDateDescending shouldBe Seq(paymentSeven, paymentSix)
    }

    "create an empty Seq when no employment in json" in new Setup {
      p60ResponseWithoutEmploymentJson.validate[Seq[Employment]] shouldBe JsSuccess(Seq())
    }

    "create a Seq[Employment] without payment if fields are not in json" in new Setup {
      p60ResponseWithoutmandatoryMonetaryAmountFieldJson.validate[Seq[Employment]] shouldBe JsSuccess(p60ResponseWithoutMandatoryMonetaryAmountField)
    }
  }

  trait Setup extends P60TestData {
    val paymentOne: Payment = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(0), Some(34.82), Some(10), None, leavingDate = Some(LocalDate.parse("2012-06-22", ISO_LOCAL_DATE)), totalTaxYTD = Some(10.10))
    val paymentTwo: Payment = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(3000), Some(34.82), Some(11), Some(5), totalTaxYTD = Some(11.11))
    val paymentThree: Payment = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(1200), None, Some(8), None, totalTaxYTD = Some(0))
    val paymentFour: Payment = Payment(LocalDate.parse("2014-05-30", ISO_LOCAL_DATE), Some(1266), None, Some(10), None, totalTaxYTD = Some(13.13))
    val p60Response: Seq[Employment] = Seq(Employment(Seq(paymentOne)), Employment(Seq(paymentTwo)), Employment(Seq(paymentThree, paymentFour)))

    val paymentFive: Payment = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(0), None, Some(0), None, leavingDate = Some(LocalDate.parse("2012-06-22", ISO_LOCAL_DATE)), totalTaxYTD = Some(0))
    val p60ResponseWithEmployerWithoutPayments: Seq[Employment] = Seq(Employment(Seq(paymentFive)), Employment(Seq()))

    val p60ResponseWithoutEmployments: Seq[Nothing] = Seq()

    val paymentSix: Payment = Payment(LocalDate.parse("2015-06-24", ISO_LOCAL_DATE), Some(1333.33), None, Some(90), None, totalTaxYTD = Some(90))
    val paymentSeven: Payment = Payment(LocalDate.parse("2015-06-25", ISO_LOCAL_DATE), Some(4000), Some(34.82), Some(90), Some(5), totalTaxYTD = Some(270))
    val paymentEight: Payment = Payment(LocalDate.parse("2015-06-24", ISO_LOCAL_DATE), None, None, None, None)
    val p60ResponseWithoutOptionalFields: Seq[Employment] = Seq(Employment(Seq(paymentSix, paymentSeven)))
    val p60ResponseWithoutMandatoryMonetaryAmountField: Seq[Employment] = Seq(Employment(Seq(paymentEight, paymentSeven)))
  }
}
