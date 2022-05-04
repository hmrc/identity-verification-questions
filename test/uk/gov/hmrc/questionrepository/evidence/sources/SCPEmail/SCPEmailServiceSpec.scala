/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.SCPEmail

import Utils.{LogCapturing, UnitSpec}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.{SCPEmailConnector, SCPEmailService}
import uk.gov.hmrc.questionrepository.models.{Question, SCPEmailQuestion, Selection, ServiceName, scpEmailService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SCPEmailServiceSpec extends UnitSpec with LogCapturing {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe scpEmailService
  }

  "calling `questions`" should {
    "return a sequence of Question's" when {
      "SCPEmailConnector returns a non empty sequence of Option[String] email's" in new WithStubbing {
        (mockSCPEmailConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(emailList))

        service.questions(selectionNino).futureValue shouldBe Seq(scpEmailQuestion)
      }
    }

    "return a empty sequence of Question's" when {
      "Evidence source in Not available" in new Setup {
        (mockAppConfig.serviceStatus(_ :ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(None, List("nino")))

        service.questions(selectionNoNino).futureValue shouldBe Seq()
      }

      "SCPEmailConnector returns an empty sequence of Payment's" in new WithStubbing {
        (mockSCPEmailConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(Seq.empty[Option[String]]))

        service.questions(selectionNino).futureValue shouldBe Seq()
      }
    }
  }

  trait Setup extends TestData {
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val mockSCPEmailConnector = mock[SCPEmailConnector]
    val mockEventDispatcher:EventDispatcher = mock[EventDispatcher]
    val mockAuditService: AuditService = mock[AuditService]

    val service = new SCPEmailService(mockSCPEmailConnector, mockEventDispatcher, mockAuditService)
  }

  trait WithStubbing extends Setup {
    (mockAppConfig.serviceStatus(_ :ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(None, List("nino")))
    (mockAppConfig.serviceCbNumberOfCallsToTrigger(_ :ServiceName)).expects(service.serviceName).returning(Some(20))
    (mockAppConfig.serviceCbUnavailableDurationInSec(_ :ServiceName)).expects(service.serviceName).returning(Some(60))
    (mockAppConfig.serviceCbUnstableDurationInSec(_ :ServiceName)).expects(service.serviceName).returning(Some(300))
  }

  trait TestData {
    val emailList = List(Some("email@email.com"))
    val noEmailList = List(None)

    val ninoIdentifier: Nino = Nino("AA000000D")
    val utrIdentifier: SaUtr = SaUtr("12345678")

    val selectionNino: Selection = Selection(ninoIdentifier, utrIdentifier)
    val selectionNoNino: Selection = Selection(utrIdentifier)

    val scpEmailQuestion = Question(SCPEmailQuestion, Seq("email@email.com"), Map.empty[String, String])
  }
}
