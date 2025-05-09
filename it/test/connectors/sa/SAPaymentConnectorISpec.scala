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

package test.connectors.sa

import java.time.LocalDate
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import test.iUtils.BaseISpec
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.sources.sa.{SAPayment, SAPaymentReturn, SAPaymentsConnector}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class SAPaymentConnectorISpec extends BaseISpec {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)
  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().build()

  "get sa payment returns" should {
    "successfully obtain a return" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector : SAPaymentsConnector = fakeApplication.injector.instanceOf[SAPaymentsConnector]

      val result: Seq[SAPaymentReturn] = await(connector.getReturns(SaUtr("1234567890")))

      result shouldBe Seq(SAPaymentReturn(Seq(SAPayment(4278.39, Some(LocalDate.parse("2024-03-11")), Some("PYT")))))
    }
  }

}
