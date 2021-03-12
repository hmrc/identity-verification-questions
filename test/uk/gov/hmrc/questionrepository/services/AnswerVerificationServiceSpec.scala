/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import Utils.UnitSpec
import uk.gov.hmrc.questionrepository.models.Identifier._
import uk.gov.hmrc.questionrepository.models._

class AnswerVerificationServiceSpec extends UnitSpec {

  "when calling checkAnswers it  " should {
    "return a Future of QuestionResult with a result of Unknown" in new SetUp {
      val result: List[QuestionResult] = await(service.checkAnswers(answerCheck))
      result shouldBe List(QuestionResult(PaymentToDate, Unknown))
    }
  }


  trait SetUp{
    val service = new AnswerVerificationService()
    val origin: Origin = Origin("valid_string")
    val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"))
    val answerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, StringAnswer("an answer")))
    val answerCheck: AnswerCheck = AnswerCheck(origin, identifiers,answerDetails)
  }
}
