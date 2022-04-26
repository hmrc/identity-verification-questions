/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class AnswerCheck(correlationId: CorrelationId, selection: Selection, answers: Seq[AnswerDetails])

object AnswerCheck {
  implicit val format: Format[AnswerCheck] = Json.format[AnswerCheck]
}
