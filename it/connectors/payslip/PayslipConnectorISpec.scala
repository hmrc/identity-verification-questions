/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors.payslip

import java.time.LocalDate

import iUtils.BaseISpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class PayslipConnectorISpec extends BaseISpec {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)

  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().build()

  "get payslip returns" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val paymentDate: LocalDate = LocalDate.now().minusMonths(1).minusDays(25)

    "successfully obtain data for nino AA000003D" in {
      val ninoIdentifier: Nino = Nino("AA000003D")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector: PayslipConnector = fakeApplication.injector.instanceOf[PayslipConnector]

      val result = await(connector.getRecords(selectionNino))
      result.toList.head shouldBe Payment(paymentDate, Some(3000), Some(120.99), Some(155.02), Some(100.02), None, None, None, None, None, None)
    }
  }
}
