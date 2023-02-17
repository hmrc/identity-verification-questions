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

import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.Vat.{ValueOfPurchasesAmount, ValueOfSalesAmount}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration, PenceAnswerConvertor, TaxYearBuilder}
import uk.gov.hmrc.identityverificationquestions.sources.QuestionServiceMeoMinimumNumberOfQuestions

import javax.inject.{Inject, Singleton}

@Singleton
class VatReturnsService @Inject()(vatReturnsConnector: VatReturnsConnector, val eventDispatcher: EventDispatcher, val auditService: AuditService)(implicit override val appConfig: AppConfig)
  extends QuestionServiceMeoMinimumNumberOfQuestions
    with CheckAvailability with CircuitBreakerConfiguration with PenceAnswerConvertor {

  override type Record = VatReturnSubmission

  override def serviceName: ServiceName = vatService

  override def connector: QuestionConnector[VatReturnSubmission] = vatReturnsConnector

  override def evidenceTransformer(records: Seq[VatReturnSubmission]): Seq[QuestionWithAnswers] = {
    records.flatMap{ vatData =>
      if (vatData.totalValuePurchasesExVAT > 0 || vatData.totalValueSalesExVAT > 0){
        Seq(
          QuestionWithAnswers(ValueOfSalesAmount, Seq(vatData.totalValueSalesExVAT.toString())),
          QuestionWithAnswers(ValueOfPurchasesAmount, Seq(vatData.totalValuePurchasesExVAT.toString()))
        )
      } else
        Seq()
    }
  }

}
