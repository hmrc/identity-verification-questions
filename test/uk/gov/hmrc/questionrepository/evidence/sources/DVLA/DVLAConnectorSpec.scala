/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.DVLA

import Utils.UnitSpec
import uk.gov.hmrc.questionrepository.evidences.sources.Dvla.DvlaConnector
import uk.gov.hmrc.questionrepository.models.{Selection, dvlaService}

import scala.concurrent.ExecutionContext.Implicits.global

class DVLAConnectorSpec extends UnitSpec {
  "service name" should {
    "be set correctly" in new SetUp {
      connector.serviceName shouldBe dvlaService
    }
  }

  "calling getRecords" should {
    "return true" in new SetUp {
      connector.getRecords(selection).futureValue shouldBe Seq(true)
    }
  }

  class SetUp {
    val connector = new DvlaConnector()
    val selection: Selection = Selection(None, None, Some(dobIdentifier))
  }
}
