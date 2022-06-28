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

package uk.gov.hmrc.identityverificationquestions.sources.payslip

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.CoreGet
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.P60.{EarningsAbovePT, EmployeeNIContributions, PaymentToDate, PostgraduateLoanDeductions, StatutoryAdoptionPay, StatutoryMaternityPay, StatutorySharedParentalPay, StudentLoanDeductions}
import uk.gov.hmrc.identityverificationquestions.models.Payslip.{IncomeTax, NationalInsurance}
import uk.gov.hmrc.identityverificationquestions.models.{QuestionWithAnswers, ServiceName, p60Service}
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.services.QuestionService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration, PenceAnswerConvertor, TaxYearBuilder}
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Connector
import uk.gov.hmrc.identityverificationquestions.sources.QuestionServiceMeoMinimumNumberOfQuestions
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.collection.SortedSet

@Singleton
class PayslipService @Inject()(payslipConnector: PayslipConnector, val eventDispatcher: EventDispatcher, val auditService: AuditService)(implicit override val appConfig: AppConfig) extends QuestionService
  with CheckAvailability
  with CircuitBreakerConfiguration
  with TaxYearBuilder
  with PenceAnswerConvertor {

  lazy val payslipMonths: Int = appConfig.rtiNumberOfPayslipMonthsToCheck


  override type Record = Payment

  override def serviceName: ServiceName = p60Service

  override def connector: QuestionConnector[Payment] = payslipConnector

  override def evidenceTransformer(records: Seq[Payment]): Seq[QuestionWithAnswers] = {

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
