/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.dvla

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.questionrepository.models.UkDrivingLicenceAnswer

case class UkDrivingLicenceRequest(dob: String, drivingLicence: UkDrivingLicenceAnswer)

object UkDrivingLicenceRequest {
  implicit val format: OFormat[UkDrivingLicenceRequest] = Json.format[UkDrivingLicenceRequest]
}
