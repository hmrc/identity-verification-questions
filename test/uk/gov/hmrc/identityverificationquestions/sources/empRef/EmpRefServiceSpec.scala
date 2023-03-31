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

import Utils.{LogCapturing, UnitSpec}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.PayeRefQuestion.{AmountOfPayment, DateOfPayment}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService

import java.time.LocalDate

class EmpRefServiceSpec extends UnitSpec with LogCapturing {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe desPayeService
  }

  "evidenceTransformer" should {
    "return Nil" when {
      "there is no PayePaymentsDetails records" in new Setup {
        val records: Seq[PayePaymentsDetails] = Seq.empty
        val expectedResult: Seq[QuestionWithAnswers] = Nil
        service.evidenceTransformer(records, corrId) shouldBe expectedResult
      }
    }

    "return Nil" when {
      "there is are PayePaymentsDetails records, but there is no payments" in new Setup {
        val records: Seq[PayePaymentsDetails] = Seq(PayePaymentsDetails(None))
        val expectedResult: Seq[QuestionWithAnswers] = Nil
        service.evidenceTransformer(records, corrId) shouldBe expectedResult
      }
    }

    "return Nil" when {
      "there is are PayePaymentsDetails records, but there is no payments case two" in new Setup {
        val records: Seq[PayePaymentsDetails] = Seq(PayePaymentsDetails(Some(List.empty[PayePayment])))
        val expectedResult: Seq[QuestionWithAnswers] = Nil
        service.evidenceTransformer(records, corrId) shouldBe expectedResult
      }
    }

    "return Question" when {
      "there is are PayePaymentsDetails records, and there is payments" in new Setup {
        val testDate: String = LocalDate.now().toString
        val records: Seq[PayePaymentsDetails] = Seq(PayePaymentsDetails(Some(List(PayePayment(PayePaymentAmount(11.11, "GB"), testDate)))))
        val expectedResult: Seq[QuestionWithAnswers] = Seq(QuestionWithAnswers(DateOfPayment, List(testDate)), QuestionWithAnswers(AmountOfPayment, List("11.11")))
        service.evidenceTransformer(records, corrId) shouldBe expectedResult
      }
    }
  }

  trait Setup {
    val mockAppConfig: AppConfig = mock[AppConfig]
    val mockEmpRefConnector: EmpRefConnector = mock[EmpRefConnector]
    val mockEventDispatcher:EventDispatcher = mock[EventDispatcher]
    val mockAuditService: AuditService = mock[AuditService]
    val metricsService: MetricsService = mock[MetricsService]
    val service: EmpRefService = new EmpRefService(mockEmpRefConnector, mockEventDispatcher, mockAuditService, mockAppConfig, metricsService)
  }

}
