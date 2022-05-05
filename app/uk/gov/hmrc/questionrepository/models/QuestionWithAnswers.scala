/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Json, Format}

/**
 * The question to be asked of the user, along with their answers from evidence sources
 * Part of the Mongo document for the user's question/answer session.
 */
case class QuestionWithAnswers(questionKey: QuestionKey, answers: Seq[String], info: Map[String, String] = Map.empty)

object QuestionWithAnswers {
  implicit val format: Format[QuestionWithAnswers] = Json.format[QuestionWithAnswers]
}
