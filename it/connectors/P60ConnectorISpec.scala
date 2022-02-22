/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package connectors

import iUtils.BaseISpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Connector
import uk.gov.hmrc.questionrepository.models.identifier.NinoI
import uk.gov.hmrc.questionrepository.models.payment.Payment
import uk.gov.hmrc.questionrepository.models.{Origin, Selection}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class P60ConnectorISpec extends BaseISpec {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)
  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().build()

  "get p60 returns" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val paymentDate: LocalDate = LocalDate.now().minusMonths(1).minusDays(25)
    val origin: Origin = Origin("test-origin")

    "successfully obtain data for nino AA002022B" in {
      val ninoIdentifier: NinoI = NinoI("AA002022B")
      val selectionNino: Selection = Selection(origin, Seq(ninoIdentifier))

      val connector: P60Connector = fakeApplication.injector.instanceOf[P60Connector]

      val result = await(connector.getRecords(selectionNino))
      result shouldBe List(Payment(paymentDate, Some(0), Some(0), Some(155.02), Some(100.02),
        earningsAbovePT = Some(0),
        statutoryMaternityPay = Some(300.02),
        statutorySharedParentalPay = Some(0),
        statutoryAdoptionPay = Some(0),
        studentLoanDeductions = Some(800.02),
        postgraduateLoanDeductions = Some(0)))
    }

    "successfully obtain data for nino AA002023B" in {
      val ninoIdentifier: NinoI = NinoI("AA002023B")
      val selectionNino: Selection = Selection(origin, Seq(ninoIdentifier))

      val connector : P60Connector = fakeApplication.injector.instanceOf[P60Connector]

      val result = await(connector.getRecords(selectionNino))

      result shouldBe List(Payment(paymentDate, Some(0), Some(0), Some(155.02), Some(100.02),
        earningsAbovePT = Some(0),
        statutoryMaternityPay = Some(0),
        statutorySharedParentalPay = Some(0),
        statutoryAdoptionPay = Some(400.02),
        studentLoanDeductions = Some(0),
        postgraduateLoanDeductions = Some(300.02)))
    }
    "successfully obtain data for nino AA002024B" in {
      val ninoIdentifier: NinoI = NinoI("AA002024B")
      val selectionNino: Selection = Selection(origin, Seq(ninoIdentifier))

      val connector : P60Connector = fakeApplication.injector.instanceOf[P60Connector]

      val result = await(connector.getRecords(selectionNino))

      result shouldBe List(Payment(paymentDate, Some(0), Some(0), Some(155.02), Some(100.02),
        earningsAbovePT = Some(200.02),
        statutoryMaternityPay = Some(0),
        statutorySharedParentalPay = Some(500.02),
        statutoryAdoptionPay = Some(0),
        studentLoanDeductions = Some(0),
        postgraduateLoanDeductions = Some(0)))
    }
  }
}
