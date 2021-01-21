package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec

class ReturningJsonSpec extends UnitSpec{

  "when creating a ReturningJson class it " should {
    "allow valid inputs" in {
      val returningJson = ReturningJson("valid_question", "this is a valid question",None, None, None)
      val returningJsonWithOptionals = ReturningJson("valid_question", "this is a valid question",
        Some("this is a valid question in welsh"), Some(STR), Some("[A-Z][A-Z]/d"))

      returningJson.quid shouldBe "valid_question"
      returningJson.questionEn shouldBe "this is a valid question"
      returningJsonWithOptionals.questionCy shouldBe Some("this is a valid question in welsh")
      returningJsonWithOptionals.answerType shouldBe Some("String")
      returningJsonWithOptionals.regex shouldBe Some("[A-Z][A-Z]/d")
    }
  }
}
