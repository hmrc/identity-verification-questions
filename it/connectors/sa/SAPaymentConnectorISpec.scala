/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package connectors.sa

import iUtils.BaseISpec
import org.joda.time.LocalDate
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.sa.{SAPayment, SAPaymentReturn, SAPaymentsConnector}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SAPaymentConnectorISpec extends BaseISpec {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)
  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().build()

  "get sa payment returns" should {
    "successfully obtain a return" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector : SAPaymentsConnector = fakeApplication.injector.instanceOf[SAPaymentsConnector]

      val result: Seq[SAPaymentReturn] = await(connector.getReturns(SaUtr("1234567890")))

      result shouldBe Seq(SAPaymentReturn(Seq(SAPayment(4278.39, Some(LocalDate.parse("2021-03-11")), Some("PYT")))))
    }
  }

}
