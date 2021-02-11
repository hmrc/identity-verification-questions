/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.Payment

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE  //   ISO_LOCAL_DATE_TIME

class PaymentSpec extends UnitSpec {
  "deserializing valid json" should {
    "create an Payment object" in new Setup {
      validPaymentJson.validate[Payment] shouldBe JsSuccess(validPayment)
    }
  }

  trait Setup {
    val validPaymentJson = Json.parse(
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
  val validPayment = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(100.01), Some(34.82), Some(10), Some(12.34))

}
