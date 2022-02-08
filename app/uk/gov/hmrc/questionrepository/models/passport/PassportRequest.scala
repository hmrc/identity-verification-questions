/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.passport

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.questionrepository.models.PassportAnswer

case class PassportRequest(dateOfBirth: String, passport: PassportAnswer)

object PassportRequest {
  implicit val format: Format[PassportRequest] = Json.format[PassportRequest]
}
