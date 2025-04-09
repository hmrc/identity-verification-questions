/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.sources.P45

import Utils.{LogCapturing, UnitSpec}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.P45.{EmployeeNIContributions, PaymentToDate}
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.models.{QuestionWithAnswers, Selection, ServiceName, p45Service}
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Connector

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class P45ServiceSpec extends UnitSpec with LogCapturing {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe p45Service
  }

  "calling `questions`" should {
    "return a sequence of Question's" when {
      "P60Connector returns a non empty sequence of Payment's" in new WithStubbing {
        (mockP45Connector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *).returning(Future.successful(Seq(paymentOne, paymentTwo, paymentThree, paymentFive)))
        (mockAppConfig.bufferInMonthsForService(_: ServiceName)).expects(service.serviceName).returning(2).atLeastOnce()

        service.questions(selectionNino, corrId).futureValue shouldBe Seq(
          paymentToDateQuestion, employeeNIContributionsQuestion
        )
      }

      "P60Connector returns a non empty sequence of Payment's with previous year in additional information" in new WithStubbing {
        (mockP45Connector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(Seq(paymentOne, paymentTwo, paymentThree)))
        (mockAppConfig.bufferInMonthsForService(_: ServiceName)).expects(service.serviceName).returning(3).atLeastOnce()

        service.questions(selectionNino, corrId).futureValue shouldBe Seq(paymentToDateQuestion2, employeeNIContributionsQuestion2)
      }
    }

    "return a empty sequence of Question's" when {
      "Evidence source in Not available" in new Setup {
        (mockAppConfig.minimumMeoQuestionCount(_: String)).expects(service.serviceName.toString).returning(2)
        (mockAppConfig.serviceStatus(_: ServiceName)).expects(service.serviceName).returning(mockAppConfig.ServiceState(None, List("nino")))

        service.questions(selectionNoNino, corrId).futureValue shouldBe Seq()
      }

      "P60Connector returns an empty sequence of Payment's" in new WithStubbing {
        (mockP45Connector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(Seq()))

        service.questions(selectionNino, corrId).futureValue shouldBe Seq()
      }

      "P60Connector returns an insufficient Payment's" in new WithStubbing {
        (mockP45Connector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext)).expects(*, *, *).returning(Future.successful(Seq(insufficientPayment)))
        (mockAppConfig.bufferInMonthsForService(_: ServiceName)).expects(service.serviceName).returning(3).atLeastOnce()

        service.questions(selectionNino, corrId).futureValue shouldBe Seq()
      }
    }
  }

  trait Setup extends TestDate {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val mockAppConfig: AppConfig = mock[AppConfig]
    val mockP45Connector: P45Connector = mock[P45Connector]
    val mockEventDispatcher: EventDispatcher = mock[EventDispatcher]
    val mockAuditService: AuditService = mock[AuditService]
    val metricsService: MetricsService = app.injector.instanceOf[MetricsService]
    val service: P45Service = new P45Service(mockP45Connector, mockEventDispatcher, mockAuditService, mockAppConfig, metricsService) {
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
    val paymentOne: Payment = Payment(LocalDate.parse("2019-06-28", ISO_LOCAL_DATE), Some(0), Some(34.82), Some(10), None)
    val paymentTwo: Payment = Payment(LocalDate.parse("2019-04-30", ISO_LOCAL_DATE), Some(3000), Some(34.82), Some(11), Some(5))
    val paymentThree: Payment = Payment(LocalDate.parse("2019-04-30", ISO_LOCAL_DATE), Some(1200), Some(1), Some(8), None, leavingDate = Some(LocalDate.parse("2019-04-30", ISO_LOCAL_DATE)))
    val paymentFour: Payment = Payment(LocalDate.parse("2019-05-30", ISO_LOCAL_DATE), Some(1266), None, Some(10), None)
    val paymentFive: Payment = Payment(LocalDate.parse("2019-05-30", ISO_LOCAL_DATE), None, None, None, None,
      Some(1000), Some(2000), Some(3000), Some(4000), Some(5000), Some(300.00))
    val insufficientPayment: Payment = Payment(LocalDate.parse("2019-04-30", ISO_LOCAL_DATE), Some(3000), None, None, None, leavingDate = Some(LocalDate.parse("2019-04-15", ISO_LOCAL_DATE)))

    val ninoIdentifier: Nino = Nino("AA000000D")
    val utrIdentifier: SaUtr = SaUtr("12345678")

    val selectionNino: Selection = Selection(ninoIdentifier, utrIdentifier)
    val selectionNoNino: Selection = Selection(utrIdentifier)

    val paymentToDateQuestion: QuestionWithAnswers = QuestionWithAnswers(PaymentToDate, Seq("3000.00", "1200.00"), Map("currentTaxYear" -> "2019/20"))
    val employeeNIContributionsQuestion: QuestionWithAnswers = QuestionWithAnswers(EmployeeNIContributions, Seq("34.82", "34.82"), Map("currentTaxYear" -> "2019/20"))

    val paymentToDateQuestion2: QuestionWithAnswers = QuestionWithAnswers(PaymentToDate, Seq("3000.00", "1200.00"), Map("currentTaxYear" -> "2019/20", "previousTaxYear" -> "2018/19"))
    val employeeNIContributionsQuestion2: QuestionWithAnswers = QuestionWithAnswers(EmployeeNIContributions, Seq("34.82", "34.82"), Map("currentTaxYear" -> "2019/20", "previousTaxYear" -> "2018/19"))
  }

}
