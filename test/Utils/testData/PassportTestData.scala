/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package Utils.testData

import play.api.libs.json.{JsValue, Json}

trait PassportTestData {
  val passportResponseJson: JsValue = Json.parse(
    """{
      |  "queryResult": 0,
      |  "request": {
      |    "nino": "SM081915",
      |    "requestId": "M1433328147816"
      |  }
      |  }""".stripMargin
  )
}
