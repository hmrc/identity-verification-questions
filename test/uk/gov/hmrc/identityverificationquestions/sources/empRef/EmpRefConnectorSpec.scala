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

package uk.gov.hmrc.identityverificationquestions.sources.empRef

import Utils.UnitSpec
import mocks.MockHttpClientV2
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService

import java.time.LocalDate

class EmpRefConnectorSpec extends UnitSpec with MockHttpClientV2 {

  "lastTwoYearsOfPayments" should {
    "return the last 2 years of payment details" in new Setup {

      val payment1Date: LocalDate = LocalDate.now().minusYears(1)
      val payment2Date: LocalDate = LocalDate.now().minusMonths(23)
      val payment3Date: LocalDate = LocalDate.now().minusYears(3)
      val payment1: PayePayment = PayePayment(PayePaymentAmount(300, "GBP"), payment1Date.toString)
      val payment2: PayePayment = PayePayment(PayePaymentAmount(400, "GBP"), payment2Date.toString)
      val payment3: PayePayment = PayePayment(PayePaymentAmount(500, "GBP"), payment3Date.toString)

      val paymentsDetailsLastTwoYears: PayePaymentsDetails = PayePaymentsDetails(Some(List(payment1, payment2)))

      empRefConnector.lastTwoYearsOfPayments(List(payment1, payment2, payment3)) shouldBe paymentsDetailsLastTwoYears
    }

  }

  trait Setup {
    val metricsService: MetricsService = mock[MetricsService]
    val mockAppConfig: AppConfig = mock[AppConfig]
    val empRefConnector = new EmpRefConnector(mockHttpClientV2, metricsService, mockAppConfig)
  }
}
