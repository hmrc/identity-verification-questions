/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

/**
 * A type of question that can be asked of the user, along with information
 * pertinent to the source (e.g. tax year queried)
 * part of the response returned to the user on POST /questions
 */
case class Question(questionKey: QuestionKey, info: Map[String, String] = Map.empty)

object Question {
  implicit val format: Format[Question] = Json.format[Question]
}
