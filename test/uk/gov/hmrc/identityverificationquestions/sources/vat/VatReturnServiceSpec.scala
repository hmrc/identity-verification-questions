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

package uk.gov.hmrc.identityverificationquestions.sources.vat

import Utils.UnitSpec
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.Vat.{ValueOfPurchasesAmount, ValueOfSalesAmount}
import uk.gov.hmrc.identityverificationquestions.models.{QuestionWithAnswers, VatReturnSubmission, vatService}
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService

class VatReturnServiceSpec extends UnitSpec {

  "Service Name should be set" in new Setup {
    service.serviceName shouldBe vatService
  }

  "evidenceTransformer" should {
    "return Empty Sequence" when {
      "there is a VatReturnSubmission record but totalValueSalesExVAT is less then 0" in new Setup {
        service.evidenceTransformer(vatReturnSubmissionData(BigDecimal(-1),BigDecimal(500.50)), corrId) shouldBe Seq()
      }

      "there is a VatReturnSubmission record but totalValuePurchasesExVAT is less then 0" in new Setup {
        service.evidenceTransformer(vatReturnSubmissionData(BigDecimal(1000), BigDecimal(-1)), corrId) shouldBe Seq()
      }

      "there is a VatReturnSubmission record but totalValueSalesExVAT and totalValuePurchasesExVAT is less then 0" in new Setup {
        service.evidenceTransformer(vatReturnSubmissionData(BigDecimal(-1), BigDecimal(-1)), corrId) shouldBe Seq()
      }
    }
    "return the data" when{
      "there is a VatReturnSubmission when totalValueSalesExVAT and totalValuePurchasesExVAT is greater then 0" in new Setup {
        service.evidenceTransformer(vatReturnSubmissionData(BigDecimal(1000), BigDecimal(500.50)), corrId) shouldBe List(QuestionWithAnswers(ValueOfSalesAmount, Seq(BigDecimal(1000).toString()), Map()), QuestionWithAnswers(ValueOfPurchasesAmount, Seq(BigDecimal(500.50).toString()),Map()))

      }
    }
  }

  trait Setup {
    val mockAppConfig: AppConfig = mock[AppConfig]
    val mockVatReturnsConnector: VatReturnsConnector = mock[VatReturnsConnector]
    val mockEventDispatcher: EventDispatcher = mock[EventDispatcher]
    val mockAuditService: AuditService = mock[AuditService]
    val service: VatReturnsService = new VatReturnsService(mockVatReturnsConnector, mockEventDispatcher, mockAuditService, mockAppConfig)
    def vatReturnSubmissionData(totalValueSalesExVAT: BigDecimal, totalValuePurchasesExVAT: BigDecimal): Seq[VatReturnSubmission] =
      Seq(VatReturnSubmission("22YA", BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), BigDecimal("1000"), totalValueSalesExVAT, totalValuePurchasesExVAT, BigDecimal("1000"), BigDecimal("1000")))
  }
}
