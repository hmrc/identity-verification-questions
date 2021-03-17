/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}

class AnswerDetailsSpec extends UnitSpec {

  "when creating an AnswerDetails object it" should {
    "allow creation with valid QuestionId and StringAnswer objects" in new Setup {
      answerDetailsStr.questionKey shouldBe PaymentToDate
      answerDetailsStr.answer.toString shouldBe "an answer"
    }

    "allow creation with valid QuestionId and IntegerAnswer objects" in new Setup {
      answerDetailsInt.questionKey shouldBe PaymentToDate
      answerDetailsInt.answer.toString shouldBe "500"
    }

    "allow creation with valid QuestionId and DoubleAnswer objects" in new Setup {
      answerDetailsDbl.questionKey shouldBe PaymentToDate
      answerDetailsDbl.answer.toString shouldBe "500.12"
    }

    "allow creation with valid QuestionId and BooleanAnswer objects" in new Setup {
      answerDetailsBln.questionKey shouldBe PaymentToDate
      answerDetailsBln.answer.toString shouldBe "true"
    }

//    "not allow if QuestionId value is invalid" in {
//      an[IllegalArgumentException] shouldBe thrownBy {
//        AnswerDetails(QuestionId("1234"), StringAnswer("an answer"))
//      }
//    }

    "not allow if answer value is invalid" in {
      an[IllegalArgumentException] shouldBe thrownBy {
        AnswerDetails(PaymentToDate, IntegerAnswer("an answer"))
      }
    }

    "serialize to json with StringAnswer" in new Setup {
      val json: JsValue = Json.toJson(answerDetailsStr)
      json shouldBe jsonStringAnswer
    }

    "serialize to json with IntegerAnswer" in new Setup {
      val json: JsValue = Json.toJson(answerDetailsInt)
      json shouldBe jsonIntegerAnswer
    }

    "serialize to json with DoubleAnswer" in new Setup {
      val json: JsValue = Json.toJson(answerDetailsDbl)
      json shouldBe jsonDoubleAnswer
    }

    "serialize to json with BooleanAnswer" in new Setup {
      val json: JsValue = Json.toJson(answerDetailsBln)
      json shouldBe jsonBooleanAnswer
    }

    "deserialize from json with StringAnswer" in new Setup {
      jsonStringAnswer.validate[AnswerDetails] shouldBe JsSuccess(answerDetailsStr)
    }

    "deserialize from json with IntegerAnswer" in new Setup {
      jsonIntegerAnswer.validate[AnswerDetails] shouldBe JsSuccess(answerDetailsInt)
    }

    "deserialize from json with DoubleAnswer" in new Setup {
      jsonDoubleAnswer.validate[AnswerDetails] shouldBe JsSuccess(answerDetailsDbl)
    }

    "deserialize from json with BooleanAnswer" in new Setup {
      jsonBooleanAnswer.validate[AnswerDetails] shouldBe JsSuccess(answerDetailsBln)
    }
  }

  trait Setup {
    val answerDetailsStr: AnswerDetails = AnswerDetails(PaymentToDate, StringAnswer("an answer"))
    val answerDetailsInt: AnswerDetails = AnswerDetails(PaymentToDate, IntegerAnswer(500))
    val answerDetailsDbl: AnswerDetails = AnswerDetails(PaymentToDate, DoubleAnswer(500.12))
    val answerDetailsBln: AnswerDetails = AnswerDetails(PaymentToDate, BooleanAnswer(true))
    val jsonStringAnswer: JsValue = Json.parse(s"""{"questionKey":"PaymentToDate","answer":"an answer"}""")
    val jsonIntegerAnswer: JsValue = Json.parse(s"""{"questionKey":"PaymentToDate","answer":500}""")
    val jsonDoubleAnswer: JsValue = Json.parse(s"""{"questionKey":"PaymentToDate","answer":500.12}""")
    val jsonBooleanAnswer: JsValue = Json.parse(s"""{"questionKey":"PaymentToDate","answer":true}""")
  }
}
