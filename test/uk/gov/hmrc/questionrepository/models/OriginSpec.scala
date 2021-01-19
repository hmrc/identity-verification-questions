/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec

class OriginSpec extends UnitSpec {

  "when creating an Origin " should {

    "allow alphanumeric, hyphens, underscores" in {
      val validString = "-aA_login-origin_zZ_"

      val origin = Origin(validString)
      origin.value shouldBe validString
    }

    "not allow invalid characters" in {
      val invalidString = "@! login-origin_,."
      an[IllegalArgumentException] shouldBe thrownBy {
        Origin(invalidString)
      }
    }

    "not allow values less than 2 characters" in {
      val invalidString = "A"
      an[IllegalArgumentException] shouldBe thrownBy {
        Origin(invalidString)
      }
    }

    "not allow values greater than 50 characters" in {
      val invalidString = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
      an[IllegalArgumentException] shouldBe thrownBy {
        Origin(invalidString)
      }
    }
  }
}
