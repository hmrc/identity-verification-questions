/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class QuestionResult(questionKey: QuestionKey, score: Score)

object QuestionResult {
  implicit val format: Format[QuestionResult] = Json.format[QuestionResult]
}
