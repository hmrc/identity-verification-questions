/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.SCPEmail

import Utils.{LogCapturing, UnitSpec}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.{SCPEmailConnector, SCPEmailService}
import uk.gov.hmrc.questionrepository.models.identifier.{NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.{Origin, Question, SCPEmailQuestion, Selection, ServiceName, scpEmailService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SCPEmailServiceSpec extends UnitSpec with LogCapturing {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe scpEmailService
  }

  "calling `questions`" should {
    "return a sequence of Question's" when {
      "SCPEmailConnector returns a non empty sequence of Option[String] email's" in new WithStubbing {
        when(mockSCPEmailConnector.getRecords(any)(any, any)).thenReturn(Future.successful(emailList))

        service.questions(selectionNino).futureValue shouldBe Seq(scpEmailQuestion)
      }
    }

    "return a empty sequence of Question's" when {
      "Evidence source in Not available" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](service.serviceName))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino")))

        service.questions(selectionNoNino).futureValue shouldBe Seq()
      }

      "SCPEmailConnector returns an empty sequence of Payment's" in new WithStubbing {
        when(mockSCPEmailConnector.getRecords(any)(any, any)).thenReturn(Future.successful(Seq.empty[Option[String]]))

        service.questions(selectionNino).futureValue shouldBe Seq()
      }
    }
  }

  trait Setup extends TestData {
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

    val mockSCPEmailConnector = mock[SCPEmailConnector]

    val service = new SCPEmailService(mockSCPEmailConnector)
  }

  trait WithStubbing extends Setup {
    when(mockAppConfig.serviceStatus(eqTo[ServiceName](service.serviceName))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino")))
    when(mockAppConfig.serviceCbNumberOfCallsToTrigger(service.serviceName)).thenReturn(Some(20))
    when(mockAppConfig.serviceCbUnavailableDurationInSec(service.serviceName)).thenReturn(Some(60))
    when(mockAppConfig.serviceCbUnstableDurationInSec(service.serviceName)).thenReturn(Some(300))
  }

  trait TestData {
    val emailList = List(Some("email@email.com"))
    val noEmailList = List(None)

    val ninoIdentifier: NinoI = NinoI("AA000000D")
    val utrIdentifier: SaUtrI = SaUtrI("12345678")

    val origin: Origin = Origin("testOrigin")

    val selectionNino: Selection = Selection(origin, Seq(ninoIdentifier, utrIdentifier))
    val selectionNoNino: Selection = Selection(origin, Seq(utrIdentifier))

    val scpEmailQuestion = Question(SCPEmailQuestion, Seq("email@email.com"), Map.empty[String, String])
  }
}
