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

import play.api.libs.json.{JsString, JsSuccess, Reads, Writes}


sealed trait AnswerType

case object STR extends AnswerType
case object INT extends AnswerType
case object DBL extends AnswerType

object AnswerType {
  implicit val answerTypeReads: Reads[AnswerType] = Reads {
    case JsString("STR") => JsSuccess(STR)
    case JsString("INT") => JsSuccess(INT)
    case JsString("DBL") => JsSuccess(DBL)
    case e => throw new IllegalArgumentException(s"unknown AnswerType $e")
  }

  implicit val answerTypeWrites: Writes[AnswerType] = Writes { answerType =>
    JsString(answerType.toString)
  }
}
