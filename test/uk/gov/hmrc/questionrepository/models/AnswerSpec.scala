/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsArray, JsBoolean, JsNumber, JsString, JsSuccess, Json}

class AnswerSpec extends UnitSpec {

  "serializing Answer" should {
    "create json for StringAnswer" in new Setup {
      val stringAnswer: StringAnswer = StringAnswer("an answer")
      Json.toJson(stringAnswer) shouldBe jsonStringAnswer
    }

    "create json for IntegerAnswer" in new Setup {
      val integerAnswer: IntegerAnswer = IntegerAnswer(500)
      Json.toJson(integerAnswer) shouldBe jsonIntegerAnswer
    }

    "create json for DoubleAnswer" in new Setup {
      val doubleAnswer: DoubleAnswer = DoubleAnswer(500.12)
      Json.toJson(doubleAnswer) shouldBe jsonDoubleAnswer
    }

    "create json for BooleanAnswer" in new Setup {
      val booleanAnswer: BooleanAnswer = BooleanAnswer(true)
      Json.toJson(booleanAnswer) shouldBe jsonBooleanAnswer
    }
  }

  "deserializing Answer" should {
    "create StringAnswer if json is valid" in new Setup {
      jsonStringAnswer.validate[Answer] shouldBe JsSuccess(StringAnswer("an answer"))
      jsonStringAnswer.validate[StringAnswer] shouldBe JsSuccess(StringAnswer("an answer"))
    }

    "generate exception if StringAnswer json is invalid" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        wellBadJson.validate[StringAnswer]
      }
    }

    "create IntegerAnswer if json is valid" in new Setup {
      jsonIntegerAnswer.validate[Answer] shouldBe JsSuccess(IntegerAnswer(500))
      jsonIntegerAnswer.validate[IntegerAnswer] shouldBe JsSuccess(IntegerAnswer(500))
    }

    "generate exception if IntegerAnswer json is invalid" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        wellBadJson.validate[IntegerAnswer]
      }
    }

    "create DoubleAnswer if json is valid" in new Setup {
      jsonDoubleAnswer.validate[Answer] shouldBe JsSuccess(DoubleAnswer(500.12))
      jsonDoubleAnswer.validate[DoubleAnswer] shouldBe JsSuccess(DoubleAnswer(500.12))
    }

    "generate exception if DoubleAnswer json is invalid" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        wellBadJson.validate[DoubleAnswer]
      }
    }

    "create BooleanAnswer if json is valid" in new Setup {
      jsonBooleanAnswer.validate[Answer] shouldBe JsSuccess(BooleanAnswer(true))
      jsonBooleanAnswer.validate[BooleanAnswer] shouldBe JsSuccess(BooleanAnswer(true))
    }

    "generate exception if BooleanAnswer json is invalid" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        wellBadJson.validate[BooleanAnswer]
      }
    }

    "generate exception if Answer json is invalid" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        wellBadJson.validate[Answer]
      }
    }
  }

  "Answer created from string" should {
    "create IntegerAnswer" in {
      IntegerAnswer("500").value shouldBe 500
    }

    "create DoubleAnswer" in {
      DoubleAnswer("500.12").value shouldBe 500.12
    }

    "create BooleanAnswer" in {
      BooleanAnswer("true").value shouldBe true
    }
  }

  trait Setup {
    val jsonStringAnswer: JsString = JsString("an answer")
    val jsonIntegerAnswer: JsNumber = JsNumber(500)
    val jsonDoubleAnswer: JsNumber = JsNumber(500.12)
    val jsonBooleanAnswer: JsBoolean = JsBoolean(true)
    val wellBadJson: JsArray = JsArray()
  }
}
