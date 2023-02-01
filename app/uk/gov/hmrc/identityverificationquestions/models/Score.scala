/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json._

sealed trait Score {
  def value: String
}

case object Correct extends Score {
  val value: String = "correct"
  override val toString: String = value
}
case object Incorrect extends Score {
  val value: String = "incorrect"
  override val toString: String = value
}
case object Unknown extends Score {
  val value: String = "unknown"
  override val toString: String = value
}

case class Error(msg: String) extends Score {
  val value: String = s"error: $msg"
  override val toString: String = value
}

object Score {
  implicit val reads: Reads[Score] = Reads {
    case JsString("correct") => JsSuccess(Correct)
    case JsString("incorrect") => JsSuccess(Incorrect)
    case JsString("unknown") => JsSuccess(Unknown)
    case JsString(error) if error.startsWith("error:") => JsSuccess(Error(error.replace("error: ", "")))
    case e => throw new IllegalArgumentException(s"unknown Score $e")
  }

  implicit val writes: Writes[Score] = Writes { score =>
    JsString(score.value)
  }
}
