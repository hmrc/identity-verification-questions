/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}

class QuestionResultSpec extends UnitSpec {

  "when creating a QuestionResult object it" should {
    "allow creation with valid QuestionId and Correct Score" in new Setup {
      questionResultCorrect.questionId.toString shouldBe "12345"
      questionResultCorrect.score.toString shouldBe "correct"
    }

    "allow creation with valid QuestionId and Incorrect Score" in new Setup {
      questionResultIncorrect.questionId.toString shouldBe "12345"
      questionResultIncorrect.score.toString shouldBe "incorrect"
    }

    "allow creation with valid QuestionId and Unknown Score" in new Setup {
      questionResultUnknown.questionId.toString shouldBe "12345"
      questionResultUnknown.score.toString shouldBe "unknown"
    }

    "not allow creation if QuestionId is invalid" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        QuestionResult(QuestionId("1234"), Correct)
      }
    }

    "serialize to json with correct score" in new Setup {
      Json.toJson(questionResultCorrect) shouldBe jsonCorrect
    }

    "serialize to json with incorrect score" in new Setup {
      Json.toJson(questionResultIncorrect) shouldBe jsonIncorrect
    }

    "serialize to json with unknown score" in new Setup {
      Json.toJson(questionResultUnknown) shouldBe jsonUnknown
    }

    "deserialize from json with correct score" in new Setup {
      jsonCorrect.validate[QuestionResult] shouldBe JsSuccess(questionResultCorrect)
    }

    "deserialize from json with incorrect score" in new Setup {
      jsonIncorrect.validate[QuestionResult] shouldBe JsSuccess(questionResultIncorrect)
    }

    "deserialize from json with unknown score" in new Setup {
      jsonUnknown.validate[QuestionResult] shouldBe JsSuccess(questionResultUnknown)
    }
  }
  trait Setup {
    val questionResultCorrect: QuestionResult = QuestionResult(QuestionId("12345"), Correct)
    val questionResultIncorrect: QuestionResult = QuestionResult(QuestionId("12345"), Incorrect)
    val questionResultUnknown: QuestionResult = QuestionResult(QuestionId("12345"), Unknown)
    val jsonCorrect: JsValue = Json.parse(s"""{"questionId":"12345","score":"correct"}""")
    val jsonIncorrect: JsValue = Json.parse(s"""{"questionId":"12345","score":"incorrect"}""")
    val jsonUnknown: JsValue = Json.parse(s"""{"questionId":"12345","score":"unknown"}""")
  }
}
