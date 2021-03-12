/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.questionrepository.models.Identifier._

case class AnswerCheck(origin: Origin, identifiers: Seq[Identifier], answers: Seq[AnswerDetails])

object AnswerCheck {
  implicit val format: Format[AnswerCheck] = Json.format[AnswerCheck]
}
