/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.Json

class SelectionSpec extends UnitSpec{

  "when creating a selection it " should {

    "allow valid values with min and max" in new Setup {

      val selection = Selection(origin,identifiers, Some(5),Some(1))
      selection.origin.value shouldBe ("valid_string")
      selection.selections.head shouldBe ninoIdentifier
      selection.max shouldBe Some(5)
      selection.min shouldBe Some(1)
    }

    "allow valid values without min and max" in new Setup {
      val selection = Selection(origin, identifiers)
      selection.origin.value shouldBe ("valid_string")
    }

    "not allow with only one min or max" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, identifiers, Some(5))
      }
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, identifiers, None, Some(1))
      }
    }

    "not allow a max to be smaller then a min" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, identifiers, Some(5), Some(8))
      }
    }

    "not allow a min to be zero or smaller" in new Setup {
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, identifiers, Some(5), Some(0))
      }
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, identifiers, Some(5), Some(-1))
      }
    }

    "serialize to json" in new Setup {
      val selection = Selection(origin,identifiers, Some(5),Some(1))
      val json = Json.toJson(identifiers)
      json.toString shouldBe s"""[{"nino":"AA000000D"}]"""
    }
  }

  trait Setup {
    val origin = Origin("valid_string")
    val ninoIdentifier = NinoI("AA000000D")
//    val identifiers: Identifier = NinoI("AA000000D")
    val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"))
  }

}
