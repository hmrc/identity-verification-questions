/*
 * Copyright 2022 HM Revenue & Customs
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
  )
}
