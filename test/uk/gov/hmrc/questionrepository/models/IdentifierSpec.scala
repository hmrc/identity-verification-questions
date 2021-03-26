/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.questionrepository.models.identifier._
import uk.gov.hmrc.questionrepository.models.identifier.Search._

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

  "when creating a date of birth" should {
    "allow valid date of birth to be created from string" in {
      val validDate = "2020-03-23"
      val dateOfBirth = DobI(validDate)
      dateOfBirth.toString shouldBe validDate
    }

    "not allow invalid dob value" in {
      val invalidString = "11111111"

      an[IllegalArgumentException] shouldBe thrownBy {
        DobI(invalidString)
      }
    }
  }

  "when serializing and de-serializing a date of birth identifier" should {
    "create valid json" in {
      val validString = "2020-03-23"
      val validJson = s"""{"dob":"2020-03-23"}"""
      val dobIdentifier = DobI(validString)
      Json.toJson(dobIdentifier).toString shouldBe validJson
    }
  }

  "create a date of birth from valid json" in {
    val validJson = s"""{"dob":"2020-03-23"}"""
    val json = Json.parse(validJson)
    json.validate[DobI] shouldBe JsSuccess(DobI("2020-03-23"))
  }

  "create Identifier from valid json" in {
    val validJson = s"""{"dob":"2020-03-23"}"""
    val json = Json.parse(validJson)
    json.validate[Identifier] shouldBe JsSuccess(DobI("2020-03-23"))
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

    "create json for mixed sequence of nino ,sautr and dob" in {
      val testSeq: Seq[Identifier] = Seq(NinoI("AA000000D"), SaUtrI("123456789"), DobI("1986-02-28"))

      Json.toJson(testSeq) shouldBe Json.parse(s"""[{"nino":"AA000000D"},{"utr":"123456789"},{"dob":"1986-02-28"}]""")
    }

    "create sequence of identifiers from json" in {
      val validJson = s"""[{"nino":"AA000000D"},{"utr":"123456789"},{"dob":"1986-02-28"}]"""

      val json = Json.parse(validJson)
      json.validate[Seq[Identifier]] shouldBe JsSuccess(Seq(NinoI("AA000000D"), SaUtrI("123456789"), DobI("1986-02-28")))
    }
  }

  "when searching a Seq[Identifier]" should {
    "return an Option[NinoI] == first NinoI in list and None for SaUTRI" when {
      "multiple NinoI and no SaUtrI in sequence" in {
        val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"), NinoI("NP123456D"))

        identifiers.nino shouldBe Some(NinoI("AA000000D"))
        identifiers.saUtr shouldBe None
      }

      "one NinoI and no SaUtrI in sequence" in {
        val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"))

        identifiers.nino shouldBe Some(NinoI("AA000000D"))
        identifiers.saUtr shouldBe None
      }
    }

    "return an Option[SaUtrI] == first SaUtrI in list and None for NinoI" when {
      "multiple SaUtrI and no NinoI in sequence" in {
        val identifiers: Seq[Identifier] = Seq(SaUtrI("123456789"), SaUtrI("987654321"))

        identifiers.saUtr shouldBe Some(SaUtrI("123456789"))
        identifiers.nino shouldBe None
      }

      "one NinoI and no SaUtrI in sequence" in {
        val identifiers: Seq[Identifier] = Seq(SaUtrI("123456789"))

        identifiers.saUtr shouldBe Some(SaUtrI("123456789"))
        identifiers.nino shouldBe None
      }
    }

    "return an Option[SaUtrI] and Option[NinoI]" when {
      "both in sequence" in {
        val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"), SaUtrI("123456789"))

        identifiers.saUtr shouldBe Some(SaUtrI("123456789"))
        identifiers.nino shouldBe Some(NinoI("AA000000D"))
      }
    }

    "return an None for SaUtrI and None for NinoI" when {
      "neither in sequence" in {
        val identifiers: Seq[Identifier] = Seq()

        identifiers.saUtr shouldBe None
        identifiers.nino shouldBe None
      }
    }
  }
}
