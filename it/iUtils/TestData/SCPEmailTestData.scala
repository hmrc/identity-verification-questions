/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
