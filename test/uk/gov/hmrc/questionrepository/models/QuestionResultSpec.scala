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
      questionResultCorrect.questionKey.toString shouldBe "PaymentToDate"
      questionResultCorrect.score.toString shouldBe "correct"
    }

    "allow creation with valid QuestionId and Incorrect Score" in new Setup {
      questionResultIncorrect.questionKey.toString shouldBe "PaymentToDate"
      questionResultIncorrect.score.toString shouldBe "incorrect"
    }

    "allow creation with valid QuestionId and Unknown Score" in new Setup {
      questionResultUnknown.questionKey.toString shouldBe "PaymentToDate"
      questionResultUnknown.score.toString shouldBe "unknown"
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
    val questionResultCorrect: QuestionResult = QuestionResult(PaymentToDate, Correct)
    val questionResultIncorrect: QuestionResult = QuestionResult(PaymentToDate, Incorrect)
    val questionResultUnknown: QuestionResult = QuestionResult(PaymentToDate, Unknown)
    val jsonCorrect: JsValue = Json.parse(s"""{"questionKey":"PaymentToDate","score":"correct"}""")
    val jsonIncorrect: JsValue = Json.parse(s"""{"questionKey":"PaymentToDate","score":"incorrect"}""")
    val jsonUnknown: JsValue = Json.parse(s"""{"questionKey":"PaymentToDate","score":"unknown"}""")
  }
}
