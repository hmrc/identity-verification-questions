/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.domain.Nino


class SelectionSpec extends UnitSpec{

  "when creating a selection it " should {

    "serialize to json" in new Setup {
      val json: JsValue = Json.toJson(selection)
      json shouldBe selectionJson
    }

    "deserialization from json" in new Setup {
      selectionJson.validate[Selection] shouldBe JsSuccess(selection)
    }
  }

  trait Setup {
    val selection: Selection = Selection(Nino("AA000000D"))
    val selectionJson: JsValue = Json.parse(s"""{"nino":"AA000000D"}""")
  }

}
