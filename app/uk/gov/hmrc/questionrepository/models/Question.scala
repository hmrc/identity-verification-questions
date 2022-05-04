/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Json, Format}

/**
 * A type question that can be posed to the user
 * "answer" field is redundant - please MAKE A NEW CASE CLASS with no answers
 */
case class Question(questionKey: QuestionKey, answers: Seq[String], info: Map[String, String] = Map.empty)

object Question {
  implicit val format: Format[Question] = Json.format[Question]
}
