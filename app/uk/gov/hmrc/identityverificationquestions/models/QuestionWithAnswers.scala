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

import play.api.libs.json.{Json, Format}

/**
 * The question to be asked of the user, along with their answers from evidence sources
 * Part of the Mongo document for the user's question/answer session.
 */
case class QuestionWithAnswers(questionKey: QuestionKey, answers: Seq[String], info: Map[String, String] = Map.empty)

object QuestionWithAnswers {
  implicit val format: Format[QuestionWithAnswers] = Json.format[QuestionWithAnswers]
}
