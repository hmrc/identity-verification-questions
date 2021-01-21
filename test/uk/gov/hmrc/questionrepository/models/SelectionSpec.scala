/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec

class SelectionSpec extends UnitSpec{

  "when creating a selection it " should {

    "allow valid values with min and max" in {
      val origin = Origin("valid_string")
      val selection = Selection(origin,"selections_placeholder",Some(5),Some(1))
      selection.origin.value shouldBe ("valid_string")
      selection.max shouldBe Some(5)
      selection.min shouldBe Some(1)
    }

    "allow valid values without min and max" in {
      val origin = Origin("valid_string")
      val selection = Selection(origin,"selections_placeholder",None,None)
      selection.origin.value shouldBe ("valid_string")
    }

    "not allow with only one min or max" in {
      val origin = Origin("valid_string")
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, "selections_placeholder", Some(5), None)
      }
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, "selections_placeholder", None, Some(1))
      }
    }

    "not allow a max to be smaller then a min" in {
      val origin = Origin("valid_string")
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, "selections_placeholder", Some(5), Some(8))
      }
    }

    "not allow a min to be zero or smaller" in {
      val origin = Origin("valid_string")
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, "selections_placeholder", Some(5), Some(0))
      }
      an[IllegalArgumentException] shouldBe thrownBy {
        Selection(origin, "selections_placeholder", Some(5), Some(-1))
      }
    }
  }

}
