/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, Json}

class ReturningJsonSpec extends UnitSpec{

  "when creating a ReturningJson class it " should {
    "allow valid inputs" in {
      val Str = STR
      val json = Json.toJson(Str)
      val returningJson = ReturningJson(
                                        "valid_question",
                                        "this is a valid question",
                                        None,
                                        None,
                                        None
      )
      val returningJsonWithOptionals = ReturningJson(
                                        "valid_question",
                                        "this is a valid question",
                                        Some("this is a valid question in welsh"),
                                        Some(Str),
                                        Some("[A-Z][A-Z]/d")
      )

      returningJson.quid shouldBe "valid_question"
      returningJson.questionEn shouldBe "this is a valid question"
      returningJsonWithOptionals.questionCy shouldBe Some("this is a valid question in welsh")
      json.validate[AnswerType] shouldBe JsSuccess(STR)
      returningJsonWithOptionals.regex shouldBe Some("[A-Z][A-Z]/d")
    }
  }
}
