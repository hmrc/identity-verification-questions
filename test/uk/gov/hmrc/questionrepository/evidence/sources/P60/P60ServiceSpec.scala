/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.P60

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

import Utils.{LogCapturing, UnitSpec}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.{P60Connector, P60Service}
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.models.identifier.{NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.payment.Payment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class P60ServiceSpec extends UnitSpec with LogCapturing {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe p60Service
  }

  "calling `questions`" should {
    "return a sequence of Question's" when {
      "P60Connector returns a non empty sequence of Payment's" in new WithStubbing {
        when(mockP60Connector.getRecords(any)(any, any)).thenReturn(Future.successful(Seq(paymentOne, paymentTwo, paymentThree)))
        when(mockAppConfig.bufferInMonthsForService(service.serviceName)).thenReturn(2)

        service.questions(selectionNino).futureValue shouldBe Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
      }

      "P60Connector returns a non empty sequence of Payment's with previous year in additional information" in new WithStubbing {
        when(mockP60Connector.getRecords(any)(any, any)).thenReturn(Future.successful(Seq(paymentOne, paymentTwo, paymentThree)))
        when(mockAppConfig.bufferInMonthsForService(service.serviceName)).thenReturn(3)

        service.questions(selectionNino).futureValue shouldBe Seq(paymentToDateQuestion2, employeeNIContributionsQuestion2)
      }
    }

    "return a empty sequence of Question's" when {
      "Evidence source in Not available" in new Setup {
        when(mockAppConfig.serviceStatus(eqTo[ServiceName](service.serviceName))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino")))

        service.questions(selectionNoNino).futureValue shouldBe Seq()
      }

      "P60Connector returns an empty sequence of Payment's" in new WithStubbing {
        when(mockP60Connector.getRecords(any)(any, any)).thenReturn(Future.successful(Seq()))

        service.questions(selectionNino).futureValue shouldBe Seq()
      }
    }
  }

  trait Setup extends TestDate {
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    val mockP60Connector: P60Connector = mock[P60Connector]
    val service: P60Service = new P60Service(mockP60Connector) {
      override def today: LocalDate = LocalDate.parse("2020-06-28", ISO_LOCAL_DATE)
    }
  }

  trait WithStubbing extends Setup {
    when(mockAppConfig.serviceStatus(eqTo[ServiceName](service.serviceName))).thenReturn(mockAppConfig.ServiceState(None, List.empty, List.empty, List("nino")))
    when(mockAppConfig.serviceCbNumberOfCallsToTrigger(service.serviceName)).thenReturn(Some(20))
    when(mockAppConfig.serviceCbUnavailableDurationInSec(service.serviceName)).thenReturn(Some(60))
    when(mockAppConfig.serviceCbUnstableDurationInSec(service.serviceName)).thenReturn(Some(300))
  }

  trait TestDate {
    val paymentOne: Payment = Payment(LocalDate.parse("2019-06-28", ISO_LOCAL_DATE), Some(0), Some(34.82), Some(10), None)
    val paymentTwo: Payment = Payment(LocalDate.parse("2019-04-30", ISO_LOCAL_DATE), Some(3000), Some(34.82), Some(11), Some(5))
    val paymentThree: Payment = Payment(LocalDate.parse("2019-04-30", ISO_LOCAL_DATE), Some(1200), None, Some(8), None)
    val paymentFour: Payment = Payment(LocalDate.parse("2019-05-30", ISO_LOCAL_DATE), Some(1266), None, Some(10), None)

    val ninoIdentifier: NinoI = NinoI("AA000000D")
    val utrIdentifier: SaUtrI = SaUtrI("12345678")

    val origin: Origin = Origin("testOrigin")

    val selectionNino: Selection = Selection(origin, Seq(ninoIdentifier, utrIdentifier))
    val selectionNoNino: Selection = Selection(origin, Seq(utrIdentifier))

    val paymentToDateQuestion: Question = Question(PaymentToDate, Seq("3000.00", "1200.00"), Map("currentTaxYear" -> "2019/20"))
    val employeeNIContributionsQuestion: Question = Question(EmployeeNIContributions, Seq("34.00", "34.00"), Map("currentTaxYear" -> "2019/20"))

    val paymentToDateQuestion2: Question = Question(PaymentToDate, Seq("3000.00", "1200.00"), Map("currentTaxYear" -> "2019/20", "previousTaxYear" -> "2018/19"))
    val employeeNIContributionsQuestion2: Question = Question(EmployeeNIContributions, Seq("34.00", "34.00"), Map("currentTaxYear" -> "2019/20", "previousTaxYear" -> "2018/19"))
  }
}
