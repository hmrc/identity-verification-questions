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
      val strType = STR

      Json.toJson(strType) shouldBe "STR"
    }
  }

  "deserializing AnswerTypes" should {
    "create AnswerType objects" in {
      val json = Json.parse(s""""STR"""")

      json.validate[AnswerType] shouldBe JsSuccess(STR)
    }
  }
}
