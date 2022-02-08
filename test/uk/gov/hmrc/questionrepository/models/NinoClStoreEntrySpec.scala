/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.domain.Nino

class NinoClStoreEntrySpec extends UnitSpec{
  "The nino cl store entry" should {
    "deserialise the json returned from IV correctly" in {
      val jsonAsString = """[{"credId":"cred-id-529348094","nino":"AA000003D","confidenceLevel":200,"createdAt":{"$date":1533049455567},"updatedAt":{"$date":1533049455567}}]"""
      val expected = NinoClStoreEntry(credId = "cred-id-529348094", nino = Nino("AA000003D"), Some(ConfidenceLevel.L200))
      val actual = Json.parse(jsonAsString).validate[Seq[NinoClStoreEntry]]
      actual shouldBe JsSuccess(List(expected))
    }
  }
}
