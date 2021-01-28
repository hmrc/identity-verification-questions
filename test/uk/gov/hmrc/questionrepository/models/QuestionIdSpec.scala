/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsError, JsNumber, JsString, JsSuccess, Json}

class QuestionIdSpec extends UnitSpec {

  "when creating a QuestionId" should {
    "allow strings of valid length" in {
      val validString = "abcderfghijk"

      val questionId = QuestionId(validString)
      questionId.value shouldBe validString
    }

    "not allow strings shorter than 5 characters" in {
      val toShort = "1234"

      an[IllegalArgumentException] shouldBe thrownBy {
        QuestionId(toShort)
      }
    }

    "not allow strings loger than 20 characters" in {
      val toLong = "123456789012345678901"

      an[IllegalArgumentException] shouldBe thrownBy {
        QuestionId(toLong)
      }
    }
  }

  "serializing a QuestionId" should {
    "produce valid json" in {
      val validString = "abcderfghijk"
      val questionId = QuestionId(validString)

      Json.toJson(questionId).toString shouldBe s""""$validString""""
    }
  }

  "deserializing a QuestionId json object" should {
    "create a QuestionId object if json is valid" in {
      val validString = "abcderfghijk"
      val json = JsString(validString)

      json.validate[QuestionId] shouldBe JsSuccess(QuestionId(validString))
    }

    "generate a IllegalArgumentException if json value is an invalid string" in {
      val toLong = "123456789012345678901"
      val json = JsString(toLong)

      an[IllegalArgumentException] shouldBe thrownBy {
        json.validate[QuestionId]
      }
    }

    "generate a IllegalArgumentException if json value is no a JsString" in {
      val json = JsNumber(5)

      json.validate[QuestionId] shouldBe JsError("invalid value for questionId")
    }

  }

}
