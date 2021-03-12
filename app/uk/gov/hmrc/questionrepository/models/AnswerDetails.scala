/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class AnswerDetails(questionKey: QuestionKey, answer: Answer)

object AnswerDetails {
  implicit val format:  Format[AnswerDetails] = Json.format[AnswerDetails]
}
