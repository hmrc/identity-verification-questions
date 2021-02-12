/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Json, Format}

case class Question(questionKey: String, answers: Seq[String], info: Map[String, String] = Map.empty)

object Question {
  implicit val format: Format[Question] = Json.format[Question]
}
