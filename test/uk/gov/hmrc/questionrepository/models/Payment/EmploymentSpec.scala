/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.Payment

import Utils.UnitSpec
import Utils.testData.P60TestData
import play.api.libs.json.JsSuccess

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

class EmploymentSpec extends UnitSpec {

  "deserializing valid json" should {
    "create an P60Response object" in new Setup {
      p60ResponseJson.validate[P60Response] shouldBe JsSuccess(p60Response)
    }

    "create a P60Response with no Payments if payments not in json" in new Setup {
      p60ResponseWithEmployerWithoutPaymentsJson.validate[P60Response] shouldBe JsSuccess(p60ResponseWithEmployerWithoutPayments)
    }

    "create a P60Response with no Employments if employments not in json" in new Setup {
      p60ResponseWithoutEmploymentsJson.validate[P60Response] shouldBe JsSuccess(p60ResponseWithoutEmployments)
    }

    "create a P60Response without optional fields if those fields are not in json" in new Setup {
      p60ResponseWithoutOptionalFieldsJson.validate[P60Response] shouldBe JsSuccess(p60ResponseWithoutOptionalFields)
    }

    "calling 'paymentsByDateDescending' on an Employment results in sequence of payments in date order newest to oldest" in new Setup {
      val p60 = p60ResponseWithoutOptionalFieldsJson.validate[P60Response]
      p60 shouldBe JsSuccess(p60ResponseWithoutOptionalFields)
      p60.get.p60Response.head.payments shouldBe Seq(paymentSix, paymentSeven)
      p60.get.p60Response.head.paymentsByDateDescending shouldBe Seq(paymentSeven, paymentSix)

    }
  }

  trait Setup extends P60TestData {
    val paymentOne = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(0), Some(34.82), Some(10), None)
    val paymentTwo = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(3000), Some(34.82), Some(11), Some(5))
    val paymentThree = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(1200), None, Some(8), None)
    val paymentFour = Payment(LocalDate.parse("2014-05-30", ISO_LOCAL_DATE), Some(1266), None, Some(10), None)
    val p60Response = P60Response(Seq(Employment(Seq(paymentOne)), Employment(Seq(paymentTwo)), Employment(Seq(paymentThree, paymentFour))))

    val paymentFive = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(0), None, Some(0), None)
    val p60ResponseWithEmployerWithoutPayments = P60Response(Seq(Employment(Seq(paymentFive)), Employment(Seq())))

    val p60ResponseWithoutEmployments = P60Response(Seq())

    val paymentSix = Payment(LocalDate.parse("2015-06-24", ISO_LOCAL_DATE), Some(1333.33), None, Some(90), None)
    val paymentSeven = Payment(LocalDate.parse("2015-06-25", ISO_LOCAL_DATE), Some(4000), Some(34.82), Some(90), Some(5))
    val p60ResponseWithoutOptionalFields = P60Response(Seq(Employment(Seq(paymentSix, paymentSeven))))
  }
}
