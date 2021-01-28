/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsString, JsSuccess, Json}

class ScoreSpec extends UnitSpec {

  "serializing Sore" should {
    "create json for Correct" in {
      val score = Correct
      Json.toJson(score) shouldBe JsString("correct")
    }

    "create json for Incorrect" in {
      val score = Incorrect
      Json.toJson(score) shouldBe JsString("incorrect")
    }

    "create json for Unknown" in {
      val score = Unknown
      Json.toJson(score) shouldBe JsString("unknown")
    }
  }

  "deserializing Score" should {
    "create Correct object" in {
      val json = Json.parse(s""""correct"""")
      json.validate[Score] shouldBe JsSuccess(Correct)
    }

    "create Incorrect object" in {
      val json = Json.parse(s""""incorrect"""")
      json.validate[Score] shouldBe JsSuccess(Incorrect)
    }

    "create Unknown object" in {
      val json = Json.parse(s""""unknown"""")
      json.validate[Score] shouldBe JsSuccess(Unknown)
    }

    "not allow invalid Score" in {
      val json = Json.parse(s""""DOH"""")

      an[IllegalArgumentException] shouldBe thrownBy {
        json.validate[Score]
      }
    }
  }
}
