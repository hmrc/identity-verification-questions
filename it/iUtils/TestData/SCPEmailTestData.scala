/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package iUtils.TestData

import play.api.libs.json.{JsValue, Json}

trait SCPEmailTestData {

  val nino: String = "AA000000A"
  val credId: String = "credid-123456789"

  val ninoClStoreResponseJson: JsValue = Json.parse(
    s"""[{
      |  "credId": "$credId",
      |  "nino": "$nino",
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
