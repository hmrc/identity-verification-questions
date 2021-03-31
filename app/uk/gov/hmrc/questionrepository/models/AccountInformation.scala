/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Json, OFormat}

case class AccountInformation(
                               sub: String,
                               roles: List[String],
                               groupId: String,
                               trustId:String,
                               name:String,
                               email: Option[String],
                               emailVerified: Option[Boolean],
                               suspended: Option[Boolean],
                               emailStatus: Option[String],
                               description: Option[String],
                               groupRegistrationCategory: Option[String],
                               agentId: Option[String],
                               agentCode: Option[String],
                               agentFriendlyName: Option[String],
                               credentialCreatedDate: Option[String])

object AccountInformation {
  implicit val format: OFormat[AccountInformation] = Json.format[AccountInformation]
}
