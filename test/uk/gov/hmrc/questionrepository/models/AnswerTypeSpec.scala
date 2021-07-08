/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, Json}

class AnswerTypeSpec extends UnitSpec {

  "serializing AnswerTypes" should {
    "create json for STR" in {
      val strType: AnswerType = STR

      Json.toJson(strType).toString shouldBe s""""STR""""
    }

    "create json for INT" in {
      val strType: AnswerType = INT

      Json.toJson(strType).toString shouldBe s""""INT""""
    }

    "create json for DBL" in {
      val strType: AnswerType = DBL

      Json.toJson(strType).toString shouldBe s""""DBL""""
    }
  }

  "deserializing AnswerTypes" should {
    "create STR object" in {
      val json = Json.parse(s""""STR"""")

      json.validate[AnswerType] shouldBe JsSuccess(STR)
    }

    "create INT object" in {
      val json = Json.parse(s""""INT"""")

      json.validate[AnswerType] shouldBe JsSuccess(INT)
    }

    "create DBL object" in {
      val json = Json.parse(s""""DBL"""")

      json.validate[AnswerType] shouldBe JsSuccess(DBL)
    }


    "not allow invalid AnswerType" in {
      val json = Json.parse(s""""DOH"""")

      an[IllegalArgumentException] shouldBe thrownBy {
        json.validate[AnswerType]
      }
    }
  }
}
