/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json._

class AnswerSpec extends UnitSpec {

  "serializing Answer" should {
    "create json for StringAnswer" in new Setup {
      val stringAnswer: Answer = SimpleAnswer("an answer")
      Json.toJson(stringAnswer) shouldBe jsonStringAnswer
    }
  }

  "deserializing Answer" should {
    "create StringAnswer if json is valid" in new Setup {
      jsonStringAnswer.validate[Answer] shouldBe JsSuccess(SimpleAnswer("an answer"))
      jsonStringAnswer.validate[SimpleAnswer] shouldBe JsSuccess(SimpleAnswer("an answer"))
    }

    "generate exception if StringAnswer json is invalid" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        wellBadJson.validate[SimpleAnswer]
      }
    }

    "generate exception if Answer json is invalid" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        wellBadJson.validate[Answer]
      }
    }

    "serialize a sequence of Answer" in new Setup {
      Json.toJson[Seq[Answer]](Seq(stringA, stringB, stringC)) shouldBe seqAnswer
    }
  }

  trait Setup {
    val jsonStringAnswer: JsString = JsString("an answer")
    val jsonDoubleAnswer: JsString = JsString("500.12")
    val jsonBooleanAnswer: JsString = JsString("true")
    val wellBadJson: JsArray = JsArray()
    val stringA: Answer = SimpleAnswer("an answer")
    val stringB: Answer = SimpleAnswer("500.12")
    val stringC: Answer = SimpleAnswer("true")
    val seqAnswer: JsValue = Json.parse(s"[${jsonStringAnswer.toString},${jsonDoubleAnswer.toString},${jsonBooleanAnswer.toString}]")
  }
}
