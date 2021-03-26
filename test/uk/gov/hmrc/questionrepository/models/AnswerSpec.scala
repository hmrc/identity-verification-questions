/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsArray, JsBoolean, JsNumber, JsString, JsSuccess, JsValue, Json}

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

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

    "create json for PassportAnswer" in new Setup {
      val passportAnswer: PassportAnswer = PassportAnswer("123456789", "surname", "firstname", passportExpiryDate)
      Json.toJson(passportAnswer) shouldBe jsonPassportAnswer
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

    "create PassportAnswer if json is valid" in new Setup {
      jsonPassportAnswer.validate[Answer] shouldBe JsSuccess(PassportAnswer("123456789","surname", "firstname", passportExpiryDate))
      jsonPassportAnswer.validate[PassportAnswer] shouldBe JsSuccess(PassportAnswer("123456789","surname", "firstname", passportExpiryDate))
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

  "serialize a sequence of Answer" in new Setup {
    Json.toJson[Seq[Answer]](Seq(stringA, passportA, doubleA, booleanA)) shouldBe seqAnswer
  }

  trait Setup {
    val jsonStringAnswer: JsString = JsString("an answer")
    val jsonIntegerAnswer: JsNumber = JsNumber(500)
    val jsonDoubleAnswer: JsNumber = JsNumber(500.12)
    val jsonBooleanAnswer: JsBoolean = JsBoolean(true)
    val wellBadJson: JsArray = JsArray()
    val passportExpiryDate: LocalDate = LocalDate.parse("2000-02-28", ISO_LOCAL_DATE)
    val jsonPassportAnswer: JsValue = Json.parse(s"""{"passportNumber":"123456789","surname":"surname","forenames":"firstname","dateOfExpiry":"2000-02-28"}""")
    val stringA: Answer = StringAnswer("an answer")
    val passportA: Answer = PassportAnswer("123456789","surname", "firstname", LocalDate.parse("2000-02-28", ISO_LOCAL_DATE))
    val doubleA: Answer = DoubleAnswer("500.12")
    val booleanA: Answer = BooleanAnswer("true")
    val seqAnswer: JsValue = Json.parse(s"[${jsonStringAnswer.toString},${jsonPassportAnswer.toString},${jsonDoubleAnswer.toString},${jsonBooleanAnswer.toString}]")
  }
}
