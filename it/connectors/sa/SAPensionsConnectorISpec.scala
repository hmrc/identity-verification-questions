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

package connectors.sa

import iUtils.BaseISpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.services.utilities.TaxYear
import uk.gov.hmrc.identityverificationquestions.sources.sa.{SAPensionsConnector, SARecord, SAReturn}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class SAPensionsConnectorISpec extends BaseISpec {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)
  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().build()

  "get sa returns" should {
    "successfully obtain a return" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector : SAPensionsConnector = fakeApplication.injector.instanceOf[SAPensionsConnector]

      val result: Seq[SAReturn] = await(connector.getReturns(Nino("AA000003D"), 2019, 2019))

      result shouldBe List(SAReturn(TaxYear(2019), List(SARecord(BigDecimal(15), BigDecimal(2019.13)))))
    }
  }

}
