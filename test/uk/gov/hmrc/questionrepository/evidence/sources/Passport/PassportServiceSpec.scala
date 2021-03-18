/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.Passport

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

import Utils.UnitSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.{AppConfig, Outage}
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.{PassportConnector, PassportService}
import uk.gov.hmrc.questionrepository.models.Identifier.{NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.{Origin, PassportQuestion, Question, Selection, ServiceName, passportService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PassportServiceSpec extends UnitSpec{

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe passportService
  }

  "calling questions" should {
    "return a sequence of Questions" when {
      "supplied with a valid nino" in new WithStubbing {
        when(mockPassportConnector.getRecords(any)(any, any)).thenReturn(Future.successful(Seq(true)))
        service.questions(selectionNino).futureValue shouldBe Seq(passportQuestion)
      }
    }
  }

  "calling questions" should {
    "return an empty list" when {
      "not supplied with a nino" in new Setup {
        val testOutage: Outage = Outage(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](service.serviceName))).thenReturn(mockAppConfig.ServiceState(Some(testOutage), List.empty, List.empty, List.empty))
        service.questions(selectionWithoutNino).futureValue shouldBe Seq.empty
      }
    }
  }

  trait Setup {
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

    val mockPassportConnector: PassportConnector = mock[PassportConnector]

    val service: PassportService = new PassportService(mockPassportConnector)
    val passportQuestion = new Question(PassportQuestion,Seq())

    val ninoIdentifier: NinoI = NinoI("AA000000D")
    val utrIdentifier: SaUtrI = SaUtrI("12345678")
    val origin: Origin = Origin("testOrigin")
    val selectionNino: Selection = Selection(origin, Seq(ninoIdentifier, utrIdentifier))
    val selectionWithoutNino: Selection = Selection(origin, Seq(utrIdentifier))


  }

  trait WithStubbing extends Setup {
    when(mockAppConfig.serviceStatus(eqTo[ServiceName](service.serviceName))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino")))
    when(mockAppConfig.serviceCbNumberOfCallsToTrigger(service.serviceName)).thenReturn(Some(20))
    when(mockAppConfig.serviceCbUnavailableDurationInSec(service.serviceName)).thenReturn(Some(60))
    when(mockAppConfig.serviceCbUnstableDurationInSec(service.serviceName)).thenReturn(Some(300))
  }
}
