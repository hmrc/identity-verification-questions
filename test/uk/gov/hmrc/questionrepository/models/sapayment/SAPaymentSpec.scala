/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.sapayment


import Utils.UnitSpec
import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.questionrepository.evidences.sources.sa.SAPayment

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
