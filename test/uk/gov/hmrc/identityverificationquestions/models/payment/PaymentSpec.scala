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
import play.api.libs.json.{JsSuccess, JsValue, Json}

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE  //   ISO_LOCAL_DATE_TIME

class PaymentSpec extends UnitSpec {

  val validPayment: Payment = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(100.01), Some(34.82), Some(10), Some(12.34), leavingDate = Some(LocalDate.parse("2012-06-22", ISO_LOCAL_DATE)), totalTaxYTD = Some(10.10))

  "deserializing valid json" should {
    "create an Payment object" in new Setup {
      validPaymentJson.validate[Payment] shouldBe JsSuccess(validPayment)
    }
  }

  trait Setup {
    val validPaymentJson: JsValue = Json.parse(
      """
        |{
        |  "payId": "20425",
        |  "leavingDate": "2012-06-22",
        |  "payFreq": "IO",
        |  "mandatoryMonetaryAmount": [
        |    {
        |      "type": "TaxablePayYTD",
        |      "amount": 100.01
        |    },
        |    {
        |      "type": "TotalTaxYTD",
        |      "amount": 10.1
        |    },
        |    {
        |      "type": "TaxablePay",
        |      "amount": 102.02
        |    },
        |    {
        |      "type": "TaxDeductedOrRefunded",
        |      "amount": 10
        |    }
        |  ],
        |  "niLettersAndValues": [
        |    {
        |      "niFigure": [
        |        {
        |          "type": "EmpeeContribnsYTD",
        |          "amount": 34.82
        |        },
        |        {
        |          "type": "EmpeeContribnsInPd",
        |          "amount": 12.34
        |        }
        |      ]
        |    }
        |  ],
        |  "starter": {
        |    "startDate": "2011-08-13"
        |  },
        |  "pmtDate": "2014-06-28",
        |  "rcvdDate": "2015-04-06",
        |  "taxYear": "14-15"
        |}
        |""".stripMargin
    )
  }
}
