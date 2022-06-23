/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.domain.Nino


class SelectionSpec extends UnitSpec {

  "when creating a selection it " should {

    "serialize to json" in new Setup {
      val json: JsValue = Json.toJson(selection)
      json shouldBe selectionJson
    }

    "deserialization from json" in new Setup {
      selectionJson.validate[Selection] shouldBe JsSuccess(selection)
    }

    "Selection to list" in new Setup {
      selection.toList shouldBe List("AA000000D")
    }

    "Selection to string" in new Setup {
      selection.toString shouldBe "AA000000D"
    }

    "Selection for obscureIdentifier" in new Setup {
      selection.obscureIdentifier(selection.toString) shouldBe "XXXX0000D"
      selection.toList.map(selection.obscureIdentifier).mkString(",") shouldBe "XXXX0000D"
    }
  }

  trait Setup {
    val selection: Selection = Selection(Nino("AA000000D"))
    val selectionJson: JsValue = Json.parse(s"""{"nino":"AA000000D"}""")
  }

}
