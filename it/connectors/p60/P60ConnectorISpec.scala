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

package connectors.p60

import iUtils.BaseISpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Connector

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

    "successfully obtain data for nino AA002022B" in {
      val ninoIdentifier: Nino = Nino("AA002022B")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector: P60Connector = fakeApplication.injector.instanceOf[P60Connector]

      val result = await(connector.getRecords(selectionNino))
      result.toList.head shouldBe Payment(paymentDate, Some(0), Some(0), Some(155.02), Some(100.02),
                                          earningsAbovePT = Some(0),
                                          statutoryMaternityPay = Some(300.02),
                                          statutorySharedParentalPay = Some(0),
                                          statutoryAdoptionPay = Some(0),
                                          studentLoanDeductions = Some(800.02),
                                          postgraduateLoanDeductions = Some(0))
    }

    "successfully obtain data for nino AA002023B" in {
      val ninoIdentifier: Nino = Nino("AA002023B")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector : P60Connector = fakeApplication.injector.instanceOf[P60Connector]

      val result = await(connector.getRecords(selectionNino))

      result.toList.head shouldBe Payment(paymentDate, Some(0), Some(0), Some(155.02), Some(100.02),
                                          earningsAbovePT = Some(0),
                                          statutoryMaternityPay = Some(0),
                                          statutorySharedParentalPay = Some(0),
                                          statutoryAdoptionPay = Some(400.02),
                                          studentLoanDeductions = Some(0),
                                          postgraduateLoanDeductions = Some(300.02))
    }
    "successfully obtain data for nino AA002024B" in {
      val ninoIdentifier: Nino = Nino("AA002024B")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector : P60Connector = fakeApplication.injector.instanceOf[P60Connector]

      val result = await(connector.getRecords(selectionNino))

      result.toList.head shouldBe Payment(paymentDate, Some(0), Some(0), Some(155.02), Some(100.02),
                                          earningsAbovePT = Some(200.02),
                                          statutoryMaternityPay = Some(0),
                                          statutorySharedParentalPay = Some(500.02),
                                          statutoryAdoptionPay = Some(0),
                                          studentLoanDeductions = Some(0),
                                          postgraduateLoanDeductions = Some(0))
    }
    "return UpstreamErrorResponse when P60 data not found for nino AA002099B" in {
      val ninoIdentifier: Nino = Nino("AA002099B")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector: P60Connector = fakeApplication.injector.instanceOf[P60Connector]
      val result = await(connector.getRecords(selectionNino))
      result shouldBe Seq[Payment]()
    }
  }
}
