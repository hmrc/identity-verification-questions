/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.questionrepository.models.P60.PaymentToDate

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
    val answerCheck: AnswerCheck = AnswerCheck(correlationId, Selection(Nino("AA000000A")), Seq(AnswerDetails(PaymentToDate, SimpleAnswer("the answer"))))
    val validJson: JsValue = Json.parse(s"""{"correlationId":"${correlationId.id}", "selection":{"nino":"AA000000A"},"answers":[{"questionKey":"rti-p60-payment-for-year","answer":"the answer"}]}""")
  }
}
