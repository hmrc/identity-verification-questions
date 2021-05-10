/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.DVLA

import Utils.UnitSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.Dvla.{DvlaConnector, DvlaService}
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.models.identifier.{DobI, SaUtrI}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DVALServiceSpec extends UnitSpec {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe dvlaService
  }

  "calling `questions`" should {
    "return a sequence of Questions" when {
      "supplied with a valid dob" in new WithStubbing {
        when(mockDvlaConnector.getRecords(any)(any, any)).thenReturn(Future.successful(List(true)))
        service.questions(selectionDob).futureValue shouldBe Seq(dvlaQuestion)
      }
    }

    "return a empty sequence of Question's" when {
      "Evidence source in Not available" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](service.serviceName))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("dob")))
        service.questions(selectionNoDob).futureValue shouldBe Seq()
      }
    }
  }

  trait Setup extends TestData {
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

    val mockDvlaConnector: DvlaConnector = mock[DvlaConnector]

    val service = new DvlaService(mockDvlaConnector)
  }

  trait WithStubbing extends Setup {
    when(mockAppConfig.serviceStatus(eqTo[ServiceName](service.serviceName))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("dob")))
    when(mockAppConfig.serviceCbNumberOfCallsToTrigger(service.serviceName)).thenReturn(Some(20))
    when(mockAppConfig.serviceCbUnavailableDurationInSec(service.serviceName)).thenReturn(Some(60))
    when(mockAppConfig.serviceCbUnstableDurationInSec(service.serviceName)).thenReturn(Some(300))
  }

  trait TestData {
    val dob = "1984-01-01"
    val dobIdentifier: DobI = DobI(dob)
    val utrIdentifier: SaUtrI = SaUtrI("12345678")

    val origin: Origin = Origin("testOrigin")

    val selectionDob: Selection = Selection(origin, Seq(dobIdentifier))
    val selectionNoDob: Selection = Selection(origin, Seq(utrIdentifier))

    val dvlaQuestion: Question = Question(DVLAQuestion, Seq())
  }
}
