/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}

class QuestionResponseSpec extends UnitSpec {

  "when creating a QuestionResponse it " should {
    "allow valid inputs" in new Setup{
      questionResponse.questionId.value shouldBe "valid_question"
      questionResponse.questionId.toString shouldBe "valid_question"
      questionResponse.questionEn shouldBe "this is a valid question"
      questionResponseWithOptionals.questionCy shouldBe Some("this is a valid question in welsh")
      questionResponseWithOptionals.answerType shouldBe Some(STR)
      questionResponseWithOptionals.regex shouldBe Some("[A-Z][A-Z]/d")
    }

    "serialize a valid QuestionResponse object without optional fields" in new Setup {
      Json.toJson(questionResponse).toString shouldBe s"""{"questionId":"valid_question","questionEn":"this is a valid question"}"""
    }

    "serialize a valid QuestionResponse object with all fields" in new Setup {
      Json.toJson(questionResponseWithOptionals).toString shouldBe s"""{"questionId":"valid_question","questionEn":"this is a valid question"""" +
                                                                    s""","questionCy":"this is a valid question in welsh"""" +
                                                                    s""","answerType":"STR","regex":"[A-Z][A-Z]/d"}"""
    }

    "deserialize valid json into a QuestionResponse" in new Setup {
      val validQuestionResponseStr = s"""{"questionId":"valid_question","questionEn":"this is a valid question","answerType":"STR"}"""
      val json: JsValue = Json.parse(validQuestionResponseStr)
      json.validate[QuestionResponse] shouldBe JsSuccess(QuestionResponse(QuestionId("valid_question"),"this is a valid question",None, Some(STR)))
    }

    "error when attempting to deserialize invalid json" in {
      val invalidQuestionResponseStr = s"""{"questionId":"valid_question","questionEn":"this is a valid question","answerType":"WRONG"}"""
      val json = Json.parse(invalidQuestionResponseStr)
      an[IllegalArgumentException] shouldBe thrownBy {
        json.validate[QuestionResponse]
      }
    }
  }

  trait Setup {
    val questionResponse: QuestionResponse = QuestionResponse(
      QuestionId("valid_question"),
      "this is a valid question"
    )
    val questionResponseWithOptionals: QuestionResponse = QuestionResponse(
      QuestionId("valid_question"),
      "this is a valid question",
      Some("this is a valid question in welsh"),
      Some(STR),
      Some("[A-Z][A-Z]/d")
    )
  }
}
