/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.UnitSpec
import uk.gov.hmrc.questionrepository.models._

class AnswersServiceSpec extends UnitSpec{

  "when calling checkAnswers it  " should {
    "return a Future of QuestionResult with a result of Unknown" in new SetUp {
      val result: List[QuestionResult] = await(controller.checkAnswers(answerCheck))
      result shouldBe List(QuestionResult(QuestionId("123456"),Unknown))
    }
  }


  trait SetUp{
    val controller = new AnswersService()
    val origin: Origin = Origin("valid_string")
    val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"))
    val answerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(QuestionId("123456"), StringAnswer("an answer")))
    val answerCheck: AnswerCheck = AnswerCheck(origin, identifiers,answerDetails)
  }
}
