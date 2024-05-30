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

package test.connectors.ntc

import org.joda.time.LocalDate
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import test.iUtils.BaseISpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.taxcredit.{CTC, TaxCreditPayment}
import uk.gov.hmrc.identityverificationquestions.sources.ntc.NtcConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class NtcConnectorISpec extends BaseISpec {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)

  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().build()

  "get ntc returns" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    "successfully obtain data for nino AA000003D" in {
      val ninoIdentifier: Nino = Nino("AA000003D")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector: NtcConnector = fakeApplication.injector.instanceOf[NtcConnector]

      val result = await(connector.getRecords(selectionNino))

      result.toList.head shouldBe TaxCreditPayment(LocalDate.now().minusMonths(2).minusDays(5), BigDecimal("264.16"), CTC)
      /* assert against following TaxCreditPayment record
      {
      subjectDate: "2022-12-09",
      amount: -379.3,
      taxCreditId: "WTC",
      paymentType: "REGULAR "
      } */
      result.toList(2).asInstanceOf[TaxCreditPayment].amount.toString shouldBe "379.30"

    }
  }
}
