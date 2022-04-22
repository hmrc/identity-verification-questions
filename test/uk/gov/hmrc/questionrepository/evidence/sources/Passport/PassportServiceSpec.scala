/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.Passport

import java.time.LocalDateTime

import Utils.UnitSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.{AppConfig, Outage}
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.{PassportConnector, PassportService}
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PassportServiceSpec extends UnitSpec{

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe passportService
  }

  "calling questions" should {
    "return a sequence of Questions" when {
      "supplied with a valid nino" in new WithStubbing {
        (mockPassportConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(Seq(true)))
        service.questions(selectionNino).futureValue shouldBe Seq(passportQuestion)
      }
    }
  }

  "calling questions" should {
    "return an empty list" when {
      "not supplied with a nino" in new Setup {
        val testOutage: Outage = Outage(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(Some(testOutage), List.empty, List.empty, List.empty))
        service.questions(selectionWithoutNino).futureValue shouldBe Seq.empty
      }
    }
  }

  trait Setup {
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val mockPassportConnector: PassportConnector = mock[PassportConnector]
    val mockEventDispatcher:EventDispatcher = mock[EventDispatcher]
    val mockAuditService: AuditService = mock[AuditService]

    val service: PassportService = new PassportService(mockPassportConnector, mockEventDispatcher, mockAuditService)
    val passportQuestion = new Question(PassportQuestion,Seq())

    val selectionNino: Selection = Selection(origin, Seq(ninoIdentifier, saUtrIdentifier))
    val selectionWithoutNino: Selection = Selection(origin, Seq(saUtrIdentifier))

  }

  trait WithStubbing extends Setup {
    (mockAppConfig.serviceStatus(_: ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino")))
    (mockAppConfig.serviceCbNumberOfCallsToTrigger(_: ServiceName)).expects(service.serviceName).returning(Some(20))
    (mockAppConfig.serviceCbUnavailableDurationInSec(_: ServiceName)).expects(service.serviceName).returning(Some(60))
    (mockAppConfig.serviceCbUnstableDurationInSec(_: ServiceName)).expects(service.serviceName).returning(Some(300))
  }
}
