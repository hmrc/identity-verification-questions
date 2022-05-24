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
import play.api.libs.json._

class ScoreSpec extends UnitSpec {

  "serializing Sore" should {
    "create json for Correct" in {
      val score: Score = Correct
      Json.toJson(score) shouldBe JsString("correct")
    }

    "create json for Incorrect" in {
      val score: Score = Incorrect
      Json.toJson(score) shouldBe JsString("incorrect")
    }

    "create json for Unknown" in {
      val score: Score = Unknown
      Json.toJson(score) shouldBe JsString("unknown")
    }

    "create json for Error" in {
      val score: Score = Error("an error")
      Json.toJson(score) shouldBe JsString("error: an error")
    }
  }

  "deserializing Score" should {
    "create Correct object" in {
      val json = Json.parse(s""""correct"""")
      json.validate[Score] shouldBe JsSuccess(Correct)
    }

    "create Incorrect object" in {
      val json = Json.parse(s""""incorrect"""")
      json.validate[Score] shouldBe JsSuccess(Incorrect)
    }

    "create Unknown object" in {
      val json = Json.parse(s""""unknown"""")
      json.validate[Score] shouldBe JsSuccess(Unknown)
    }

    "create Error object" in {
      val json = Json.parse(s""""error: an error"""")
      json.validate[Score] shouldBe JsSuccess(Error("an error"))
    }

    "not allow invalid Score" in {
      val json = Json.parse(s""""DOH"""")

      an[IllegalArgumentException] shouldBe thrownBy {
        json.validate[Score]
      }
    }
  }
}
