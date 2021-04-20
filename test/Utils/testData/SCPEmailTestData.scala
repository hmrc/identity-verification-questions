/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package Utils.testData

import play.api.libs.json.{JsValue, Json}

trait SCPEmailTestData {
  val ninoClStoreResponseJson: JsValue = Json.parse(
    """[{
      |  "credId": "credid-123456789",
      |  "nino": "AA000003D",
      |  "confidenceLevel": 200
      |  }]""".stripMargin
  )

  val accountInfoResponseJson: JsValue = Json.parse(
    """{
      |  "sub": "sub",
      |  "roles": ["roles"],
      |  "groupId": "groupId",
      |  "trustId": "trustId",
      |  "name": "name",
      |  "email": "email@email.com",
      |  "emailVerified": true,
      |  "suspended": false,
      |  "emailStatus": "emailStatus",
      |  "description": "description",
      |  "groupRegistrationCategory": "groupRegistrationCategory",
      |  "agentId": "agentId",
      |  "agentCode": "agentCode",
      |  "agentFriendlyName": "agentFriendlyName",
      |  "credentialCreatedDate": "a date"
      |  }""".stripMargin



//      validAccountInfoWithMaximumDetails.sub shouldBe "sub"
//  validAccountInfoWithMaximumDetails.roles shouldBe List("roles")
//  validAccountInfoWithMaximumDetails.groupId shouldBe "groupId"
//  validAccountInfoWithMaximumDetails.trustId shouldBe "trustId"
//  validAccountInfoWithMaximumDetails.name shouldBe "name"
//  validAccountInfoWithMaximumDetails.email shouldBe Some("email")
//  validAccountInfoWithMaximumDetails.emailVerified shouldBe Some(true)
//  validAccountInfoWithMaximumDetails.suspended shouldBe Some(false)
//  validAccountInfoWithMaximumDetails.emailStatus shouldBe Some("emailStatus")
//  validAccountInfoWithMaximumDetails.description shouldBe Some("description")
//  validAccountInfoWithMaximumDetails.groupRegistrationCategory shouldBe Some("groupRegistrationCategory")
//  validAccountInfoWithMaximumDetails.agentId shouldBe Some("agentId")
//  validAccountInfoWithMaximumDetails.agentCode shouldBe Some("agentCode")
//  validAccountInfoWithMaximumDetails.agentFriendlyName shouldBe Some("agentFriendlyName")
//  validAccountInfoWithMaximumDetails.credentialCreatedDate shouldBe Some("credentialCreatedDate")
  )
}
