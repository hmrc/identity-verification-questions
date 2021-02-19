/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import uk.gov.hmrc.questionrepository.models.Identifier._

class QuestionDataCacheSpec extends UnitSpec{

  "when creating a questionDataCache it " should {
    "allow valid inputs" in new Setup{
      val questionDataCache: QuestionDataCache = QuestionDataCache(selection,questionList)
      questionDataCache.selection shouldBe selection
      questionDataCache.questions shouldBe questionList
    }
  }

}

trait Setup {
  val origin: Origin = Origin("valid_string")
  val ninoIdentifier: NinoI = NinoI("AA000000D")
  val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"))
  val selection: Selection = Selection(origin,identifiers)
  case class TestRecord(value: BigDecimal)
  val questionList = List(Question("key",List(TestRecord(1).toString)))
}
