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
