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

import play.api.libs.json._

sealed trait Answer

/**
 * Answer format used for all P60 (single monetary value) questions
 */
case class SimpleAnswer(value: String) extends Answer {
  override val toString: String = value
}

object SimpleAnswer {
  implicit val simpleAnswerFormats: Format[SimpleAnswer] = new Format[SimpleAnswer] {
    override def writes(a: SimpleAnswer): JsValue = Answer.format.writes(a)
    override def reads(json: JsValue): JsResult[SimpleAnswer] =
      Answer.format.reads(json).asInstanceOf[JsResult[SimpleAnswer]] // can we avoid the use of asInstanceOf?
  }
}

// add more answer types here, when needed

object Answer {

  implicit val format: Format[Answer] = {
    new Format[Answer] {
      override def reads(json: JsValue): JsResult[Answer] = json match {
        case JsString(value) => JsSuccess(SimpleAnswer(value))
        case jsValue => throw new IllegalArgumentException(s"Unrecognised answer value: $jsValue")
      }
      override def writes(a: Answer): JsValue = a match {
        case SimpleAnswer(value) => JsString(value)
        case a => throw new IllegalArgumentException(s"Unable to serialize value: $a")
      }
    }
  }

}
