/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.domain.Nino

class IdentifierSpec extends UnitSpec {

  "when creating a nino" should {
    "allow valid nino to be created from String" in {
      val validString = "AA000000D"

      val nino = NinoI(validString)
      nino.toString shouldBe validString
    }

    "allow valid nino to be created from Nino" in {
      val validString = "AA000000D"
      val validNino = Nino(validString)

      val nino = NinoI(validNino)
      nino.toString shouldBe validString
    }

    "not allow invalid nino value" in {
      val invalidString = "11111111"

      an[IllegalArgumentException] shouldBe thrownBy {
        NinoI(invalidString)
      }
    }
  }

  "when serializing and de-serializing a nino identifier" should {
    "create valid json" in {
      val validString = "AA000000D"
      val validJson = s"""{"nino":"AA000000D"}"""
      val ninoIdentifier = NinoI(validString)

      Json.toJson(ninoIdentifier).toString shouldBe validJson
    }

    "create NinoI from valid json" in {
      val validJson = s"""{"nino":"AA000000D"}"""

      val json = Json.parse(validJson)
      json.validate[NinoI] shouldBe JsSuccess(NinoI("AA000000D"))
    }

    "create Identifier from valid json" in {
      val validJson = s"""{"nino":"AA000000D"}"""

      val json = Json.parse(validJson)
      json.validate[Identifier] shouldBe JsSuccess(NinoI("AA000000D"))
    }
  }

  "when creating a sautr" should {
    "allow valid utr to be created from String" in {
      val validString = "12345678"

      val utr = SaUtrI(validString)
      utr.toString shouldBe validString
    }
  }

  "when serializing and de-serializing a sautr identifier" should {
    "create valid json" in {
      val validString = "123456789"
      val validJson = s"""{"utr":"123456789"}"""
      val utrIdentifier = SaUtrI(validString)

      Json.toJson(utrIdentifier).toString shouldBe validJson
    }

    "create SaUtrI from valid json" in {
      val validJson = s"""{"utr":"123456789"}"""

      val json = Json.parse(validJson)
      json.validate[SaUtrI] shouldBe JsSuccess(SaUtrI("123456789"))
    }

    "create Identifier from valid json" in {
      val validJson = s"""{"utr":"123456789"}"""

      val json = Json.parse(validJson)
      json.validate[Identifier] shouldBe JsSuccess(SaUtrI("123456789"))
    }
  }

  "when serializing and de-serializing sequences of iIdentifiers" should {
    "create json for sequence of nino" in {
      val testSeq: Seq[Identifier] = Seq(NinoI("AA000000D"), NinoI("NP123456D"))

      Json.toJson(testSeq) shouldBe Json.parse(s"""[{"nino":"AA000000D"},{"nino":"NP123456D"}]""")
    }

    "create json for sequence of sautr" in {
      val testSeq: Seq[Identifier] = Seq(SaUtrI("987654321"), SaUtrI("123456789"))

      Json.toJson(testSeq) shouldBe Json.parse(s"""[{"utr":"987654321"},{"utr":"123456789"}]""")
    }

    "create json for mixed sequence of nino and sautr" in {
      val testSeq: Seq[Identifier] = Seq(NinoI("AA000000D"), SaUtrI("123456789"))

      Json.toJson(testSeq) shouldBe Json.parse(s"""[{"nino":"AA000000D"},{"utr":"123456789"}]""")
    }

    "create sequence of identifiers from json" in {
      val validJson = s"""[{"nino":"AA000000D"},{"utr":"123456789"}]"""

      val json = Json.parse(validJson)
      json.validate[Seq[Identifier]] shouldBe JsSuccess(Seq(NinoI("AA000000D"), SaUtrI("123456789")))
    }
  }
}
