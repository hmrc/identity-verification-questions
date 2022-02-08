/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.payment

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
