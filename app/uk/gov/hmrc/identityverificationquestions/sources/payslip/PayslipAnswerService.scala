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

package uk.gov.hmrc.identityverificationquestions.sources.payslip

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.AnswerConnector
import uk.gov.hmrc.identityverificationquestions.models.Payslip.{IncomeTax, NationalInsurance}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.services.AnswerService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import scala.concurrent.ExecutionContext

@Singleton
class PayslipAnswerService @Inject()(payslipAnswerConnector: PayslipAnswerConnector, as: AuditService)(implicit val appConfig: AppConfig, ec: ExecutionContext)
  extends AnswerService with CheckAvailability with CircuitBreakerConfiguration {

  override type Record = QuestionResult

  override def auditService: AuditService = as

  override def serviceName: ServiceName = payslipService

  override def connector: AnswerConnector[QuestionResult] = payslipAnswerConnector

  override def supportedQuestions: Seq[QuestionKey] = Seq(IncomeTax, NationalInsurance)

  override def answerTransformer(records: Seq[QuestionResult], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] = records

}
