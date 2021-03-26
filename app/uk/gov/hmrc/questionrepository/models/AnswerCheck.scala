/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.questionrepository.models.identifier._

case class AnswerCheck(correlationId: CorrelationId, origin: Origin, identifiers: Seq[Identifier], answers: Seq[AnswerDetails])

object AnswerCheck {
  implicit val format: Format[AnswerCheck] = Json.format[AnswerCheck]
}
