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

package test.connectors.p45

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import test.iUtils.BaseISpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.sources.P45.P45Connector

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

class P45ConnectorISpec extends BaseISpec {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)
  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().build()

  "get p45 returns" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val paymentDate: LocalDate = LocalDate.now().minusMonths(6).minusDays(5)

    "successfully obtain data for nino AA000003B" in {
      val ninoIdentifier: Nino = Nino("AA000003B")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector: P45Connector = fakeApplication.injector.instanceOf[P45Connector]

      val result = await(connector.getRecords(selectionNino))
      result.toList.head shouldBe Payment(paymentDate, Some(0), Some(130.99), Some(155.02), Some(100.02), leavingDate = Some(LocalDate.parse("2012-06-22", ISO_LOCAL_DATE)), totalTaxYTD = Some(130.99))
    }

    "successfully obtain data for nino AA000045A" in {
      val ninoIdentifier: Nino = Nino("AA000045A")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector : P45Connector = fakeApplication.injector.instanceOf[P45Connector]

      val result = await(connector.getRecords(selectionNino))

      result.toList.head shouldBe Payment(paymentDate, Some(45.99), Some(130.99), Some(155.02), Some(100.02), leavingDate = Some(LocalDate.parse("2013-06-22", ISO_LOCAL_DATE)), totalTaxYTD = Some(145.99))
    }

    "return UpstreamErrorResponse when P45 data not found for nino AA002099B" in {
      val ninoIdentifier: Nino = Nino("AA002099B")
      val selectionNino: Selection = Selection(ninoIdentifier)

      val connector: P45Connector = fakeApplication.injector.instanceOf[P45Connector]
      val result = await(connector.getRecords(selectionNino))
      result shouldBe Seq[Payment]()
    }
  }
}
