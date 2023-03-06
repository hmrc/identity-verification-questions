/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.sources.payslip

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

import Utils.{LogCapturing, UnitSpec}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.Payslip.{IncomeTax, NationalInsurance}
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.models.{QuestionWithAnswers, Selection, ServiceName, payslipService}
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PayslipServiceSpec extends UnitSpec with LogCapturing {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe payslipService
  }

  "calling `questions`" should {
    "return a sequence of Question's" when {
      "PayslipConnector returns a non empty sequence of Payment's" in new WithStubbing {
        (mockPayslipConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *).returning(Future.successful(Seq(paymentOne, paymentTwo)))
        (mockAppConfig.rtiNumberOfPayslipMonthsToCheck(_ :ServiceName)).expects(service.serviceName).returning(3).atLeastOnce()
      service.questions(selectionNino, corrId).futureValue shouldBe Seq(
          incomeTaxQuestion, nationalInsuranceQuestion
        )
      }
    }

    "return a empty sequence of Question's" when {
      "Evidence source in Not available" in new Setup {
        (mockAppConfig.minimumMeoQuestionCount(_: String)).expects(service.serviceName.toString).returning(2)
        (mockAppConfig.serviceStatus(_ :ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(None, List("nino")))

        service.questions(selectionNoNino, corrId).futureValue shouldBe Seq()
      }

      "PayslipConnector returns an empty sequence of Payment's" in new WithStubbing {
        (mockPayslipConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(Seq()))

        service.questions(selectionNino, corrId).futureValue shouldBe Seq()
      }
      "PayslipConnector returns an insufficient Payment's" in new WithStubbing {
        (mockPayslipConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(Seq(paymentThree)))
        (mockAppConfig.rtiNumberOfPayslipMonthsToCheck(_ :ServiceName)).expects(service.serviceName).returning(3).atLeastOnce()

        service.questions(selectionNino, corrId).futureValue shouldBe Seq()
      }
    }
  }

  trait Setup extends TestDate {
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val mockPayslipConnector: PayslipConnector = mock[PayslipConnector]
    val mockEventDispatcher:EventDispatcher = mock[EventDispatcher]
    val mockAuditService: AuditService = mock[AuditService]
    val service: PayslipService = new PayslipService(mockPayslipConnector, mockEventDispatcher, mockAuditService) {
      override def today: LocalDate = LocalDate.parse("2020-06-28", ISO_LOCAL_DATE)
    }
  }

  trait WithStubbing extends Setup {
    (mockAppConfig.minimumMeoQuestionCount(_: String)).expects(service.serviceName.toString).returning(2)
    (mockAppConfig.serviceStatus(_: ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(None, List("nino")))
    (mockAppConfig.serviceCbNumberOfCallsToTrigger(_: ServiceName)).expects(service.serviceName).returning(Some(20))
    (mockAppConfig.serviceCbUnavailableDurationInSec(_: ServiceName)).expects(service.serviceName).returning(Some(60))
    (mockAppConfig.serviceCbUnstableDurationInSec(_: ServiceName)).expects(service.serviceName).returning(Some(300))
  }

  trait TestDate {
    val paymentOne: Payment = Payment(LocalDate.parse("2019-06-28", ISO_LOCAL_DATE), incomeTax = Some(340.82), nationalInsurancePaid = Some(10))
    val paymentTwo: Payment = Payment(LocalDate.parse("2019-04-30", ISO_LOCAL_DATE), incomeTax = Some(356.56), nationalInsurancePaid = Some(11))
    val paymentThree: Payment = Payment(LocalDate.parse("2019-04-30", ISO_LOCAL_DATE), incomeTax = Some(356.56))

    val ninoIdentifier: Nino = Nino("AA000000D")
    val utrIdentifier: SaUtr = SaUtr("12345678")

    val selectionNino: Selection = Selection(ninoIdentifier, utrIdentifier)
    val selectionNoNino: Selection = Selection(utrIdentifier)

    val incomeTaxQuestion: QuestionWithAnswers = QuestionWithAnswers(IncomeTax,List("340.82", "356.56"),Map("months" -> "3"))
    val nationalInsuranceQuestion: QuestionWithAnswers = QuestionWithAnswers(NationalInsurance, Seq("10.00", "11.00"), Map("months" -> "3"))

  }
}
