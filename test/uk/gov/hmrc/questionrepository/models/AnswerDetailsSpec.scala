/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.questionrepository.models.P60.PaymentToDate

class AnswerDetailsSpec extends UnitSpec {

  "when creating an AnswerDetails object it" should {

    "allow creation with valid QuestionId and StringAnswer objects" in new Setup {
      answerDetailsStr.questionKey shouldBe PaymentToDate
      answerDetailsStr.answer.toString shouldBe "an answer"
    }

    "serialize to json with StringAnswer" in new Setup {
      val json: JsValue = Json.toJson(answerDetailsStr)
      json shouldBe jsonStringAnswer
    }

    "deserialize from json with StringAnswer" in new Setup {
      jsonStringAnswer.validate[AnswerDetails] shouldBe JsSuccess(answerDetailsStr)
    }

  }

  trait Setup {
    val answerDetailsStr: AnswerDetails = AnswerDetails(PaymentToDate, SimpleAnswer("an answer"))
    val jsonStringAnswer: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","answer":"an answer"}""")
    val jsonIntegerAnswer: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","answer":500}""")
    val jsonDoubleAnswer: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","answer":500.12}""")
    val jsonBooleanAnswer: JsValue = Json.parse(s"""{"questionKey":"rti-p60-payment-for-year","answer":true}""")
  }
}
