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
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.Payslip.{IncomeTax, NationalInsurance}
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.models.{CorrelationId, QuestionWithAnswers, ServiceName, payslipService}
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration, PenceAnswerConvertor, TaxYearBuilder}
import uk.gov.hmrc.identityverificationquestions.sources.QuestionServiceMeoMinimumNumberOfQuestions

@Singleton
class PayslipService @Inject()(payslipConnector: PayslipConnector,
                               val eventDispatcher: EventDispatcher,
                               val auditService: AuditService,
                               val appConfig: AppConfig,
                               val metricsService: MetricsService) extends QuestionServiceMeoMinimumNumberOfQuestions
  with CheckAvailability
  with CircuitBreakerConfiguration
  with TaxYearBuilder
  with PenceAnswerConvertor {

  lazy val payslipMonths: Int = appConfig.rtiNumberOfPayslipMonthsToCheck(serviceName)

  override type Record = Payment

  override def serviceName: ServiceName = payslipService

  override def connector: QuestionConnector[Payment] = payslipConnector

  override def evidenceTransformer(records: Seq[Payment], corrId: CorrelationId): Seq[QuestionWithAnswers] = {
    def additionalInfoMap = Map("months" -> payslipMonths.toString)

    val payslipQuestions: Seq[QuestionWithAnswers] = {
      val incomeTaxAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.incomeTax).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(IncomeTax, answers.map(_.toString), additionalInfoMap))
      }
      val nationalInsuranceAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.nationalInsurancePaid).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(NationalInsurance, answers.map(_.toString), additionalInfoMap))
      }
      incomeTaxAnswers ++ nationalInsuranceAnswers
    }

    payslipQuestions
  }
}
