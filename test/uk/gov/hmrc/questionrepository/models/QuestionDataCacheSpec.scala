/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import uk.gov.hmrc.questionrepository.models.P60.PaymentToDate
import uk.gov.hmrc.questionrepository.models.identifier._

import java.time.LocalDateTime

class QuestionDataCacheSpec extends UnitSpec{

  "when creating a questionDataCache it " should {
    "allow valid inputs" in new Setup{
      val questionDataCache: QuestionDataCache = QuestionDataCache(correlationId, selection, questionList, dateTime)
      questionDataCache.selection shouldBe selection
      questionDataCache.questions shouldBe questionList
    }
  }

}

trait Setup {
  val correlationId = CorrelationId()
  val origin: Origin = Origin("valid_string")
  val ninoIdentifier: NinoI = NinoI("AA000000D")
  val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"))
  val selection: Selection = Selection(origin,identifiers)
  case class TestRecord(value: BigDecimal)
  val questionList = List(Question(PaymentToDate,List(TestRecord(1).toString)))
  val dateTime = LocalDateTime.now()
}
