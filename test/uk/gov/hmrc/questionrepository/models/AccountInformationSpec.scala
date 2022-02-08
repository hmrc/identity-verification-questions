/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec

class AccountInformationSpec extends UnitSpec{
  "when creating an AccountInformation object it" should {
    "allow creation with valid inputs" in new SetUp{
      validAccountInfoWithMaximumDetails.sub shouldBe "sub"
      validAccountInfoWithMaximumDetails.roles shouldBe List("roles")
      validAccountInfoWithMaximumDetails.groupId shouldBe "groupId"
      validAccountInfoWithMaximumDetails.trustId shouldBe "trustId"
      validAccountInfoWithMaximumDetails.name shouldBe "name"
      validAccountInfoWithMaximumDetails.email shouldBe Some("email")
      validAccountInfoWithMaximumDetails.emailVerified shouldBe Some(true)
      validAccountInfoWithMaximumDetails.suspended shouldBe Some(false)
      validAccountInfoWithMaximumDetails.emailStatus shouldBe Some("emailStatus")
      validAccountInfoWithMaximumDetails.description shouldBe Some("description")
      validAccountInfoWithMaximumDetails.groupRegistrationCategory shouldBe Some("groupRegistrationCategory")
      validAccountInfoWithMaximumDetails.agentId shouldBe Some("agentId")
      validAccountInfoWithMaximumDetails.agentCode shouldBe Some("agentCode")
      validAccountInfoWithMaximumDetails.agentFriendlyName shouldBe Some("agentFriendlyName")
      validAccountInfoWithMaximumDetails.credentialCreatedDate shouldBe Some("credentialCreatedDate")
    }
  }
  trait SetUp{
    val validAccountInfoWithMaximumDetails: AccountInformation =
      AccountInformation("sub",List("roles"),"groupId","trustId","name",Some("email"),Some(true),Some(false),
        Some("emailStatus"),Some("description"),Some("groupRegistrationCategory"),Some("agentId"),Some("agentCode"),
        Some("agentFriendlyName"),Some("credentialCreatedDate"))

  }
}
