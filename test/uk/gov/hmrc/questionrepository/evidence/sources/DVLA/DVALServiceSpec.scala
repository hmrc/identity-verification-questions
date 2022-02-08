/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.DVLA

import Utils.UnitSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.Dvla.{DvlaConnector, DvlaService}
import uk.gov.hmrc.questionrepository.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DVALServiceSpec extends UnitSpec {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe dvlaService
  }

  "calling `questions`" should {
    "return a sequence of Questions" when {
      "supplied with a valid dob" in new WithStubbing {
        (mockDvlaConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(List(true)))
        service.questions(selectionDob).futureValue shouldBe Seq(dvlaQuestion)
      }
    }

    "return a empty sequence of Question's" when {
      "Evidence source in Not available" in new Setup {
        (mockAppConfig.serviceStatus(_:ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(None, List.empty, List.empty, List("dob")))
        service.questions(selectionNoDob).futureValue shouldBe Seq()
      }
    }
  }

  trait Setup extends TestData {
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    val mockDvlaConnector: DvlaConnector = mock[DvlaConnector]
    val service = new DvlaService(mockDvlaConnector)
  }

  trait WithStubbing extends Setup {
    (mockAppConfig.serviceStatus(_ :ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(None, List.empty, List.empty, List("dob")))
    (mockAppConfig.serviceCbNumberOfCallsToTrigger(_ :ServiceName)).expects(service.serviceName).returning(Some(20))
    (mockAppConfig.serviceCbUnavailableDurationInSec(_ :ServiceName)).expects(service.serviceName).returning(Some(60))
    (mockAppConfig.serviceCbUnstableDurationInSec(_ :ServiceName)).expects(service.serviceName).returning(Some(300))
  }

  trait TestData {
    val selectionDob: Selection = Selection(origin, Seq(dobIdentifier))
    val selectionNoDob: Selection = Selection(origin, Seq(saUtrIdentifier))
    val dvlaQuestion: Question = Question(DVLAQuestion, Seq())
  }
}
